package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.model.DalefonAiState
import com.example.ui.theme.DalefonCharAquaCyan
import com.example.ui.theme.DalefonCharDarkViolet
import com.example.ui.theme.DalefonCharHotPink
import com.example.ui.theme.DalefonCharLilac
import com.example.ui.theme.DalefonCharLilacBright
import com.example.ui.theme.DalefonCharLilacLight
import com.example.ui.theme.DalefonCharLilacShadow
import com.example.ui.theme.DalefonCharNeonPink
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * Avatar oficial de Dalefon AI según el diseño del agente:
 * - Forma orgánica 'd' en 3D iridiscente con degradados lila, cian y rosa neón.
 * - Estados visuales precisos:
 *   1. BASE (IDLE): Ojos en cápsula vertical blanca con halo lila suave y parpadeo periódico.
 *   2. ESCUCHANDO: Ojos circulares brillantes en cian turquesa + onda de audio inferior (···|·|||·|···).
 *   3. PROCESANDO: Ojos en ranura horizontal (-- ) + aro circular de puntos giratorios alrededor del rostro.
 *   4. PENSANDO: Ojos asimétricos curiosos mirando arriba a la derecha + aro de puntos de pensamiento girando arriba a la izquierda.
 *   5. RESPONDIENDO: Ojos en arco feliz (^^) en rosa neón + silueta con resplandor neón doble.
 */
@Composable
fun DalefonAvatar(
    state: DalefonAiState,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 210.dp,
    showWaveform: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dalefon_avatar_anim")

    // Flotación orgánica suave
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "avatar_float"
    )

    // Rotación para anillos de procesamiento y pensamiento
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dots_rotation"
    )

    // Modulación de onda de voz para estado ESCUCHANDO y RESPONDIENDO
    val wavePulse by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave_pulse"
    )

    // Parpadeo periódico natural para estado BASE
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(state) {
        while (true) {
            delay(3400)
            if (state == DalefonAiState.IDLE) {
                isBlinking = true
                delay(130)
                isBlinking = false
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.testTag("dalefon_avatar_container")
    ) {
        Box(
            modifier = Modifier
                .offset(y = floatOffset.dp)
                .size(sizeDp)
                .testTag("dalefon_avatar_character"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                // 1. CAPA POSTERIOR TRANSLÚCIDA (Antena secundaria / efecto de cristal 3D como en el logo Dalefon)
                drawSecondaryBackdropLayer(w, h, state)

                // 2. CUERPO PRINCIPAL DEL PERSONAJE DALEFON EN FORMA DE 'd'
                drawDalefonBody(w, h, state)

                // 3. OJOS E INDICADORES ESPECÍFICOS SEGÚN EL ESTADO
                drawDalefonStateElements(
                    state = state,
                    w = w,
                    h = h,
                    isBlinking = isBlinking,
                    rotationAngle = rotationAngle,
                    wavePulse = wavePulse
                )
            }
        }

        // ONDA DE AUDIO INFERIOR para estado ESCUCHANDO (···|·|||·|···)
        if (showWaveform && state == DalefonAiState.LISTENING) {
            Spacer(modifier = Modifier.height(10.dp))
            DalefonSoundwaveBar(wavePulse = wavePulse)
        }
    }
}

/**
 * Traza la silueta orgánica del cuerpo de Dalefon (cabeza redondeada con swoop/antena ascendente hacia la derecha).
 */
private fun createDalefonBodyPath(w: Float, h: Float): Path {
    val path = Path()

    // Parámetros calculados para la forma de gota/letra 'd'
    val headCenterX = w * 0.48f
    val headCenterY = h * 0.58f
    val headRadius = w * 0.35f

    // Inicio en la base inferior izquierda de la cabeza
    path.moveTo(headCenterX - headRadius * 0.7f, headCenterY + headRadius * 0.7f)

    // Curva por el fondo y el lado izquierdo de la cabeza
    path.cubicTo(
        headCenterX - headRadius * 1.05f, headCenterY + headRadius * 0.4f,
        headCenterX - headRadius * 1.05f, headCenterY - headRadius * 0.5f,
        headCenterX - headRadius * 0.6f, headCenterY - headRadius * 0.85f
    )

    // Transición hacia la parte superior y la antena swoop derecha
    path.cubicTo(
        headCenterX - headRadius * 0.1f, headCenterY - headRadius * 1.05f,
        w * 0.42f, h * 0.30f,
        w * 0.55f, h * 0.18f
    )

    // Punta redondeada superior de la antena
    path.cubicTo(
        w * 0.61f, h * 0.11f,
        w * 0.70f, h * 0.13f,
        w * 0.69f, h * 0.22f
    )

    // Descenso curvo por el costado derecho exterior hacia la base
    path.cubicTo(
        w * 0.68f, h * 0.36f,
        w * 0.79f, h * 0.44f,
        w * 0.81f, h * 0.62f
    )

    // Cierre por la parte inferior derecha
    path.cubicTo(
        w * 0.82f, h * 0.82f,
        headCenterX + headRadius * 0.4f, headCenterY + headRadius * 1.02f,
        headCenterX - headRadius * 0.7f, headCenterY + headRadius * 0.7f
    )

    path.close()
    return path
}

/**
 * Dibuja la capa trasera translúcida desplazada (swoop secundario con tono cian/magenta).
 */
private fun DrawScope.drawSecondaryBackdropLayer(w: Float, h: Float, state: DalefonAiState) {
    val backPath = Path()
    // Curva de la capa trasera secundaria inspirada en el logo
    backPath.moveTo(w * 0.22f, h * 0.46f)
    backPath.cubicTo(
        w * 0.20f, h * 0.28f,
        w * 0.38f, h * 0.12f,
        w * 0.54f, h * 0.08f
    )
    backPath.cubicTo(
        w * 0.60f, h * 0.06f,
        w * 0.66f, h * 0.10f,
        w * 0.64f, h * 0.18f
    )
    backPath.cubicTo(
        w * 0.54f, h * 0.24f,
        w * 0.38f, h * 0.36f,
        w * 0.32f, h * 0.54f
    )
    backPath.close()

    drawPath(
        path = backPath,
        brush = Brush.linearGradient(
            colors = listOf(
                DalefonCharAquaCyan.copy(alpha = 0.35f),
                DalefonCharLilac.copy(alpha = 0.20f),
                DalefonCharNeonPink.copy(alpha = 0.25f)
            ),
            start = Offset(w * 0.2f, h * 0.1f),
            end = Offset(w * 0.6f, h * 0.4f)
        )
    )
}

/**
 * Renderiza el cuerpo volumétrico 3D de Dalefon con sus degradados iridiscentes.
 */
private fun DrawScope.drawDalefonBody(w: Float, h: Float, state: DalefonAiState) {
    val bodyPath = createDalefonBodyPath(w, h)

    // Si está en estado RESPONDIENDO, dibujamos un resplandor de contorno neón vibrante rosa y cian
    if (state == DalefonAiState.RESPONDING) {
        drawPath(
            path = bodyPath,
            brush = Brush.sweepGradient(
                colors = listOf(
                    DalefonCharHotPink,
                    DalefonCharAquaCyan,
                    DalefonCharLilacBright,
                    DalefonCharHotPink
                ),
                center = Offset(w * 0.5f, h * 0.5f)
            ),
            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
        )
        // Aura exterior difusa
        drawPath(
            path = bodyPath,
            color = DalefonCharHotPink.copy(alpha = 0.35f),
            style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
        )
    }

    // Relleno base iridiscente (Lila suave a sombra violeta)
    drawPath(
        path = bodyPath,
        brush = Brush.radialGradient(
            colors = listOf(
                DalefonCharLilacLight,
                DalefonCharLilac,
                DalefonCharLilacShadow,
                DalefonCharDarkViolet
            ),
            center = Offset(w * 0.46f, h * 0.48f),
            radius = w * 0.55f
        )
    )

    // Borde de luz cian en la antena derecha y costado superior
    drawPath(
        path = bodyPath,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                DalefonCharAquaCyan.copy(alpha = 0.85f),
                DalefonCharAquaCyan.copy(alpha = 0.95f),
                Color.Transparent
            ),
            start = Offset(w * 0.40f, h * 0.10f),
            end = Offset(w * 0.85f, h * 0.65f)
        ),
        style = Stroke(width = 4.dp.toPx())
    )

    // Luz de recorte trasera en rosa neón (en la parte inferior y curva interna de la antena)
    drawPath(
        path = bodyPath,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.Transparent,
                DalefonCharNeonPink.copy(alpha = 0.70f),
                Color.Transparent
            ),
            start = Offset(w * 0.60f, h * 0.30f),
            end = Offset(w * 0.82f, h * 0.85f)
        ),
        style = Stroke(width = 3.dp.toPx())
    )

    // Sombra interna suave en la esquina inferior izquierda
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color.Transparent,
                DalefonCharDarkViolet.copy(alpha = 0.45f)
            ),
            center = Offset(w * 0.30f, h * 0.75f),
            radius = w * 0.30f
        ),
        radius = w * 0.30f,
        center = Offset(w * 0.30f, h * 0.75f)
    )
}

/**
 * Renderiza los ojos e indicadores únicos de cada uno de los 5 estados de Dalefon AI:
 * BASE, ESCUCHANDO, PROCESANDO, PENSANDO, RESPONDIENDO.
 */
private fun DrawScope.drawDalefonStateElements(
    state: DalefonAiState,
    w: Float,
    h: Float,
    isBlinking: Boolean,
    rotationAngle: Float,
    wavePulse: Float
) {
    val faceCenterX = w * 0.49f
    val faceCenterY = h * 0.54f

    when (state) {
        DalefonAiState.IDLE -> {
            // 1. BASE: Ojos en cápsula vertical blanca con halo lila suave
            val eyeWidth = w * 0.088f
            val eyeHeight = if (isBlinking) 3.dp.toPx() else w * 0.165f
            val corner = CornerRadius(eyeWidth / 2f, eyeHeight / 2f)

            val leftEyeCenter = Offset(w * 0.42f, faceCenterY)
            val rightEyeCenter = Offset(w * 0.56f, faceCenterY)

            // Halo suave lila
            drawRoundRect(
                color = DalefonCharLilacLight.copy(alpha = 0.55f),
                topLeft = Offset(leftEyeCenter.x - eyeWidth / 2f - 3.dp.toPx(), leftEyeCenter.y - eyeHeight / 2f - 3.dp.toPx()),
                size = Size(eyeWidth + 6.dp.toPx(), eyeHeight + 6.dp.toPx()),
                cornerRadius = CornerRadius(corner.x + 3.dp.toPx(), corner.y + 3.dp.toPx())
            )
            drawRoundRect(
                color = DalefonCharLilacLight.copy(alpha = 0.55f),
                topLeft = Offset(rightEyeCenter.x - eyeWidth / 2f - 3.dp.toPx(), rightEyeCenter.y - eyeHeight / 2f - 3.dp.toPx()),
                size = Size(eyeWidth + 6.dp.toPx(), eyeHeight + 6.dp.toPx()),
                cornerRadius = CornerRadius(corner.x + 3.dp.toPx(), corner.y + 3.dp.toPx())
            )

            // Núcleo blanco brillante
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(leftEyeCenter.x - eyeWidth / 2f, leftEyeCenter.y - eyeHeight / 2f),
                size = Size(eyeWidth, eyeHeight),
                cornerRadius = corner
            )
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(rightEyeCenter.x - eyeWidth / 2f, rightEyeCenter.y - eyeHeight / 2f),
                size = Size(eyeWidth, eyeHeight),
                cornerRadius = corner
            )
        }

        DalefonAiState.LISTENING -> {
            // 2. ESCUCHANDO: Dos ojos circulares brillantes en cian turquesa
            val eyeRadius = w * 0.076f
            val leftEyeCenter = Offset(w * 0.42f, faceCenterY)
            val rightEyeCenter = Offset(w * 0.56f, faceCenterY)

            // Halo cian difuso exterior
            drawCircle(
                color = DalefonCharAquaCyan.copy(alpha = 0.45f),
                radius = eyeRadius + 5.dp.toPx(),
                center = leftEyeCenter
            )
            drawCircle(
                color = DalefonCharAquaCyan.copy(alpha = 0.45f),
                radius = eyeRadius + 5.dp.toPx(),
                center = rightEyeCenter
            )

            // Círculo cian turquesa
            drawCircle(
                color = DalefonCharAquaCyan,
                radius = eyeRadius,
                center = leftEyeCenter
            )
            drawCircle(
                color = DalefonCharAquaCyan,
                radius = eyeRadius,
                center = rightEyeCenter
            )

            // Destello interior blanco de pupila viva
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = eyeRadius * 0.5f,
                center = leftEyeCenter
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.9f),
                radius = eyeRadius * 0.5f,
                center = rightEyeCenter
            )
        }

        DalefonAiState.PROCESSING -> {
            // 3. PROCESANDO: Ojos en ranura horizontal (-- ) + aro circular de puntos giratorios alrededor del rostro
            val slitWidth = w * 0.11f
            val slitHeight = 4.dp.toPx()
            val corner = CornerRadius(slitHeight / 2f, slitHeight / 2f)

            val leftSlitCenter = Offset(w * 0.41f, faceCenterY)
            val rightSlitCenter = Offset(w * 0.57f, faceCenterY)

            // Ojos horizontales blancos brillantes
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(leftSlitCenter.x - slitWidth / 2f, leftSlitCenter.y - slitHeight / 2f),
                size = Size(slitWidth, slitHeight),
                cornerRadius = corner
            )
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(rightSlitCenter.x - slitWidth / 2f, rightSlitCenter.y - slitHeight / 2f),
                size = Size(slitWidth, slitHeight),
                cornerRadius = corner
            )

            // Aro de puntos giratorios alrededor del rostro
            rotate(rotationAngle, Offset(faceCenterX, faceCenterY)) {
                val ringRadius = w * 0.23f
                val dotCount = 18
                for (i in 0 until dotCount) {
                    val angle = Math.toRadians((i * (360.0 / dotCount)))
                    val dotX = faceCenterX + ringRadius * cos(angle).toFloat()
                    val dotY = faceCenterY + ringRadius * sin(angle).toFloat()
                    val alpha = (i.toFloat() / dotCount).coerceIn(0.2f, 1.0f)

                    drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = 2.dp.toPx(),
                        center = Offset(dotX, dotY)
                    )
                }
            }
        }

        DalefonAiState.THINKING -> {
            // 4. PENSANDO: Ojos asimétricos curiosos mirando arriba a la derecha + aro de puntos de pensamiento girando arriba a la izquierda
            // Ojo izquierdo: inclinado mirando arriba-derecha
            val leftEyeCenter = Offset(w * 0.41f, faceCenterY + 2.dp.toPx())
            val leftEyeRadius = w * 0.055f

            // Fondo blanco del ojo
            drawCircle(
                color = Color.White,
                radius = leftEyeRadius,
                center = leftEyeCenter
            )
            // Pupila oscura arriba a la derecha
            drawCircle(
                color = DalefonCharDarkViolet,
                radius = leftEyeRadius * 0.52f,
                center = Offset(leftEyeCenter.x + 3.dp.toPx(), leftEyeCenter.y - 2.5.dp.toPx())
            )
            // Brillo blanco en la pupila
            drawCircle(
                color = Color.White,
                radius = 1.8.dp.toPx(),
                center = Offset(leftEyeCenter.x + 4.dp.toPx(), leftEyeCenter.y - 3.5.dp.toPx())
            )

            // Ojo derecho: más grande y expresivo mirando arriba a la derecha hacia la antena
            val rightEyeCenter = Offset(w * 0.59f, faceCenterY - 4.dp.toPx())
            val rightEyeRadius = w * 0.065f

            drawCircle(
                color = Color.White,
                radius = rightEyeRadius,
                center = rightEyeCenter
            )
            drawCircle(
                color = DalefonCharDarkViolet,
                radius = rightEyeRadius * 0.52f,
                center = Offset(rightEyeCenter.x + 3.5.dp.toPx(), rightEyeCenter.y - 3.5.dp.toPx())
            )
            drawCircle(
                color = Color.White,
                radius = 2.2.dp.toPx(),
                center = Offset(rightEyeCenter.x + 4.5.dp.toPx(), rightEyeCenter.y - 4.5.dp.toPx())
            )

            // Anillo de puntos de pensamiento arriba a la izquierda del personaje (como en el diseño)
            val thoughtCenterX = w * 0.28f
            val thoughtCenterY = h * 0.19f
            rotate(rotationAngle, Offset(thoughtCenterX, thoughtCenterY)) {
                val thoughtRadius = w * 0.08f
                val dotCount = 8
                for (i in 0 until dotCount) {
                    val angle = Math.toRadians((i * (360.0 / dotCount)))
                    val dotX = thoughtCenterX + thoughtRadius * cos(angle).toFloat()
                    val dotY = thoughtCenterY + thoughtRadius * sin(angle).toFloat()
                    val alpha = (i.toFloat() / dotCount).coerceIn(0.25f, 1.0f)
                    val radius = (1.5f + (i * 0.25f)).dp.toPx()

                    drawCircle(
                        color = Color.White.copy(alpha = alpha),
                        radius = radius,
                        center = Offset(dotX, dotY)
                    )
                }
            }
        }

        DalefonAiState.RESPONDING -> {
            // 5. RESPONDIENDO: Ojos en arco feliz (^^) en rosa neón
            val archWidth = w * 0.095f
            val archHeight = w * 0.065f

            drawHappyArchEye(
                centerX = w * 0.42f,
                centerY = faceCenterY,
                width = archWidth,
                height = archHeight,
                color = DalefonCharHotPink
            )

            drawHappyArchEye(
                centerX = w * 0.56f,
                centerY = faceCenterY,
                width = archWidth,
                height = archHeight,
                color = DalefonCharHotPink
            )
        }
    }
}

/**
 * Dibuja un ojo en arco alegre/curvado en rosa neón para el estado de respuesta.
 */
private fun DrawScope.drawHappyArchEye(
    centerX: Float,
    centerY: Float,
    width: Float,
    height: Float,
    color: Color
) {
    val path = Path().apply {
        moveTo(centerX - width / 2f, centerY + height * 0.3f)
        cubicTo(
            centerX - width / 3f, centerY - height * 0.7f,
            centerX + width / 3f, centerY - height * 0.7f,
            centerX + width / 2f, centerY + height * 0.3f
        )
    }

    // Halo exterior suave rosa
    drawPath(
        path = path,
        color = color.copy(alpha = 0.5f),
        style = Stroke(width = 7.dp.toPx(), cap = StrokeCap.Round)
    )

    // Trazo central blanco/rosa brillante
    drawPath(
        path = path,
        color = Color.White,
        style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
    )
}

/**
 * Visualizador de onda de audio horizontal (···|·|||·|···) que aparece bajo el personaje en modo ESCUCHANDO.
 */
@Composable
fun DalefonSoundwaveBar(
    wavePulse: Float,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.height(24.dp)
    ) {
        val barHeights = listOf(
            3.dp, 4.dp, 5.dp, 12.dp * wavePulse, 6.dp, 18.dp * wavePulse,
            15.dp * wavePulse, 20.dp * wavePulse, 7.dp, 10.dp * wavePulse, 4.dp, 3.dp
        )

        barHeights.forEachIndexed { index, height ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .width(3.dp)
                    .height(height.coerceAtLeast(3.dp))
                    .drawBehind {
                        drawRoundRect(
                            color = if (index in 5..7) DalefonCharAquaCyan else DalefonCharLilacBright,
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                    }
            )
        }
    }
}

/**
 * Insignia de logotipo Dalefon 'd' como aparece en la esquina superior izquierda del diseño oficial.
 */
@Composable
fun DalefonLogoMark(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 38.dp
) {
    Box(
        modifier = modifier.size(sizeDp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Trazo estilizado de la letra 'd' / gota Dalefon
            val path = createDalefonBodyPath(w, h)

            // Fondo translúcido suave
            drawPath(
                path = path,
                brush = Brush.linearGradient(
                    colors = listOf(
                        DalefonCharLilac.copy(alpha = 0.25f),
                        DalefonCharAquaCyan.copy(alpha = 0.15f)
                    )
                )
            )

            // Contorno degradado cian a rosa neón
            drawPath(
                path = path,
                brush = Brush.sweepGradient(
                    colors = listOf(
                        DalefonCharAquaCyan,
                        DalefonCharLilacBright,
                        DalefonCharNeonPink,
                        DalefonCharAquaCyan
                    ),
                    center = Offset(w * 0.5f, h * 0.5f)
                ),
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

/**
 * Mini-avatar para la barra superior del Modo Chat.
 */
@Composable
fun DalefonMiniAvatar(
    state: DalefonAiState,
    modifier: Modifier = Modifier,
    sizeDp: Dp = 44.dp
) {
    DalefonAvatar(
        state = state,
        modifier = modifier,
        sizeDp = sizeDp,
        showWaveform = false
    )
}
