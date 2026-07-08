package com.example.calorietracker.ui.theme.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun ExpandingLoadingBar(
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF22C55E)
) {
    val widthFactor = remember { Animatable(0.1f) }
    val alpha = remember { Animatable(0.3f) }
    val exitProgress = remember { Animatable(0f) }
    
    var isCurrentlyExiting by remember { mutableStateOf(false) }
    var hasStartedLoadingAtLeastOnce by remember { mutableStateOf(false) }

    LaunchedEffect(isLoading) {
        if (isLoading) {
            hasStartedLoadingAtLeastOnce = true
            exitProgress.snapTo(0f)
            isCurrentlyExiting = false
            
            while (true) {
                launch { alpha.animateTo(1f, tween(800, easing = LinearEasing)) }
                widthFactor.animateTo(0.95f, tween(800, easing = FastOutSlowInEasing))
                
                launch { alpha.animateTo(0.3f, tween(800, easing = LinearEasing)) }
                widthFactor.animateTo(0.1f, tween(800, easing = FastOutSlowInEasing))
            }
        } else if (hasStartedLoadingAtLeastOnce) {
            // Only run exit animation if we were actually loading
            isCurrentlyExiting = true
            launch { alpha.animateTo(1f, tween(200)) }
            widthFactor.animateTo(1f, tween(200, easing = FastOutLinearInEasing))
            
            exitProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(450, easing = FastOutSlowInEasing)
            )
            
            isCurrentlyExiting = false
            exitProgress.snapTo(0f)
            widthFactor.snapTo(0.1f)
            alpha.snapTo(0.3f)
        }
    }

    Box(modifier = modifier.fillMaxWidth().height(2.dp)) {
        if (isLoading || isCurrentlyExiting) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                val centerX = canvasWidth / 2
                val centerY = canvasHeight / 2

                if (!isCurrentlyExiting) {
                    val w = canvasWidth * widthFactor.value
                    drawLine(
                        color = color.copy(alpha = alpha.value),
                        start = Offset(centerX - w / 2, centerY),
                        end = Offset(centerX + w / 2, centerY),
                        strokeWidth = canvasHeight,
                        cap = StrokeCap.Round
                    )
                } else {
                    val p = exitProgress.value
                    val currentAlpha = (1f - p).coerceIn(0f, 1f)
                    val halfWidth = canvasWidth / 2
                    val moveOffset = centerX * p * 1.3f
                    
                    drawLine(
                        color = color.copy(alpha = currentAlpha),
                        start = Offset(centerX - moveOffset - halfWidth, centerY),
                        end = Offset(centerX - moveOffset, centerY),
                        strokeWidth = canvasHeight,
                        cap = StrokeCap.Round
                    )
                    
                    drawLine(
                        color = color.copy(alpha = currentAlpha),
                        start = Offset(centerX + moveOffset, centerY),
                        end = Offset(centerX + moveOffset + halfWidth, centerY),
                        strokeWidth = canvasHeight,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}
