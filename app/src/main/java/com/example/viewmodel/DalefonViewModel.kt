package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.FoniGeminiClient
import com.example.ai.FoniKnowledgeEngine
import com.example.model.CardActionType
import com.example.model.DalefonAiState
import com.example.model.DalefonMessage
import com.example.model.InteractionMode
import com.example.model.MessageSender
import com.example.model.TelecomPlan
import com.example.model.UserAccount
import com.example.voice.DalefonSpeechManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DalefonViewModel(application: Application) : AndroidViewModel(application) {

    private val _aiState = MutableStateFlow(DalefonAiState.IDLE)
    val aiState: StateFlow<DalefonAiState> = _aiState.asStateFlow()

    private val _interactionMode = MutableStateFlow(InteractionMode.VOICE)
    val interactionMode: StateFlow<InteractionMode> = _interactionMode.asStateFlow()

    private val _currentVoiceTranscript = MutableStateFlow(
        "Bienvenido a la nueva era de la Telefonía. Soy Foni, ¿en qué te puedo apoyar hoy con tu línea Dalefon? 🤝 ✨"
    )
    val currentVoiceTranscript: StateFlow<String> = _currentVoiceTranscript.asStateFlow()

    private val _userSpokenText = MutableStateFlow("")
    val userSpokenText: StateFlow<String> = _userSpokenText.asStateFlow()

    private val _userAccount = MutableStateFlow(UserAccount())
    val userAccount: StateFlow<UserAccount> = _userAccount.asStateFlow()

    private val _plans = MutableStateFlow(
        listOf(
            TelecomPlan(
                id = "dale_150",
                name = "Dale 150 (Promo Portabilidad)",
                priceMxn = 150,
                dataGb = "15 GB (10GB + 5GB Bono)",
                durationDays = 15,
                includesUnlimitedSocial = true,
                includesRoamingUsaCan = true,
                description = "10 GB de recarga + 5 GB de bono a velocidad máxima durante 15 días, llamadas y SMS ilimitados.",
                isPopular = true
            ),
            TelecomPlan(
                id = "dale_220",
                name = "Dale 220 (Triple de Datos)",
                priceMxn = 220,
                dataGb = "36 GB (12GB + 24GB Bono)",
                durationDays = 30,
                includesUnlimitedSocial = true,
                includesRoamingUsaCan = true,
                description = "12 GB de recarga + 24 GB de bono promocional durante 30 días para navegación libre nacional.",
                isPopular = false
            ),
            TelecomPlan(
                id = "dale_120",
                name = "Dale 120 (Doble de Datos)",
                priceMxn = 120,
                dataGb = "4 GB (2GB + 2GB Bono)",
                durationDays = 30,
                includesUnlimitedSocial = true,
                includesRoamingUsaCan = true,
                description = "2 GB de recarga + 2 GB de bono durante 30 días en red 4.5G Altan con llamadas y SMS ilimitados.",
                isPopular = false
            ),
            TelecomPlan(
                id = "dale_180",
                name = "Dale 180 (Triple de Datos)",
                priceMxn = 180,
                dataGb = "12 GB (4GB + 8GB Bono)",
                durationDays = 30,
                includesUnlimitedSocial = true,
                includesRoamingUsaCan = true,
                description = "4 GB de recarga + 8 GB de bono a máxima velocidad durante 30 días con minutos y mensajes sin costo.",
                isPopular = false
            ),
            TelecomPlan(
                id = "dale_350",
                name = "Dale 350 (Doble de Datos)",
                priceMxn = 350,
                dataGb = "70 GB (35GB + 35GB Bono)",
                durationDays = 30,
                includesUnlimitedSocial = true,
                includesRoamingUsaCan = true,
                description = "35 GB de recarga + 35 GB de bono promocional durante 30 días para alto consumo y hotspot.",
                isPopular = false
            ),
            TelecomPlan(
                id = "dale_550",
                name = "Dale 550 (Máxima Capacidad)",
                priceMxn = 550,
                dataGb = "100 GB (50GB + 50GB Bono)",
                durationDays = 30,
                includesUnlimitedSocial = true,
                includesRoamingUsaCan = true,
                description = "50 GB de recarga + 50 GB de bono mensual durante 30 días para máxima productividad.",
                isPopular = false
            )
        )
    )
    val plans: StateFlow<List<TelecomPlan>> = _plans.asStateFlow()

    private val _messages = MutableStateFlow<List<DalefonMessage>>(
        listOf(
            DalefonMessage(
                sender = MessageSender.AI,
                text = "Bienvenido a la nueva era de la Telefonía. Olvídate de cargos sorpresa, contratos eternos y datos que desaparecen. Aquí tu conexión es clara, justa y sin trucos. 🤝 ✨\n\nSoy **Foni**, tu asistente virtual oficial de Dalefon. ¿En qué te puedo apoyar hoy con tu línea?"
            )
        )
    )
    val messages: StateFlow<List<DalefonMessage>> = _messages.asStateFlow()


    // Modals / Sheets
    private val _showPlansSheet = MutableStateFlow(false)
    val showPlansSheet: StateFlow<Boolean> = _showPlansSheet.asStateFlow()

    private val _showEsimSheet = MutableStateFlow(false)
    val showEsimSheet: StateFlow<Boolean> = _showEsimSheet.asStateFlow()

    private val _showAccountSheet = MutableStateFlow(false)
    val showAccountSheet: StateFlow<Boolean> = _showAccountSheet.asStateFlow()

    private val _showApnSheet = MutableStateFlow(false)
    val showApnSheet: StateFlow<Boolean> = _showApnSheet.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isVoiceConversationActive = MutableStateFlow(false)
    val isVoiceConversationActive: StateFlow<Boolean> = _isVoiceConversationActive.asStateFlow()

    private val _isLiveAudioMode = MutableStateFlow(false)
    val isLiveAudioMode: StateFlow<Boolean> = _isLiveAudioMode.asStateFlow()

    private val _soundLevelRms = MutableStateFlow(0f)
    val soundLevelRms: StateFlow<Float> = _soundLevelRms.asStateFlow()

    private var processJob: Job? = null

    // Speech manager
    private lateinit var speechManager: DalefonSpeechManager

    init {
        speechManager = DalefonSpeechManager(
            context = application.applicationContext,
            onSpeechStarted = {
                _aiState.value = DalefonAiState.RESPONDING
            },
            onSpeechEnded = {
                if (_aiState.value == DalefonAiState.RESPONDING) {
                    if (_isLiveAudioMode.value) {
                        // LIVE AUDIO CONTINUO: Al terminar de responder Foni, se reactiva la escucha automáticamente
                        _aiState.value = DalefonAiState.LISTENING
                        _currentVoiceTranscript.value = "Te sigo escuchando... Haz otra pregunta o dime qué necesitas."
                        speechManager.startListening()
                    } else {
                        _aiState.value = DalefonAiState.IDLE
                    }
                }
            },
            onVoiceInputResult = { recognizedText ->
                handleUserInput(recognizedText)
            },
            onPartialResult = { partial ->
                _userSpokenText.value = partial
            },
            onRmsChanged = { rms ->
                _soundLevelRms.value = rms
            },
            onRecognitionError = { _ ->
                // En modo Live Audio continuo, si hubo silencio o timeout temporal, rearmamos la escucha
                if (_isLiveAudioMode.value && _aiState.value == DalefonAiState.LISTENING) {
                    viewModelScope.launch {
                        delay(1000)
                        if (_isLiveAudioMode.value && _aiState.value == DalefonAiState.LISTENING) {
                            speechManager.startListening()
                        }
                    }
                }
            },
            onBargeIn = {
                // Interrupción inmediata detectada por voz (Barge-in acústico)
                if (_isLiveAudioMode.value) {
                    interruptAssistant()
                }
            }
        )
    }

    fun toggleInteractionMode() {
        if (_interactionMode.value == InteractionMode.VOICE) {
            _interactionMode.value = InteractionMode.CHAT
        } else {
            _interactionMode.value = InteractionMode.VOICE
        }
    }

    fun setInteractionMode(mode: InteractionMode) {
        _interactionMode.value = mode
    }

    fun toggleMute() {
        val next = !_isMuted.value
        _isMuted.value = next
        speechManager.setMuted(next)
    }

    /**
     * Inicia el Modo Live Audio continuo con un solo clic.
     * La conversación se mantiene en vivo (pregunta -> respuesta -> pregunta)
     * hasta que el usuario decida cerrarla con el botón de finalizar.
     */
    fun startVoiceListening() {
        processJob?.cancel()
        speechManager.stopSpeaking(notifyEnded = false)
        _interactionMode.value = InteractionMode.VOICE
        _isLiveAudioMode.value = true
        _isVoiceConversationActive.value = true
        _aiState.value = DalefonAiState.LISTENING
        _userSpokenText.value = ""
        _currentVoiceTranscript.value = "Modo Live Audio iniciado. Te escucho... Pregúntame lo que necesites."
        speechManager.startListening()
    }

    /**
     * Finaliza la sesión Live Audio cuando el usuario pulsa el botón de cerrar micrófono.
     */
    fun stopVoiceListening() {
        stopLiveAudioMode()
    }

    fun stopLiveAudioMode() {
        _isLiveAudioMode.value = false
        _isVoiceConversationActive.value = false
        processJob?.cancel()
        speechManager.stopListening()
        speechManager.stopSpeaking(notifyEnded = false)
        _aiState.value = DalefonAiState.IDLE
        _currentVoiceTranscript.value = "Conversación finalizada. Toca el micrófono para iniciar una nueva sesión Live."
    }

    /**
     * Interrumpe la respuesta del asistente de inmediato si el usuario tiene otra duda o interrumpe.
     */
    fun interruptAssistant() {
        processJob?.cancel()
        speechManager.stopSpeaking(notifyEnded = false)
        _aiState.value = DalefonAiState.LISTENING
        _currentVoiceTranscript.value = "Te escucho, dime tu duda..."
        speechManager.startListening()
    }

    fun submitVoiceQuery(query: String) {
        handleUserInput(query)
    }

    fun handleUserInput(input: String) {
        if (input.isBlank()) return

        processJob?.cancel()
        _userSpokenText.value = input

        // Agregar al historial de chat
        val userMsg = DalefonMessage(
            sender = MessageSender.USER,
            text = input
        )
        _messages.value = _messages.value + userMsg

        // Estado procesando: ojos en ranura horizontal y anillo de puntos
        _aiState.value = DalefonAiState.PROCESSING

        processJob = viewModelScope.launch {
            // Fase de procesamiento inicial
            delay(750)

            // Fase de pensamiento: ojos curiosos mirando arriba a la derecha y puntos de pensamiento
            _aiState.value = DalefonAiState.THINKING
            delay(850)

            // Obtener respuesta desde Foni (Gemini con Base de Conocimiento oficial o Motor Local de Foni)
            val (responseText, cardAction) = FoniGeminiClient.getResponse(input)

            _currentVoiceTranscript.value = responseText

            val aiMsg = DalefonMessage(
                sender = MessageSender.AI,
                text = responseText,
                cardAction = cardAction
            )
            _messages.value = _messages.value + aiMsg

            // Si hay tarjeta vinculada, podemos abrir la hoja correspondiente si el usuario lo pidió
            when (cardAction) {
                CardActionType.PLANS_CATALOG -> _showPlansSheet.value = true
                CardActionType.ESIM_ACTIVATION -> _showEsimSheet.value = true
                CardActionType.ACCOUNT_SUMMARY -> _showAccountSheet.value = true
                CardActionType.APN_SETTINGS -> _showApnSheet.value = true
                CardActionType.NONE -> {}
            }

            // Reproducir audio con TextToSpeech con la voz y tono de Foni (activa estado RESPONDIENDO)
            speechManager.speak(responseText)
        }
    }

    fun setAiState(state: DalefonAiState) {
        processJob?.cancel()
        _aiState.value = state
        when (state) {
            DalefonAiState.IDLE -> {
                _currentVoiceTranscript.value = "Bienvenido a la nueva era de la Telefonía. Soy Foni, ¿en qué te puedo apoyar hoy con tu línea Dalefon? 🤝 ✨"
            }
            DalefonAiState.LISTENING -> {
                _currentVoiceTranscript.value = "Te escucho atentamente..."
            }
            DalefonAiState.PROCESSING -> {
                _currentVoiceTranscript.value = "Procesando tu solicitud en la red Dalefon..."
            }
            DalefonAiState.THINKING -> {
                _currentVoiceTranscript.value = "Consultando las mejores opciones para ti..."
            }
            DalefonAiState.RESPONDING -> {
                _currentVoiceTranscript.value = "¡Tu línea Dalefon cuenta con cobertura 4.5G en todo México!"
            }
        }
    }


    private fun generateTelecomResponse(query: String): Pair<String, CardActionType> {
        return FoniKnowledgeEngine.generateResponse(query)
    }


    fun onPlanPurchased(plan: TelecomPlan) {
        val currentAcc = _userAccount.value
        val addedGb = when (plan.id) {
            "plan_15gb" -> 15.0f
            "plan_ilimitado" -> 40.0f
            "plan_redes" -> 5.0f
            else -> 3.0f
        }
        _userAccount.value = currentAcc.copy(
            currentPlanName = plan.name,
            dataRemainingGb = currentAcc.dataRemainingGb + addedGb,
            dataTotalGb = currentAcc.dataTotalGb + addedGb,
            daysRemaining = plan.durationDays
        )

        val confirmText = "¡Excelente! Has contratado ${plan.name}. Tus ${plan.dataGb} ya han sido agregados a tu línea Dalefon."
        _currentVoiceTranscript.value = confirmText
        _messages.value = _messages.value + DalefonMessage(
            sender = MessageSender.AI,
            text = confirmText
        )
        speechManager.speak(confirmText)
    }

    fun onSimActivated(details: String) {
        val currentAcc = _userAccount.value
        _userAccount.value = currentAcc.copy(
            simType = details
        )
        val confirmText = "¡Felicidades! Tu $details ha quedado activada exitosamente con cobertura 4.5G de Dalefon en México."
        _currentVoiceTranscript.value = confirmText
        _messages.value = _messages.value + DalefonMessage(
            sender = MessageSender.AI,
            text = confirmText
        )
        speechManager.speak(confirmText)
    }

    fun dismissPlansSheet() { _showPlansSheet.value = false }
    fun showPlansCatalog() { _showPlansSheet.value = true }

    fun dismissEsimSheet() { _showEsimSheet.value = false }
    fun showEsimWizard() { _showEsimSheet.value = true }

    fun dismissAccountSheet() { _showAccountSheet.value = false }
    fun showAccountSummary() { _showAccountSheet.value = true }

    fun dismissApnSheet() { _showApnSheet.value = false }
    fun showApnGuide() { _showApnSheet.value = true }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
    }
}
