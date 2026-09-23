package com.example.ui.screens

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SettingsCell
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.CardActionType
import com.example.model.DalefonMessage
import com.example.model.MessageSender
import com.example.ui.components.DalefonMiniAvatar
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
 * Pantalla de Atención Escrita mediante Chat de Dalefon.
 * Mantiene la identidad tecnológica con mini-avatar con ojos animados y tarjetas de autoservicio.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DalefonChatScreen(
    viewModel: DalefonViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val aiState by viewModel.aiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    var textInput by remember { mutableStateOf("") }

    val quickActionChips = listOf(
        "Ver paquetes y gigas" to { viewModel.showPlansCatalog() },
        "Activar eSIM / SIM" to { viewModel.showEsimWizard() },
        "Conservar mi número (NIP)" to { viewModel.handleUserInput("Quiero conservar mi número con portabilidad") },
        "Configurar APN" to { viewModel.showApnGuide() },
        "Consultar mi saldo" to { viewModel.showAccountSummary() }
    )

    // Auto-scroll al recibir nuevos mensajes
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DalefonDarkBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
            .testTag("dalefon_chat_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. TOP BAR: Avatar animado mini con ojos expresivos + Nombre + Botón a Modo Voz
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DalefonDarkSurface)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Mini avatar con ojos expresivos reactivos
                    DalefonMiniAvatar(
                        state = aiState,
                        sizeDp = 42.dp,
                        modifier = Modifier.size(42.dp)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Foni • Dalefon AI",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DalefonTextWhite
                            )
                            Box(
                                modifier = Modifier
                                    .padding(start = 6.dp)
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(DalefonGreenAccent)
                            )
                        }
                        Text(
                            text = aiState.label,
                            fontSize = 12.sp,
                            color = DalefonPurpleBright
                        )
                    }
                }

                // BOTÓN PARA VOLVER A MODO VOZ SIRI
                Button(
                    onClick = { viewModel.toggleInteractionMode() },
                    colors = ButtonDefaults.buttonColors(containerColor = DalefonDarkSurfaceVariant),
                    shape = RoundedCornerShape(18.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DalefonPurplePrimary),
                    modifier = Modifier.testTag("switch_to_voice_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.RecordVoiceOver,
                        contentDescription = "Modo Voz",
                        tint = DalefonPurpleBright,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Modo Voz",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DalefonTextWhite
                    )
                }
            }

            // 2. LISTA DE MENSAJES Y TARJETAS INTERACTIVAS
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(messages) { message ->
                    ChatMessageItem(
                        message = message,
                        onOpenPlans = { viewModel.showPlansCatalog() },
                        onOpenEsim = { viewModel.showEsimWizard() },
                        onOpenAccount = { viewModel.showAccountSummary() },
                        onOpenApn = { viewModel.showApnGuide() }
                    )
                }
            }

            // 3. PILLS RÁPIDAS DE ACCIÓN TELECOM
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DalefonDarkBg)
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickActionChips) { (label, action) ->
                    Surface(
                        color = DalefonDarkSurfaceVariant,
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DalefonPurpleDark),
                        modifier = Modifier
                            .clickable { action() }
                            .testTag("chat_action_chip_$label")
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = DalefonTextSubtle,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // 4. BARRA DE ENTRADA ESCRITA
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DalefonDarkSurface)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text("Escribe a Dalefon AI...", color = DalefonTextMuted, fontSize = 14.sp)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_text_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DalefonPurpleBright,
                        unfocusedBorderColor = DalefonPurpleDark,
                        focusedTextColor = DalefonTextWhite,
                        unfocusedTextColor = DalefonTextWhite,
                        focusedContainerColor = DalefonDarkBg,
                        unfocusedContainerColor = DalefonDarkBg
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Botón dictado por voz rápido
                IconButton(
                    onClick = {
                        viewModel.startVoiceListening()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DalefonDarkSurfaceVariant)
                        .border(1.dp, DalefonPurpleDark, CircleShape)
                        .testTag("chat_mic_shortcut_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Dictar por voz",
                        tint = DalefonCyanAccent
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Botón Enviar mensaje
                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.handleUserInput(textInput)
                            textInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DalefonPurplePrimary)
                        .testTag("chat_send_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

/**
 * Burbuja individual de mensaje en la conversación de chat con soporte para acciones contextuales.
 */
@Composable
fun ChatMessageItem(
    message: DalefonMessage,
    onOpenPlans: () -> Unit,
    onOpenEsim: () -> Unit,
    onOpenAccount: () -> Unit,
    onOpenApn: () -> Unit
) {
    val isUser = message.sender == MessageSender.USER

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        // Burbuja de texto
        Box(
            modifier = Modifier
                .clip(
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                )
                .background(
                    if (isUser) {
                        Brush.linearGradient(
                            colors = listOf(DalefonPurplePrimary, DalefonPurpleDark)
                        )
                    } else {
                        Brush.linearGradient(
                            colors = listOf(DalefonDarkSurfaceVariant, DalefonDarkSurface)
                        )
                    }
                )
                .border(
                    1.dp,
                    if (isUser) DalefonPurpleBright.copy(alpha = 0.4f) else DalefonPurplePrimary.copy(alpha = 0.25f),
                    RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                )
                .padding(14.dp)
                .testTag(if (isUser) "user_message_bubble" else "ai_message_bubble")
        ) {
            Text(
                text = message.text,
                fontSize = 14.sp,
                color = DalefonTextWhite,
                lineHeight = 20.sp
            )
        }

        // Tarjetas interactivas de telecomunicaciones según la acción
        if (!isUser && message.cardAction != CardActionType.NONE) {
            Spacer(modifier = Modifier.height(8.dp))
            when (message.cardAction) {
                CardActionType.PLANS_CATALOG -> {
                    QuickActionCard(
                        icon = Icons.Default.ShoppingBag,
                        title = "Catálogo de Paquetes Dalefon",
                        subtitle = "Desde $100 MXN con gigas libres y redes sociales",
                        buttonText = "Ver y Contratar Paquetes",
                        onClick = onOpenPlans
                    )
                }
                CardActionType.ESIM_ACTIVATION -> {
                    QuickActionCard(
                        icon = Icons.Default.QrCode,
                        title = "Asistente de Activación eSIM",
                        subtitle = "Obtén tu código QR y activa tu línea en minutos",
                        buttonText = "Abrir Código QR eSIM",
                        onClick = onOpenEsim
                    )
                }
                CardActionType.ACCOUNT_SUMMARY -> {
                    QuickActionCard(
                        icon = Icons.Default.Wallet,
                        title = "Consulta de Saldo y Consumo",
                        subtitle = "Revisa tus gigas restantes, llamadas y vigencia",
                        buttonText = "Ver Detalles de mi Línea",
                        onClick = onOpenAccount
                    )
                }
                CardActionType.APN_SETTINGS -> {
                    QuickActionCard(
                        icon = Icons.Default.SettingsCell,
                        title = "Configuración APN Móvil",
                        subtitle = "internet.dalefon.com para datos celulares",
                        buttonText = "Ver Parámetros APN",
                        onClick = onOpenApn
                    )
                }
                CardActionType.NONE -> {}
            }
        }
    }
}

@Composable
fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("quick_action_card"),
        colors = CardDefaults.cardColors(containerColor = DalefonDarkSurfaceVariant),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DalefonPurplePrimary.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(DalefonPurplePrimary.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = DalefonPurpleBright, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = DalefonTextWhite)
                    Text(text = subtitle, fontSize = 12.sp, color = DalefonTextSubtle)
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = DalefonPurplePrimary),
                shape = RoundedCornerShape(10.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(text = "Ver", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}
