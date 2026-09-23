package com.example.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SimCard
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TelecomPlan
import com.example.model.UserAccount
import com.example.ui.theme.DalefonCardBg
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

/**
 * Tarjeta interactiva embebida en la conversación para compra de paquetes Dalefon.
 */
@Composable
fun PlanPreviewCard(
    plan: TelecomPlan,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("plan_card_${plan.id}"),
        colors = CardDefaults.cardColors(
            containerColor = DalefonDarkSurfaceVariant.copy(alpha = 0.85f)
        ),
        shape = RoundedCornerShape(18.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (plan.isPopular) DalefonPurpleBright.copy(alpha = 0.6f) else DalefonPurplePrimary.copy(alpha = 0.25f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (plan.isPopular) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DalefonPurplePrimary)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stars,
                        contentDescription = "Popular",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "MÁS ELEGIDO EN MÉXICO",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = plan.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = DalefonTextWhite
                    )
                    Text(
                        text = "${plan.dataGb} libres • ${plan.durationDays} días",
                        fontSize = 13.sp,
                        color = DalefonPurpleLight
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${plan.priceMxn} MXN",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = DalefonCyanAccent
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = plan.description,
                fontSize = 13.sp,
                color = DalefonTextSubtle,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onSelect,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .testTag("buy_plan_btn_${plan.id}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DalefonPurplePrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Contratar este paquete", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

/**
 * Modal Bottom Sheet para explorar y comprar todos los paquetes Dalefon.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlansCatalogSheet(
    plans: List<TelecomPlan>,
    onDismiss: () -> Unit,
    onPlanPurchased: (TelecomPlan) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var successPlan by remember { mutableStateOf<TelecomPlan?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DalefonDarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .testTag("plans_catalog_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Paquetes Móviles Dalefon",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DalefonTextWhite
                    )
                    Text(
                        text = "Red 4.5G de máxima cobertura en todo México",
                        fontSize = 13.sp,
                        color = DalefonTextSubtle
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = DalefonTextSubtle)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (successPlan != null) {
                // Confirmación de compra exitosa
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DalefonDarkSurfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Éxito",
                            tint = DalefonGreenAccent,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "¡Paquete Activado con Éxito!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DalefonTextWhite
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Has adquirido ${successPlan?.name} por $${successPlan?.priceMxn} MXN. Tus datos y llamadas ya están disponibles.",
                            fontSize = 14.sp,
                            color = DalefonTextSubtle,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = DalefonPurplePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Aceptar y Volver", color = Color.White)
                        }
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(plans) { plan ->
                        PlanPreviewCard(
                            plan = plan,
                            onSelect = {
                                successPlan = plan
                                onPlanPurchased(plan)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modal Bottom Sheet para Activación de SIM y eSIM guiada por la IA.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ESimActivationSheet(
    onDismiss: () -> Unit,
    onActivationComplete: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedSimType by remember { mutableIntStateOf(0) } // 0: eSIM, 1: SIM Física
    var step by remember { mutableIntStateOf(1) }
    var isVerifyingImei by remember { mutableStateOf(false) }
    var isDone by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DalefonDarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .testTag("esim_activation_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Activación SIM & eSIM Dalefon",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DalefonTextWhite
                    )
                    Text(
                        text = "Guía guiada por IA en 3 sencillos pasos",
                        fontSize = 13.sp,
                        color = DalefonTextSubtle
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = DalefonTextSubtle)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Selector eSIM digital vs SIM física
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(DalefonDarkSurfaceVariant)
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedSimType == 0) DalefonPurplePrimary else Color.Transparent)
                        .clickable { selectedSimType = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "eSIM Digital (Recomendada)",
                        fontSize = 13.sp,
                        fontWeight = if (selectedSimType == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedSimType == 0) Color.White else DalefonTextSubtle
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedSimType == 1) DalefonPurplePrimary else Color.Transparent)
                        .clickable { selectedSimType = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "SIM Física Dalefon",
                        fontSize = 13.sp,
                        fontWeight = if (selectedSimType == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedSimType == 1) Color.White else DalefonTextSubtle
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (isDone) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DalefonDarkSurfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Éxito",
                            tint = DalefonGreenAccent,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "¡Línea Dalefon Activada!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DalefonTextWhite
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tu número mexicano es +52 55 9823 4410 con perfil eSIM instalado en la red Altan 4.5G.",
                            fontSize = 14.sp,
                            color = DalefonTextSubtle,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onDismiss,
                            colors = ButtonDefaults.buttonColors(containerColor = DalefonPurplePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Listo", color = Color.White)
                        }
                    }
                }
            } else if (selectedSimType == 0) {
                // Pasos de activación de eSIM
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DalefonDarkSurfaceVariant),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DalefonPurplePrimary.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.QrCode, contentDescription = null, tint = DalefonCyanAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Código QR de Instalación eSIM",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DalefonTextWhite
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Ve a Configuración > Red Celular > Agregar eSIM y escanea este código QR o ingresa la clave manual.",
                            fontSize = 13.sp,
                            color = DalefonTextSubtle
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Simulación visual del código QR Dalefon
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .align(Alignment.CenterHorizontally)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "QR eSIM",
                                tint = DalefonDarkBg,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Clave manual de activación
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                            .background(DalefonDarkBg)
                            .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Clave de Activación Manual (LPA)",
                                    fontSize = 11.sp,
                                    color = DalefonTextMuted
                                )
                                Text(
                                    text = "LPA:1\$dalefon.prod.mx\$89520400",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DalefonTextWhite
                                )
                            }
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString("LPA:1\$dalefon.prod.mx\$89520400"))
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copiar", tint = DalefonPurpleBright)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                isDone = true
                                onActivationComplete("eSIM Activada")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = DalefonPurplePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Confirmar y Activar eSIM", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            } else {
                // Pasos de activación de SIM Física
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DalefonDarkSurfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SimCard, contentDescription = null, tint = DalefonPurpleBright)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Activar Chip Físico Dalefon",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = DalefonTextWhite
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Introduce el código ICCID de 19 dígitos impreso al reverso de tu tarjeta plástica Dalefon.",
                            fontSize = 13.sp,
                            color = DalefonTextSubtle
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DalefonDarkBg)
                                .padding(14.dp)
                        ) {
                            Text(
                                text = "8952 0400 9821 4452 91F",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = DalefonCyanAccent
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = {
                                isDone = true
                                onActivationComplete("SIM Física Activada")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = DalefonPurplePrimary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Activar Chip Dalefon", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Modal Bottom Sheet para Acceso y Consulta de Cuenta y Saldo en tiempo real.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSummarySheet(
    account: UserAccount,
    onDismiss: () -> Unit,
    onRechargeClick: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DalefonDarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .testTag("account_summary_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Mi Cuenta Dalefon",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DalefonTextWhite
                    )
                    Text(
                        text = "Línea: +52 ${account.phoneNumber}",
                        fontSize = 14.sp,
                        color = DalefonPurpleLight
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = DalefonTextSubtle)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta de consumo de datos y saldo
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DalefonDarkSurfaceVariant),
                shape = RoundedCornerShape(18.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DalefonPurplePrimary.copy(alpha = 0.35f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Datos de Alta Velocidad",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DalefonTextWhite
                        )
                        Text(
                            text = "${account.dataRemainingGb} GB / ${account.dataTotalGb} GB",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = DalefonCyanAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val progress = (account.dataRemainingGb / account.dataTotalGb).coerceIn(0f, 1f)
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = DalefonPurpleBright,
                        trackColor = DalefonDarkBg
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Llamadas y SMS", fontSize = 12.sp, color = DalefonTextMuted)
                            Text(text = account.minutesRemaining, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DalefonTextWhite)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Vigencia", fontSize = 12.sp, color = DalefonTextMuted)
                            Text(text = "${account.daysRemaining} días restantes", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DalefonGreenAccent)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DalefonDarkBg)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SignalCellularAlt, contentDescription = null, tint = DalefonGreenAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = account.networkStatus, fontSize = 12.sp, color = DalefonTextSubtle)
                        }
                        Text(text = account.simType, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DalefonPurpleLight)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onRechargeClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("recharge_account_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = DalefonPurplePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Recargar Saldo o Cambiar Paquete", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

/**
 * Modal Bottom Sheet para Guía de Configuración APN Dalefon.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApnSettingsSheet(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val clipboardManager = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DalefonDarkSurface,
        scrimColor = Color.Black.copy(alpha = 0.65f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .testTag("apn_settings_sheet")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Configuración APN Dalefon",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = DalefonTextWhite
                    )
                    Text(
                        text = "Para activar internet y datos móviles en México",
                        fontSize = 13.sp,
                        color = DalefonTextSubtle
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = DalefonTextSubtle)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DalefonDarkSurfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Parámetros de Red Móvil:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DalefonTextWhite
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    ApnFieldRow(label = "Nombre de conexión", value = "Dalefon Internet")
                    ApnFieldRow(label = "APN (Punto de acceso)", value = "internet.dalefon.com")
                    ApnFieldRow(label = "Tipo de APN", value = "default,supl")
                    ApnFieldRow(label = "MCC", value = "334 (México)")
                    ApnFieldRow(label = "MNC", value = "140 (Red Altan)")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    clipboardManager.setText(AnnotatedString("internet.dalefon.com"))
                    copied = true
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = DalefonPurplePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (copied) "¡APN Copiado al Portapapeles!" else "Copiar APN internet.dalefon.com", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun ApnFieldRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = DalefonTextSubtle)
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = DalefonCyanAccent)
    }
}
