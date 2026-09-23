package com.example.ai

import android.content.Context
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

/**
 * Cliente para el servicio de Gemini Live Audio / TTS de Google.
 * Utiliza el modelo 'gemini-2.5-flash-preview-tts' con voz preconstruida de alta fidelidad
 * ("Aoede" / "Kore") para producir voz humana sumamente natural, expresiva y cálida
 * en español mexicano, reemplazando la lectura robótica del sistema Android.
 */
object FoniGeminiLiveAudioClient {

    private const val TAG = "FoniLiveAudio"
    private const val TTS_MODEL = "gemini-2.5-flash-preview-tts"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$TTS_MODEL:generateContent"

    // Voz neural recomendada para Foni (tono cálido, empático, natural)
    private const val DEFAULT_VOICE_NAME = "Aoede"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Genera un archivo de audio WAV de alta fidelidad con voz natural humana a partir del texto.
     * Retorna el archivo temporal listo para reproducción, o null si la API no está disponible.
     */
    suspend fun synthesizeSpeechAudio(text: String, context: Context): File? = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            Log.d(TAG, "Clave de Gemini no configurada; omitiendo síntesis Live Audio.")
            return@withContext null
        }

        val cleanText = sanitizeTextForLiveSpeech(text)
        if (cleanText.isBlank()) {
            return@withContext null
        }

        try {
            val requestJson = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", cleanText)
                            })
                        })
                    })
                })

                put("generationConfig", JSONObject().apply {
                    put("responseModalities", JSONArray().apply {
                        put("AUDIO")
                    })
                    put("speechConfig", JSONObject().apply {
                        put("voiceConfig", JSONObject().apply {
                            put("prebuiltVoiceConfig", JSONObject().apply {
                                put("voiceName", DEFAULT_VOICE_NAME)
                            })
                        })
                    })
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url("$BASE_URL?key=$apiKey")
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string()

            if (response.isSuccessful && !responseBody.isNullOrEmpty()) {
                val json = JSONObject(responseBody)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val candidate = candidates.getJSONObject(0)
                    val content = candidate.optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val part = parts.getJSONObject(0)
                        val inlineData = part.optJSONObject("inlineData")
                        if (inlineData != null) {
                            val mimeType = inlineData.optString("mimeType", "")
                            val base64Audio = inlineData.optString("data", "")
                            if (base64Audio.isNotBlank()) {
                                val rawBytes = Base64.decode(base64Audio, Base64.DEFAULT)
                                return@withContext processAudioBytesToFile(rawBytes, mimeType, context)
                            }
                        }
                    }
                }
            } else {
                Log.w(TAG, "Error en respuesta Gemini Live Audio: ${response.code} $responseBody")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Excepción al sintetizar Live Audio con Gemini: ${e.message}")
        }

        null
    }

    /**
     * Procesa los bytes de audio recibidos (PCM crudo 24kHz o formato contenedor)
     * y genera un archivo WAV temporal con cabecera estándar de 44 bytes para MediaPlayer.
     */
    private fun processAudioBytesToFile(rawBytes: ByteArray, mimeType: String, context: Context): File? {
        try {
            val audioBytes = if (mimeType.contains("pcm") || mimeType.contains("L16") || mimeType.contains("rate=24000")) {
                val sampleRate = extractSampleRate(mimeType, defaultRate = 24000)
                addWavHeaderToPcm(rawBytes, sampleRate = sampleRate, channels = 1)
            } else {
                rawBytes
            }

            // Crear archivo temporal en caché
            val tempFile = File.createTempFile("foni_live_speech_", ".wav", context.cacheDir)
            FileOutputStream(tempFile).use { fos ->
                fos.write(audioBytes)
                fos.flush()
            }
            tempFile.deleteOnExit()
            return tempFile
        } catch (e: Exception) {
            Log.e(TAG, "Error al escribir archivo temporal de audio: ${e.message}", e)
            return null
        }
    }

    /**
     * Extrae la frecuencia de muestreo del MIME type (ej. "audio/L16;codec=pcm;rate=24000")
     */
    private fun extractSampleRate(mimeType: String, defaultRate: Int): Int {
        val rateRegex = Regex("rate=(\\d+)")
        val match = rateRegex.find(mimeType)
        return match?.groupValues?.getOrNull(1)?.toIntOrNull() ?: defaultRate
    }

    /**
     * Construye y antepone la cabecera canónica RIFF/WAVE de 44 bytes para audio PCM lineal de 16 bits.
     */
    private fun addWavHeaderToPcm(pcmData: ByteArray, sampleRate: Int, channels: Int): ByteArray {
        val totalAudioLen = pcmData.size
        val totalDataLen = totalAudioLen + 36
        val bitsPerSample = 16
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = channels * bitsPerSample / 8

        val header = ByteArray(44)
        // ChunkID "RIFF"
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        // ChunkSize
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        // Format "WAVE"
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        // Subchunk1ID "fmt "
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        // Subchunk1Size (16 para PCM)
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        // AudioFormat (1 para PCM lineal sin comprimir)
        header[20] = 1
        header[21] = 0
        // NumChannels
        header[22] = channels.toByte()
        header[23] = 0
        // SampleRate
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        // ByteRate
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        // BlockAlign
        header[32] = blockAlign.toByte()
        header[33] = 0
        // BitsPerSample
        header[34] = bitsPerSample.toByte()
        header[35] = 0
        // Subchunk2ID "data"
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        // Subchunk2Size
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

        val wavBytes = ByteArray(header.size + pcmData.size)
        System.arraycopy(header, 0, wavBytes, 0, header.size)
        System.arraycopy(pcmData, 0, wavBytes, header.size, pcmData.size)
        return wavBytes
    }

    /**
     * Limpia caracteres de formato markdown o emojis antes de pasarlo a Gemini Live Audio
     * para que la dicción sea fluida y natural.
     */
    private fun sanitizeTextForLiveSpeech(text: String): String {
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
}
