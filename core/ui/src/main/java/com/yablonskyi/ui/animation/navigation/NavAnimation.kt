package com.yablonskyi.ui.animation.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry

sealed interface NavAnimation {
    /**
     * Right to Left Slide Animation
     */
    object SlideTransition : NavAnimation {

        private const val DURATION_MILLIS = 300

        val enterSlideTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards EnterTransition?) =
            {
                slideIntoContainer(
                    animationSpec = tween(DURATION_MILLIS),
                    towards = AnimatedContentTransitionScope.SlideDirection.Left
                )
            }

        val exitSlideTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards ExitTransition?) =
            {
                scaleOut(
                    targetScale = 0.9f
                ) + slideOutOfContainer(
                    animationSpec = tween(DURATION_MILLIS),
                    towards = AnimatedContentTransitionScope.SlideDirection.Right
                )
            }
    }
}

@Composable
fun AnimatedContentScope.roundDuringNavigation(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
): Modifier {
    val animatedCornerRadius by transition.animateDp(
        transitionSpec = {
            tween(durationMillis = 350)
        },
        label = "navigationCornerRadius",
    ) { state ->
        when (state) {
            EnterExitState.PreEnter,
            EnterExitState.PostExit -> cornerRadius

            EnterExitState.Visible -> 0.dp
        }
    }

    return modifier.graphicsLayer {
        shape = RoundedCornerShape(animatedCornerRadius)
        clip = animatedCornerRadius > 0.dp
    }
}