package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.DalefonAiState
import com.example.ui.components.DalefonAvatar
import com.example.ui.components.DalefonLogoMark
import com.example.ui.components.VoiceConversationSheet
import com.example.ui.theme.DalefonCharAquaCyan
import com.example.ui.theme.DalefonCharHotPink
import com.example.ui.theme.DalefonCharLilac
import com.example.ui.theme.DalefonCharLilacBright
import com.example.ui.theme.DalefonCharNeonPink
import com.example.ui.theme.DalefonCircuitDot
import com.example.ui.theme.DalefonCircuitLine
import com.example.ui.theme.DalefonCyanAccent
import com.example.ui.theme.DalefonDarkBg
import com.example.ui.theme.DalefonDarkSurface
import com.example.ui.theme.DalefonDarkSurfaceVariant
import com.example.ui.theme.DalefonGreenAccent
import com.example.ui.theme.DalefonPurpleBright
import com.example.ui.theme.DalefonPurpleDark
import com.example.ui.theme.DalefonPurpleLight
import com.example.ui.theme.DalefonPurpleNeon
import com.example.ui.theme.DalefonPurplePrimary
import com.example.ui.theme.DalefonTextMuted
import com.example.ui.theme.DalefonTextSubtle
import com.example.ui.theme.DalefonTextWhite
import com.example.viewmodel.DalefonViewModel

/**
 * Pantalla principal de Dalefon inspirada en Siri con la identidad visual oficial de Dalefon AI:
 * - Avatar 3D oficial con ojos expresivos que cambian entre BASE, ESCUCHANDO, PROCESANDO, PENSANDO y RESPONDIENDO.
 * - Trazos de circuitos tecnológicos en el fondo.
 * - Barra interactiva para probar/inspeccionar cada estado de la IA.
 * - Botón claramente visible para cambiar a atención escrita por chat.
 */
@Composable
fun DalefonVoiceScreen(
    viewModel: DalefonViewModel,
    modifier: Modifier = Modifier
) {
    val aiState by viewModel.aiState.collectAsStateWithLifecycle()
    val transcript by viewModel.currentVoiceTranscript.collectAsStateWithLifecycle()
    val userSpoken by viewModel.userSpokenText.collectAsStateWithLifecycle()
    val isMuted by viewModel.isMuted.collectAsStateWithLifecycle()
    val isVoiceConversationActive by viewModel.isVoiceConversationActive.collectAsStateWithLifecycle()
    val isLiveAudioMode by viewModel.isLiveAudioMode.collectAsStateWithLifecycle()
    val soundLevelRms by viewModel.soundLevelRms.collectAsStateWithLifecycle()

    val quickVoiceSuggestions = listOf(
        "Quiero comprar un paquete de datos",
        "¿Cómo activo mi eSIM o chip?",
        "Quiero conservar mi número (Portabilidad)",
        "Configurar internet APN",
        "Consultar mi saldo y bolsas"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        DalefonDarkBg,
                        Color(0xFF130926),
                        DalefonDarkBg
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("dalefon_voice_screen")
    ) {
        // Trazos de circuitos tecnológicos de fondo (como en el diseño oficial de Dalefon AI)
        CircuitBackgroundCanvas(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. BARRA SUPERIOR: Insignia de logotipo Dalefon 'd', Red 4.5G Altan y Botón a Chat Escrito
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Logotipo oficial Dalefon 'd' + Red Altan OMV
                Row(verticalAlignment = Alignment.CenterVertically) {
                    DalefonLogoMark(sizeDp = 38.dp)

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DALEFON",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp,
                                color = DalefonTextWhite
                            )
                            Text(
                                text = " • FONI",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = DalefonCharAquaCyan
                            )
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(DalefonGreenAccent)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "4.5G LTE • Altan MX",
                                fontSize = 11.sp,
                                color = DalefonTextSubtle
                            )
                        }
                    }
                }

                // BOTÓN CLARAMENTE VISIBLE PARA ATENCIÓN ESCRITA MEDIANTE CHAT
                Button(
                    onClick = { viewModel.toggleInteractionMode() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DalefonDarkSurfaceVariant
                    ),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, DalefonPurplePrimary),
                    modifier = Modifier
                        .height(40.dp)
                        .testTag("switch_to_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Chat",
                        tint = DalefonPurpleBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Modo Chat",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DalefonTextWhite
                    )
                }
            }

            // 2. SECCIÓN CENTRAL: AVATAR DALEFON AI OFICIAL CON OJOS EXPRESIVOS + ESTADOS
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(1f)
            ) {
                // AVATAR DALEFON AI 3D CON OJOS EXPRESIVOS SEGÚN EL ESTADO
                DalefonAvatar(
                    state = aiState,
                    sizeDp = 200.dp,
                    showWaveform = true,
                    modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
                )

                // TEXTO DE IDENTIDAD COMO EN EL DISEÑO: "BASE DALEFON AI®"
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Text(
                        text = when (aiState) {
                            DalefonAiState.IDLE -> if (isLiveAudioMode) "LIVE AUDIO LISTO" else "BASE"
                            DalefonAiState.LISTENING -> if (isLiveAudioMode) "LIVE AUDIO • ESCUCHANDO" else "ESCUCHANDO"
                            DalefonAiState.PROCESSING -> "PROCESANDO"
                            DalefonAiState.THINKING -> "PENSANDO CON IA"
                            DalefonAiState.RESPONDING -> "GEMINI LIVE AUDIO • HABLANDO"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        color = when (aiState) {
                            DalefonAiState.IDLE -> DalefonTextMuted
                            DalefonAiState.LISTENING -> DalefonCharAquaCyan
                            DalefonAiState.PROCESSING -> DalefonCharLilacBright
                            DalefonAiState.THINKING -> DalefonCharLilac
                            DalefonAiState.RESPONDING -> DalefonCharHotPink
                        }
                    )
                    Text(
                        text = "FONI • DALEFON AI®",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 2.sp,
                        color = DalefonTextWhite
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // TRANSCRIPCIÓN MINIMALISTA TIPO SIRI
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (userSpoken.isNotBlank()) {
                        Text(
                            text = "\"$userSpoken\"",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = DalefonPurpleLight,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }

                    Text(
                        text = transcript,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DalefonTextWhite,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )

                    // Botón para interrumpir a Foni mientras habla
                    if (aiState == DalefonAiState.RESPONDING) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.interruptAssistant() },
                            colors = ButtonDefaults.buttonColors(containerColor = DalefonPurplePrimary),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DalefonCharAquaCyan),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("voice_screen_interrupt_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = DalefonCharAquaCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Interrumpir (Tengo otra duda)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // 3. PILLS DE SUGERENCIAS RÁPIDAS DE TELECOMUNICACIONES
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Prueba diciendo o tocando:",
                    fontSize = 11.sp,
                    color = DalefonTextMuted,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickVoiceSuggestions) { suggestion ->
                        Surface(
                            color = DalefonDarkSurface.copy(alpha = 0.85f),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DalefonPurpleDark),
                            modifier = Modifier
                                .clickable {
                                    viewModel.handleUserInput(suggestion)
                                }
                                .testTag("voice_suggestion_${suggestion.take(10)}")
                        ) {
                            Text(
                                text = suggestion,
                                fontSize = 12.sp,
                                color = DalefonTextSubtle,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }

            // 4. BARRA INFERIOR DE VOZ (Botón micrófono reactivo, silenciador y chat)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón silenciar / desmutear audio
                IconButton(
                    onClick = { viewModel.toggleMute() },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(DalefonDarkSurface)
                        .border(1.dp, DalefonPurpleDark, CircleShape)
                        .testTag("mute_audio_button")
                ) {
                    Icon(
                        imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = if (isMuted) "Silenciado" else "Con audio",
                        tint = if (isMuted) DalefonTextMuted else DalefonCharAquaCyan
                    )
                }

                // BOTÓN PRINCIPAL DE MICRÓFONO LIVE AUDIO
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(76.dp)
                ) {
                    if (isLiveAudioMode || aiState == DalefonAiState.LISTENING) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isLiveAudioMode) Color(0xFFFF3366).copy(alpha = 0.35f)
                                    else DalefonCharAquaCyan.copy(alpha = 0.30f)
                                )
                        )
                    }

                    Surface(
                        onClick = {
                            if (isLiveAudioMode) {
                                viewModel.stopLiveAudioMode()
                            } else {
                                viewModel.startVoiceListening()
                            }
                        },
                        shape = CircleShape,
                        color = Color.Transparent,
                        modifier = Modifier
                            .size(68.dp)
                            .testTag("main_mic_button")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = if (isLiveAudioMode) {
                                            listOf(Color(0xFFFF3366), DalefonCharHotPink)
                                        } else if (aiState == DalefonAiState.LISTENING) {
                                            listOf(DalefonCharAquaCyan, DalefonPurpleBright)
                                        } else {
                                            listOf(DalefonPurplePrimary, DalefonPurpleDark)
                                        }
                                    ),
                                    shape = CircleShape
                                )
                                .border(
                                    2.dp,
                                    if (isLiveAudioMode) Color(0xFFFF5588) else DalefonPurpleBright.copy(alpha = 0.85f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isLiveAudioMode) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = if (isLiveAudioMode) "Finalizar Modo Live (Cerrar micrófono)" else "Iniciar Modo Live Audio",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                // Botón rápido para abrir chat escrito
                IconButton(
                    onClick = { viewModel.toggleInteractionMode() },
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(DalefonDarkSurface)
                        .border(1.dp, DalefonPurpleDark, CircleShape)
                        .testTag("quick_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = "Chat escrito",
                        tint = DalefonPurpleBright
                    )
                }
            }
        }

        // Modo Live Audio Continuo (ModalBottomSheet estilo Gemini Live / Siri)
        if (isVoiceConversationActive) {
            VoiceConversationSheet(
                onDismiss = { viewModel.stopLiveAudioMode() },
                onSendQuery = { query -> viewModel.submitVoiceQuery(query) },
                onInterrupt = { viewModel.interruptAssistant() },
                aiState = aiState,
                currentTranscript = transcript,
                soundLevelRms = soundLevelRms,
                isLiveAudioMode = isLiveAudioMode
            )
        }
    }
}

/**
 * Traza líneas de circuitos tecnológicos en el fondo similares a la lámina de diseño de Dalefon AI.
 */
@Composable
fun CircuitBackgroundCanvas(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val strokeColor = DalefonCircuitLine
        val dotColor = DalefonCircuitDot

        // Línea de circuito horizontal 1 (costado izquierdo hacia el centro)
        val path1 = Path().apply {
            moveTo(0f, h * 0.22f)
            lineTo(w * 0.18f, h * 0.22f)
            lineTo(w * 0.25f, h * 0.28f)
            lineTo(w * 0.32f, h * 0.28f)
        }
        drawPath(path1, strokeColor, style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round))
        drawCircle(dotColor, radius = 3.dp.toPx(), center = Offset(w * 0.32f, h * 0.28f))

        // Línea de circuito horizontal 2 (costado derecho hacia el centro)
        val path2 = Path().apply {
            moveTo(w, h * 0.26f)
            lineTo(w * 0.82f, h * 0.26f)
            lineTo(w * 0.74f, h * 0.33f)
            lineTo(w * 0.68f, h * 0.33f)
        }
        drawPath(path2, strokeColor, style = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round))
        drawCircle(dotColor, radius = 3.dp.toPx(), center = Offset(w * 0.68f, h * 0.33f))

        // Puntos conectores en la parte superior derecha
        drawCircle(dotColor.copy(alpha = 0.6f), radius = 2.dp.toPx(), center = Offset(w * 0.76f, h * 0.05f))
        drawCircle(dotColor.copy(alpha = 0.6f), radius = 2.dp.toPx(), center = Offset(w * 0.80f, h * 0.05f))
    }
}
