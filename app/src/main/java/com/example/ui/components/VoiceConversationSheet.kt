package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DalefonAiState
import com.example.ui.theme.DalefonCharAquaCyan
import com.example.ui.theme.DalefonCharHotPink
import com.example.ui.theme.DalefonCharLilac
import com.example.ui.theme.DalefonCharLilacBright
import com.example.ui.theme.DalefonDarkBg
import com.example.ui.theme.DalefonDarkSurface
import com.example.ui.theme.DalefonDarkSurfaceVariant
import com.example.ui.theme.DalefonPurpleBright
import com.example.ui.theme.DalefonPurpleDark
import com.example.ui.theme.DalefonPurplePrimary
import com.example.ui.theme.DalefonTextMuted
import com.example.ui.theme.DalefonTextSubtle
import com.example.ui.theme.DalefonTextWhite

/**
 * Modo Live Audio Continuo (estilo Gemini Live) para Foni ("La telefonía morada").
 * Con un solo clic se mantiene una conversación fluida pregunta -> respuesta -> pregunta
 * de forma ininterrumpida hasta que el usuario decida finalizarla.
 * Permite interrupción inmediata (barge-in) mientras Foni está hablando.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceConversationSheet(
    onDismiss: () -> Unit,
    onSendQuery: (String) -> Unit,
    onInterrupt: () -> Unit,
    aiState: DalefonAiState = DalefonAiState.LISTENING,
    currentTranscript: String = "",
    soundLevelRms: Float = 0f,
    isLiveAudioMode: Boolean = true,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var typedQuery by remember { mutableStateOf("") }

    val quickQuestions = listOf(
        "Quiero comprar un paquete de gigas",
        "¿Cómo activo mi eSIM o chip?",
        "Quiero conservar mi número (Portabilidad)",
        "Configurar internet APN",
        "Consultar mi saldo y bolsas",
        "¿Cómo recargo por transferencia CLABE?"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_rings")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(850, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DalefonDarkBg,
        dragHandle = null,
        modifier = modifier
            .imePadding()
            .testTag("voice_conversation_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Barra de Encabezado con Indicador Live y Botón Cerrar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (isLiveAudioMode) Color(0xFFFF3366) else DalefonCharAquaCyan
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isLiveAudioMode) "GEMINI LIVE AUDIO ✨" else "MODO DE VOZ",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = if (isLiveAudioMode) Color(0xFFFF5588) else DalefonCharAquaCyan
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_voice_conversation_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar sesión Live",
                        tint = DalefonTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. Avatar animado de Foni que refleja el estado actual en tiempo real
            DalefonMiniAvatar(
                state = aiState,
                sizeDp = 86.dp,
                modifier = Modifier.size(86.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Estado conversacional descriptivo
            Text(
                text = when (aiState) {
                    DalefonAiState.IDLE -> "Foni en espera"
                    DalefonAiState.LISTENING -> "Te estoy escuchando..."
                    DalefonAiState.PROCESSING -> "Procesando tu consulta..."
                    DalefonAiState.THINKING -> "Generando respuesta con IA..."
                    DalefonAiState.RESPONDING -> "Foni hablando (Gemini Live Audio)"
                },
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = when (aiState) {
                    DalefonAiState.IDLE -> DalefonTextMuted
                    DalefonAiState.LISTENING -> DalefonCharAquaCyan
                    DalefonAiState.PROCESSING -> DalefonCharLilacBright
                    DalefonAiState.THINKING -> DalefonCharLilac
                    DalefonAiState.RESPONDING -> DalefonCharHotPink
                }
            )

            Text(
                text = when (aiState) {
                    DalefonAiState.LISTENING -> "Habla libremente. Al terminar de responderte con voz natural, te volveré a escuchar automáticamente."
                    DalefonAiState.RESPONDING -> "Voz neural activa. Puedes hablar o tocar 'Interrumpir' si tienes otra duda."
                    else -> "Conversación fluida continua de pregunta y respuesta con Gemini Live Audio."
                },
                fontSize = 12.sp,
                color = DalefonTextSubtle,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
            )

            // 3. Visualizador de Ondas de Audio en Vivo
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .height(46.dp)
                    .padding(vertical = 4.dp)
            ) {
                val heights = listOf(14, 28, 42, 22, 36, 18, 32, 20, 10)
                heights.forEachIndexed { index, baseHeight ->
                    val animOffset = ((index % 3) * 0.15f)
                    val dynamicFactor = when (aiState) {
                        DalefonAiState.LISTENING, DalefonAiState.RESPONDING -> (pulseScale + animOffset).coerceIn(0.7f, 1.45f)
                        DalefonAiState.PROCESSING, DalefonAiState.THINKING -> 0.6f
                        DalefonAiState.IDLE -> 0.3f
                    }
                    val barHeight = (baseHeight * dynamicFactor).dp

                    Box(
                        modifier = Modifier
                            .width(5.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(3.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = if (aiState == DalefonAiState.RESPONDING) {
                                        listOf(DalefonCharHotPink, DalefonPurpleBright, DalefonCharAquaCyan)
                                    } else {
                                        listOf(DalefonCharAquaCyan, DalefonPurpleBright, DalefonCharLilac)
                                    }
                                )
                            )
                    )
                }
            }

            // 4. Botón de INTERRUPCIÓN INMEDIATA (Barge-in táctil tipo Gemini Voice)
            AnimatedVisibility(
                visible = aiState == DalefonAiState.RESPONDING,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Button(
                    onClick = onInterrupt,
                    colors = ButtonDefaults.buttonColors(containerColor = DalefonPurplePrimary),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, DalefonCharAquaCyan),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .testTag("interrupt_assistant_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Bolt,
                        contentDescription = null,
                        tint = DalefonCharAquaCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Interrumpir a Foni (Tengo otra duda)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = DalefonTextWhite
                    )
                }
            }

            // 5. Transcripción o previsualización de la conversación
            if (currentTranscript.isNotBlank()) {
                Surface(
                    color = DalefonDarkSurfaceVariant.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DalefonPurpleDark),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Text(
                        text = currentTranscript,
                        fontSize = 13.sp,
                        color = DalefonTextWhite,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 6. Consultas frecuentes de telecomunicaciones
            Text(
                text = "Preguntas rápidas de seguimiento:",
                fontSize = 12.sp,
                color = DalefonTextMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(quickQuestions) { question ->
                    Surface(
                        color = DalefonDarkSurfaceVariant,
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DalefonPurplePrimary),
                        modifier = Modifier
                            .clickable {
                                onSendQuery(question)
                            }
                            .testTag("quick_live_option_${question.take(8)}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = DalefonCharAquaCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = question,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = DalefonTextWhite
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 7. Entrada de texto complementaria para escribir dudas durante la sesión Live
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DalefonDarkSurface, RoundedCornerShape(24.dp))
                    .border(1.dp, DalefonPurpleDark, RoundedCornerShape(24.dp))
                    .padding(horizontal = 12.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = typedQuery,
                    onValueChange = { typedQuery = it },
                    placeholder = {
                        Text("O escribe tu duda a Foni...", color = DalefonTextMuted, fontSize = 13.sp)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("conversation_typed_query_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = DalefonTextWhite,
                        unfocusedTextColor = DalefonTextWhite,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        if (typedQuery.isNotBlank()) {
                            val q = typedQuery
                            typedQuery = ""
                            onSendQuery(q)
                        }
                    },
                    enabled = typedQuery.isNotBlank(),
                    modifier = Modifier.testTag("send_typed_query_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar pregunta",
                        tint = if (typedQuery.isNotBlank()) DalefonCharAquaCyan else DalefonTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 8. BOTÓN PARA CERRAR MICRÓFONO Y FINALIZAR SESIÓN LIVE
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A1124)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF3366)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("finish_live_audio_button")
            ) {
                Icon(
                    imageVector = Icons.Default.MicOff,
                    contentDescription = null,
                    tint = Color(0xFFFF5588),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Finalizar Modo Live (Cerrar micrófono)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}
