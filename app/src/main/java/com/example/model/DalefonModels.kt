package com.example.model

/**
 * Estados del asistente de inteligencia artificial de Dalefon.
 */
enum class DalefonAiState(val label: String, val description: String) {
    IDLE(
        label = "Base",
        description = "En espera de tu solicitud"
    ),
    LISTENING(
        label = "Escuchando",
        description = "Te escucho, dime qué necesitas"
    ),
    PROCESSING(
        label = "Procesando",
        description = "Analizando tu solicitud..."
    ),
    THINKING(
        label = "Pensando",
        description = "Formulando la mejor respuesta..."
    ),
    RESPONDING(
        label = "Respondiendo",
        description = "Dalefon AI te está respondiendo"
    )
}

/**
 * Modo de interacción actual del usuario.
 */
enum class InteractionMode {
    VOICE,  // Experiencia inspirada en Siri (pantalla limpia, voz, avatar central)
    CHAT    // Experiencia de atención escrita (mensajes interactivos, texto)
}

/**
 * Paquetes y servicios móviles de Dalefon en México.
 */
data class TelecomPlan(
    val id: String,
    val name: String,
    val priceMxn: Int,
    val dataGb: String,
    val durationDays: Int,
    val includesUnlimitedSocial: Boolean,
    val includesRoamingUsaCan: Boolean,
    val description: String,
    val isPopular: Boolean = false
)

/**
 * Información de la cuenta del usuario en Dalefon.
 */
data class UserAccount(
    val phoneNumber: String = "55 9823 4410",
    val customerName: String = "Alejandro Morales",
    val currentPlanName: String = "Plan Dale Con Todo 15GB",
    val dataRemainingGb: Float = 11.4f,
    val dataTotalGb: Float = 15.0f,
    val minutesRemaining: String = "Ilimitados (MX/USA/CAN)",
    val smsRemaining: String = "Ilimitados",
    val daysRemaining: Int = 18,
    val balanceMxn: Double = 120.00,
    val simType: String = "eSIM Activa",
    val networkStatus: String = "4.5G LTE Altan Redes"
)

/**
 * Tipo de acción rápida o tarjeta telecom embebida en la conversación.
 */
enum class CardActionType {
    NONE,
    PLANS_CATALOG,
    ESIM_ACTIVATION,
    ACCOUNT_SUMMARY,
    APN_SETTINGS
}

/**
 * Mensaje individual para el historial de chat y transcripción de voz.
 */
data class DalefonMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val cardAction: CardActionType = CardActionType.NONE
)

enum class MessageSender {
    USER,
    AI
}

/**
 * Estado del asistente de activación SIM / eSIM.
 */
data class ESimActivationInfo(
    val iccid: String = "8952 0400 9821 4452 91F",
    val imeiTested: String = "3548 9210 4482 103",
    val isCompatible: Boolean = true,
    val qrCodeUrl: String = "LPA:1\$dalefon.prod.esim.mx\$895204009821",
    val activationStep: Int = 1
)
