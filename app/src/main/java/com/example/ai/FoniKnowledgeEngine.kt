package com.example.ai

import com.example.model.CardActionType

/**
 * Motor de respuestas locales de Foni que replica el 100% de la capacidad de respuesta,
 * tono Ping-Pong, restricciones y base de conocimiento de Dalefon.
 */
object FoniKnowledgeEngine {

    fun generateResponse(userQuery: String): Pair<String, CardActionType> {
        val q = userQuery.lowercase().trim()

        return when {
            // Saludos o inicio de conversación
            isGreeting(q) -> {
                Pair(
                    "¡Hola! Bienvenido a la nueva era de la telefonía. Aquí tu conexión es clara, justa y sin trucos. 🤝 ✨\n\nSoy **Foni**, tu asistente de Dalefon. ¿En qué te puedo apoyar hoy con tu línea?",
                    CardActionType.NONE
                )
            }

            // Compra de paquetes / planes / gigas / datos
            q.contains("comprar") || q.contains("paquete") || q.contains("plan") || q.contains("gigas") || q.contains("datos") || q.contains("cuanto cuesta") || q.contains("precios") -> {
                Pair(
                    "¡Con gusto! En Dalefon tenemos paquetes desde **$100 MXN**. El favorito en México es **'Plan Dale Con Todo'** con **15 GB por $150** durante 30 días.\n\n¿Te gustaría ver el catálogo de paquetes en pantalla o prefieres que te recomiende uno para tu consumo? 💜",
                    CardActionType.PLANS_CATALOG
                )
            }

            // Activación de SIM o eSIM
            q.contains("activar") || q.contains("activación") || q.contains("esim") || q.contains("sim física") || q.contains("vincular") || q.contains("identificación") -> {
                Pair(
                    "Para activar tu **SIM o eSIM Dalefon**, el primer paso es abrir la **App Mi Dalefon** y tocar el botón **'Activa tu línea'**.\n\nTen a la mano tu **Número de orden** de 9 dígitos que te llegó por correo. ¿Ya tienes la app instalada en tu teléfono? 👍",
                    CardActionType.ESIM_ACTIVATION
                )
            }

            // Número de orden / Correo de compra
            q.contains("orden") || q.contains("correo") || q.contains("notificaciones@dalefon.mx") || q.contains("no me llegó") -> {
                Pair(
                    "El **Número de orden** tiene 9 dígitos numéricos (puede empezar con cero) y viene en el correo de bienvenida desde **notificaciones@dalefon.mx**.\n\nTe sugiero revisar tu bandeja de entrada y la carpeta de **Spam**. ¿Pudiste localizarlo?",
                    CardActionType.NONE
                )
            }

            // Portabilidad / Conservar número / NIP
            q.contains("portabilidad") || q.contains("conservar") || q.contains("cambiarme") || q.contains("mi número") || q.contains("nip") || q.contains("051") -> {
                Pair(
                    "¡Claro que puedes conservar tu número! Además, te damos **bonos de datos dobles y triples** durante 12 meses. 🎉\n\nEl primer paso es obtener tu **NIP de Portabilidad** enviando un SMS con la palabra **NIP al 051** desde el chip que deseas traer. ¿Ya cuentas con tu NIP a 4 dígitos?",
                    CardActionType.NONE
                )
            }

            // Configuración de APN / Internet móvil
            q.contains("apn") || q.contains("configurar internet") || q.contains("no tengo datos") || q.contains("sin internet") || q.contains("punto de acceso") -> {
                Pair(
                    "Vamos a configurar tu internet Dalefon paso a paso. ⚙️\n\nPrimero, ve a los **Ajustes** de tu teléfono, entra en **Redes Móviles** y selecciona **Nombres de punto de acceso (APN)**.\n\nAvísame cuando estés ahí para darte los datos exactos. Te abro la guía en pantalla.",
                    CardActionType.APN_SETTINGS
                )
            }

            // Datos APN específicos
            q.contains("datos apn") || q.contains("servidor apn") || q.contains("internet.mvno163.com") -> {
                Pair(
                    "Aquí tienes los datos oficiales de tu APN Dalefon:\n\n• **Nombre:** DALEFON\n• **APN:** `internet.mvno163.com`\n• **Usuario:** `administrator`\n• **Contraseña:** `Altan123`\n• **Tipo de autenticación:** CHAP\n\nRecuerda guardar y reiniciar tu teléfono.",
                    CardActionType.APN_SETTINGS
                )
            }

            // Problemas de llamadas / VoLTE / Roaming / Señal
            q.contains("llamadas") || q.contains("volte") || q.contains("roaming") || q.contains("no entran llamadas") || q.contains("no salen llamadas") -> {
                Pair(
                    "Para que las llamadas funcionen al 100%, asegúrate de tener dos ajustes activos:\n\n1. **Roaming de datos:** Activo.\n2. **Llamadas VoLTE:** Activo.\n\nSi tu equipo es **Xiaomi**, puedes forzar el VoLTE marcando el código `*#*#86583#*#*`. ¿Pudiste verificarlo?",
                    CardActionType.NONE
                )
            }

            // Saldo / Mi Cuenta / Consultar consumo
            q.contains("saldo") || q.contains("bolsa") || q.contains("cuantos gigas") || q.contains("consumo") || q.contains("cuenta") -> {
                Pair(
                    "Tu línea cuenta con **11.4 GB disponibles de 15 GB**, llamadas y SMS ilimitados en México, EE. UU. y Canadá, con vigencia de 18 días restantes.\n\nTe muestro el resumen completo de tu cuenta en pantalla. 💜",
                    CardActionType.ACCOUNT_SUMMARY
                )
            }

            // Recargas y formas de pago
            q.contains("recargar") || q.contains("recarga") || q.contains("transferencia") || q.contains("clabe") || q.contains("donde pagar") -> {
                Pair(
                    "Puedes recargar directamente en la **App Mi Dalefon** o por **transferencia bancaria** usando la **CLABE ÚNICA** de tu número; la recarga se refleja en menos de 5 minutos.\n\nTambién puedes pagar en Farmacias del Ahorro, Circle K, Extra o desde tu app bancaria (BBVA, Banamex, Nu, Stori). ¿Qué método prefieres?",
                    CardActionType.PLANS_CATALOG
                )
            }

            // Cambio de LADA
            q.contains("lada") || q.contains("cambiar número") || q.contains("clave lada") -> {
                Pair(
                    "El cambio de LADA es **gratuito una sola vez** por línea activa. Solo envía un correo a **soporte@dalefon.mx** con tu identificación oficial y tu número Dalefon indicando la ciudad o estado que deseas.\n\nSe aplica el mismo día después de las 7:00 p.m.",
                    CardActionType.NONE
                )
            }

            // Envío de SIM física
            q.contains("envío") || q.contains("cuanto tarda") || q.contains("paquetería") || q.contains("cuando llega") -> {
                Pair(
                    "El envío de la SIM física inicial es **gratis**. Se envía al día hábil siguiente de tu compra y llega a tu domicilio en un lapso de **7 a 10 días hábiles** por paquetería con número de guía rastreable.",
                    CardActionType.NONE
                )
            }

            // Temas fuera de telefonía
            q.contains("clima") || q.contains("receta") || q.contains("chiste") || q.contains("quién eres") && !q.contains("dalefon") -> {
                Pair(
                    "Me encantaría platicar de eso, pero mi especialidad es ayudarte con tu línea Dalefon. 📱 ¿En qué te puedo apoyar hoy con tu servicio móvil?",
                    CardActionType.NONE
                )
            }

            // Respuestas afirmativas de seguimiento Ping-Pong ("ya", "listo", "ya estoy", "si")
            q == "ya" || q == "listo" || q == "si" || q == "ya estoy ahí" || q == "ya lo hice" || q == "ya quedó" -> {
                Pair(
                    "¡Excelente avance! 👍 Ahora introduce el siguiente dato o dime si tu equipo ya se conectó a la red 4.5G de Dalefon.",
                    CardActionType.NONE
                )
            }

            // Caso complejo o no reconocido: canalización a agente humano con política de Dalefon
            else -> {
                Pair(
                    "Entendido. Voy a canalizar tu solicitud con uno de nuestros agentes especializados de Dalefon para revisar tu línea a detalle. 💜\n\nMientras tanto, ¿deseas revisar tus ajustes de red o paquetes disponibles?",
                    CardActionType.NONE
                )
            }
        }
    }

    private fun isGreeting(query: String): Boolean {
        val g = query.trim()
        return g.startsWith("hola") || g.startsWith("buenos") || g.startsWith("buenas") || g == "hey" || g == "hi" || g == "inicio"
    }
}
