package com.github.nullptroma.wallenc.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.github.nullptroma.wallenc.presentation.screens.ScreenRoute


class NavigationState(
    val navHostController: NavHostController
) {
    fun navigationTo(route: ScreenRoute) {
        navHostController.navigate(route) {
            popUpTo(navHostController.graph.findStartDestination().id)
            launchSingleTop = true
            restoreState = true
        }
    }
}

@Composable
fun rememberNavigationState(
    navHostController: NavHostController? = null
): NavigationState {
    val controller = navHostController ?: rememberNavController()
    return remember { NavigationState(controller) }
}