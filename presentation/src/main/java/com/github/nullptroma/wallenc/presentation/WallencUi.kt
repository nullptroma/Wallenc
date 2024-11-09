package com.github.nullptroma.wallenc.presentation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.github.nullptroma.wallenc.presentation.navigation.NavBarItem
import com.github.nullptroma.wallenc.presentation.navigation.rememberNavigationState
import com.github.nullptroma.wallenc.presentation.screens.main.MainRoute
import com.github.nullptroma.wallenc.presentation.screens.main.MainScreen
import com.github.nullptroma.wallenc.presentation.screens.settings.SettingsRoute
import com.github.nullptroma.wallenc.presentation.screens.settings.SettingsScreen
import com.github.nullptroma.wallenc.presentation.theme.WallencTheme


@Composable
fun WallencUi() {
    WallencTheme {
        Surface {
            WallencNavRoot()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallencNavRoot() {
    val navState = rememberNavigationState()

    val topLevelScreenRoutes = rememberSaveable {
        mutableMapOf(
            MainRoute::class.qualifiedName!! to MainRoute(),
            SettingsRoute::class.qualifiedName!! to SettingsRoute("Base settings")
        )
    }

    // Все пункты меню верхнего уровня
    val topLevelNavBarItems = remember {
        listOf(
            NavBarItem("Main", MainRoute::class.qualifiedName!!, Icons.Rounded.Menu),
            NavBarItem("Settings", SettingsRoute::class.qualifiedName!!, Icons.Rounded.Settings)
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navState.navHostController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                topLevelNavBarItems.forEach {
                    val routeClassName = it.screenRouteClass

                    NavigationBarItem(
                        icon = {
                            Icon(
                                it.icon,
                                contentDescription = it.name
                            )
                        },
                        label = { Text(it.name) },
                        selected = currentRoute?.startsWith(routeClassName) == true,
                        onClick = {
                            var route = topLevelScreenRoutes[it.screenRouteClass]
                            if(route == null)
                                throw NoSuchElementException("Screen route of type ${it.screenRouteClass} no found")
                            if(currentRoute?.startsWith(routeClassName) != true)
                                navState.navigationTo(route)
                        }
                    )
                }
            }
        }) { innerPaddings ->
        NavHost(navState.navHostController, startDestination = topLevelScreenRoutes[MainRoute::class.qualifiedName]!!) {
            composable<MainRoute>(enterTransition = {
                fadeIn(tween(200))
            }, exitTransition = {
                fadeOut(tween(200))
            }) {
                MainScreen(Modifier.padding(innerPaddings), onSettingsRoute = { settingsRoute ->
                    topLevelScreenRoutes[settingsRoute::class.qualifiedName!!] = settingsRoute
                    navState.navigationTo(settingsRoute)
                })
            }
            composable<SettingsRoute>(enterTransition = {
                fadeIn(tween(200))
            }, exitTransition = {
                fadeOut(tween(200))
            }) {
                val route: SettingsRoute = it.toRoute()
                SettingsScreen(Modifier.padding(innerPaddings), route.text)
            }
        }
    }
}