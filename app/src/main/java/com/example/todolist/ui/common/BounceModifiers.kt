package com.example.todolist.ui.common

import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Modifier cho hiệu ứng bounce khi click
 * Scale nhỏ lại rồi bật ra với spring animation
 */
fun Modifier.bounceClick(
    scaleDown: Float = 0.92f,
    animationSpec: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
): Modifier = composed {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = animationSpec,
        label = "bounceClick"
    )

    this
        .scale(scale)
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    when {
                        event.changes.any { it.pressed } -> isPressed = true
                        else -> isPressed = false
                    }
                }
            }
        }
}

/**
 * Modifier cho hiệu ứng bounce nhẹ hơn (cho cards, items)
 */
fun Modifier.bounceLightClick(
    scaleDown: Float = 0.96f
): Modifier = bounceClick(
    scaleDown = scaleDown,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )
)

/**
 * NestedScrollConnection cho hiệu ứng overscroll bounce
 */
@Composable
fun rememberBounceOverscrollEffect(): BounceOverscrollState {
    return remember { BounceOverscrollState() }
}

class BounceOverscrollState {
    var overscrollOffset by mutableFloatStateOf(0f)
        private set
    
    private val animatable = Animatable(0f)
    
    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            // Nếu đang overscroll và scroll ngược lại, consume scroll để reset
            if (overscrollOffset != 0f) {
                val newOffset = overscrollOffset + available.y * 0.3f
                // Nếu scroll theo hướng ngược lại với overscroll
                if ((overscrollOffset > 0 && available.y < 0) || (overscrollOffset < 0 && available.y > 0)) {
                    val consumed = minOf(kotlin.math.abs(available.y), kotlin.math.abs(overscrollOffset))
                    overscrollOffset = if (overscrollOffset > 0) {
                        (overscrollOffset - consumed).coerceAtLeast(0f)
                    } else {
                        (overscrollOffset + consumed).coerceAtMost(0f)
                    }
                    return Offset(0f, if (available.y > 0) consumed else -consumed)
                }
            }
            return Offset.Zero
        }
        
        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            // Nếu còn scroll dư (đã scroll hết content), tạo overscroll effect
            if (available.y != 0f) {
                val newOffset = overscrollOffset + available.y * 0.4f
                // Giới hạn overscroll
                overscrollOffset = newOffset.coerceIn(-150f, 150f)
                return available
            }
            return Offset.Zero
        }
        
        override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
            // Spring back về 0
            animatable.snapTo(overscrollOffset)
            animatable.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) {
                overscrollOffset = value
            }
            return available
        }
    }
}

/**
 * Modifier để apply overscroll bounce vào LazyColumn/LazyRow
 */
fun Modifier.bounceOverscroll(state: BounceOverscrollState): Modifier = composed {
    this
        .nestedScroll(state.nestedScrollConnection)
        .offset { IntOffset(0, state.overscrollOffset.roundToInt()) }
}

/**
 * Modifier cho hiệu ứng press với ripple + scale nhẹ
 */
fun Modifier.pressScale(
    scaleDown: Float = 0.98f
): Modifier = bounceClick(
    scaleDown = scaleDown,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh
    )
)
