package com.example.voice

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.ai.FoniGeminiLiveAudioClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.util.Locale

/**
 * Gestor de voz para Foni ("La telefonía morada").
 * Implementa síntesis de voz natural en español de México (es-MX) con entonación cálida,
 * soporte de interrupción instantánea (barge-in) y limpieza fonética de markdown.
 */
class DalefonSpeechManager(
    private val context: Context,
    private val onSpeechStarted: () -> Unit,
    private val onSpeechEnded: () -> Unit,
    private val onVoiceInputResult: (String) -> Unit,
    private val onPartialResult: ((String) -> Unit)? = null,
    private val onRmsChanged: ((Float) -> Unit)? = null,
    private val onRecognitionError: ((Int) -> Unit)? = null,
    private val onBargeIn: (() -> Unit)? = null
) {
    private var textToSpeech: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var mediaPlayer: MediaPlayer? = null
    private var liveAudioJob: Job? = null
    private var rmsPulseJob: Job? = null
    private var isTtsReady = false
    private var isMuted = false
    private val scope = CoroutineScope(Dispatchers.Main)
    private var simulationJob: Job? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    init {
        initTts()
        initSpeechRecognizer()
    }

    private fun initTts() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val localeMx = Locale.forLanguageTag("es-MX")
                var langResult = textToSpeech?.setLanguage(localeMx)

                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    langResult = textToSpeech?.setLanguage(Locale.forLanguageTag("es-ES"))
                }

                // Ajuste de timbre de voz para Foni: Cálido, amigable, claro y resolutivo
                textToSpeech?.setPitch(1.10f)
                textToSpeech?.setSpeechRate(1.03f)

                // Intentar seleccionar la voz más natural en español de México disponible en el dispositivo
                try {
                    val availableVoices = textToSpeech?.voices
                    if (!availableVoices.isNullOrEmpty()) {
                        val selectedVoice = availableVoices.firstOrNull { v ->
                            v.locale.language == "es" &&
                                (v.locale.country.equals("MX", ignoreCase = true) || v.name.contains("es-mx", ignoreCase = true)) &&
                                !v.isNetworkConnectionRequired
                        } ?: availableVoices.firstOrNull { v ->
                            v.locale.language == "es"
                        }

                        if (selectedVoice != null) {
                            textToSpeech?.voice = selectedVoice
                        }
                    }
                } catch (e: Exception) {
                    Log.d("DalefonSpeech", "Selección de voz alternativa: ${e.message}")
                }

                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        scope.launch {
                            onSpeechStarted()
                        }
                    }

                    override fun onDone(utteranceId: String?) {
                        scope.launch {
                            onSpeechEnded()
                        }
                    }

                    override fun onError(utteranceId: String?) {
                        scope.launch {
                            onSpeechEnded()
                        }
                    }
                })
                isTtsReady = true
            } else {
                Log.w("DalefonSpeech", "TTS no inicializado. Usando temporización simulada.")
            }
        }
    }

    private fun initSpeechRecognizer() {
        mainHandler.post {
            try {
                if (SpeechRecognizer.isRecognitionAvailable(context)) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(object : RecognitionListener {
                            override fun onReadyForSpeech(params: Bundle?) {
                                Log.d("DalefonSpeech", "Reconocimiento de voz listo")
                            }

                            override fun onBeginningOfSpeech() {
                                // Interrupción inmediata (Barge-in): si el usuario empieza a hablar, silenciamos a Foni
                                stopSpeaking(notifyEnded = false)
                                onBargeIn?.invoke()
                            }

                            override fun onRmsChanged(rmsdB: Float) {
                                onRmsChanged?.invoke(rmsdB)
                            }

                            override fun onBufferReceived(buffer: ByteArray?) {}
                            override fun onEndOfSpeech() {}

                            override fun onError(error: Int) {
                                Log.w("DalefonSpeech", "SpeechRecognizer error code: $error")
                                onRecognitionError?.invoke(error)
                            }

                            override fun onResults(results: Bundle?) {
                                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                if (!matches.isNullOrEmpty()) {
                                    val text = matches[0]
                                    if (text.isNotBlank()) {
                                        onVoiceInputResult(text)
                                    }
                                }
                            }

                            override fun onPartialResults(partialResults: Bundle?) {
                                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                                if (!matches.isNullOrEmpty()) {
                                    onPartialResult?.invoke(matches[0])
                                }
                            }

                            override fun onEvent(eventType: Int, params: Bundle?) {}
                        })
                    }
                } else {
                    Log.i("DalefonSpeech", "Reconocimiento de voz nativo no disponible en este entorno.")
                }
            } catch (e: Exception) {
                Log.w("DalefonSpeech", "Error al inicializar SpeechRecognizer: ${e.message}")
            }
        }
    }

    fun isSpeechRecognitionAvailable(): Boolean {
        return speechRecognizer != null
    }

    fun speak(text: String) {
        liveAudioJob?.cancel()
        simulationJob?.cancel()
        stopSpeaking(notifyEnded = false)

        if (isMuted) {
            simulateSpeechTiming(text)
            return
        }

        val speechCleanText = cleanTextForSpeech(text)

        // 1. PRIORIDAD: Síntesis con Gemini Live Audio (voz neural humana realista de Google)
        liveAudioJob = scope.launch {
            onSpeechStarted()
            val liveAudioFile = try {
                FoniGeminiLiveAudioClient.synthesizeSpeechAudio(speechCleanText, context)
            } catch (e: Exception) {
                Log.w("DalefonSpeech", "Error al invocar Gemini Live Audio: ${e.message}")
                null
            }

            if (liveAudioFile != null && liveAudioFile.exists()) {
                playLiveAudio(liveAudioFile)
            } else {
                // 2. RESPALDO LOCAL: Si no hay clave de API o está offline, usa TextToSpeech local
                fallbackToLocalTts(speechCleanText)
            }
        }
    }

    private fun playLiveAudio(audioFile: File) {
        try {
            stopLiveAudioPlayer()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFile.absolutePath)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                        .build()
                )
                prepare()
                start()
                startLiveRmsSimulation()

                setOnCompletionListener {
                    stopLiveRmsSimulation()
                    stopLiveAudioPlayer()
                    try {
                        audioFile.delete()
                    } catch (e: Exception) {}
                    onSpeechEnded()
                }

                setOnErrorListener { mp, _, _ ->
                    stopLiveRmsSimulation()
                    stopLiveAudioPlayer()
                    try {
                        audioFile.delete()
                    } catch (e: Exception) {}
                    onSpeechEnded()
                    true
                }
            }
        } catch (e: Exception) {
            Log.e("DalefonSpeech", "Error al reproducir Live Audio: ${e.message}")
            fallbackToLocalTts(audioFile.name)
        }
    }

    private fun startLiveRmsSimulation() {
        rmsPulseJob?.cancel()
        rmsPulseJob = scope.launch {
            while (mediaPlayer?.isPlaying == true) {
                val simulatedRms = (3.5f + (kotlin.random.Random.nextFloat() * 5.0f))
                onRmsChanged?.invoke(simulatedRms)
                delay(120)
            }
            onRmsChanged?.invoke(0f)
        }
    }

    private fun stopLiveRmsSimulation() {
        rmsPulseJob?.cancel()
        rmsPulseJob = null
        onRmsChanged?.invoke(0f)
    }

    private fun stopLiveAudioPlayer() {
        try {
            if (mediaPlayer != null) {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.stop()
                }
                mediaPlayer?.release()
                mediaPlayer = null
            }
        } catch (e: Exception) {
            Log.w("DalefonSpeech", "Error al liberar MediaPlayer: ${e.message}")
        }
    }

    private fun fallbackToLocalTts(speechCleanText: String) {
        if (isTtsReady && textToSpeech != null) {
            val params = Bundle()
            val utteranceId = "foni_${System.currentTimeMillis()}"
            val result = textToSpeech?.speak(speechCleanText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
            if (result != TextToSpeech.SUCCESS) {
                simulateSpeechTiming(speechCleanText)
            }
        } else {
            simulateSpeechTiming(speechCleanText)
        }
    }

    /**
     * Limpia caracteres de formato markdown y emojis para que la síntesis de voz
     * no pronuncie "asterisco asterisco" ni símbolos de formateo.
     */
    private fun cleanTextForSpeech(text: String): String {
        return text
            .replace("**", "")
            .replace("*", "")
            .replace("`", "")
            .replace("#", "")
            .replace("•", "")
            .replace("💜", "")
            .replace("📱", "")
            .replace("⚙️", "")
            .replace("🤝", "")
            .replace("✨", "")
            .replace("👍", "")
            .replace("🎉", "")
            .replace("  ", " ")
            .trim()
    }

    private fun simulateSpeechTiming(text: String) {
        simulationJob = scope.launch {
            onSpeechStarted()
            val duration = (text.length * 60L).coerceIn(1800L, 8000L)
            delay(duration)
            onSpeechEnded()
        }
    }

    fun stopSpeaking(notifyEnded: Boolean = true) {
        liveAudioJob?.cancel()
        simulationJob?.cancel()
        stopLiveRmsSimulation()
        stopLiveAudioPlayer()
        textToSpeech?.stop()
        if (notifyEnded) {
            onSpeechEnded()
        }
    }

    fun startListening() {
        // Interrumpir cualquier reproducción de audio anterior sin forzar estado IDLE
        stopSpeaking(notifyEnded = false)

        mainHandler.post {
            if (speechRecognizer != null) {
                try {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "es-MX")
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Habla con Foni...")
                        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    }
                    speechRecognizer?.startListening(intent)
                } catch (e: Exception) {
                    Log.w("DalefonSpeech", "Error al iniciar reconocimiento de voz: ${e.message}")
                }
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.w("DalefonSpeech", "Error al detener reconocimiento de voz: ${e.message}")
            }
        }
    }

    fun setMuted(muted: Boolean) {
        this.isMuted = muted
        if (muted) {
            stopSpeaking()
        }
    }

    fun isMuted(): Boolean = isMuted

    fun destroy() {
        liveAudioJob?.cancel()
        simulationJob?.cancel()
        stopLiveRmsSimulation()
        stopLiveAudioPlayer()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                Log.w("DalefonSpeech", "Error al destruir SpeechRecognizer: ${e.message}")
            }
        }
    }
}
