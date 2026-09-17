package com.yablonskyi.ui.animation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.NavBackStackEntry

sealed interface NavAnimation {
    /**
     * Right to Left Slide Animation
     */
    object SlideTransition : NavAnimation {

        private const val DURATION_MILLIS = 400

        val enterSlideTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards EnterTransition?) =
            {
                slideIntoContainer(
                    animationSpec = tween(DURATION_MILLIS, easing = LinearEasing),
                    towards = AnimatedContentTransitionScope.SlideDirection.Start
                )
            }

        val exitSlideTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> @JvmSuppressWildcards ExitTransition?) =
            {
                slideOutOfContainer(
                    animationSpec = tween(DURATION_MILLIS, easing = LinearEasing),
                    towards = AnimatedContentTransitionScope.SlideDirection.End
                )
            }
    }

    object FadeTransition: NavAnimation {
        private const val DURATION_MILLIS = 400

        val enterTransition = fadeIn(tween(DURATION_MILLIS))
        val exitTransition = fadeOut(tween(DURATION_MILLIS))
    }
}