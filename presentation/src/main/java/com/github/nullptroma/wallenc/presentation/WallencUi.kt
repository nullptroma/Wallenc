package com.github.nullptroma.wallenc.presentation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.github.nullptroma.wallenc.presentation.navigation.NavBarItemData
import com.github.nullptroma.wallenc.presentation.navigation.rememberNavigationState
import com.github.nullptroma.wallenc.presentation.screens.main.MainRoute
import com.github.nullptroma.wallenc.presentation.screens.main.MainScreen
import com.github.nullptroma.wallenc.presentation.screens.main.MainViewModel
import com.github.nullptroma.wallenc.presentation.screens.settings.SettingsRoute
import com.github.nullptroma.wallenc.presentation.screens.settings.SettingsScreen
import com.github.nullptroma.wallenc.presentation.screens.settings.SettingsViewModel
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
fun WallencNavRoot(viewModel: WallencViewModel = hiltViewModel()) {
    val navState = rememberNavigationState()
    val mainNavState = rememberNavigationState()

    val mainViewModel: MainViewModel = hiltViewModel()
    val settingsViewModel: SettingsViewModel = hiltViewModel()

    val topLevelRoutes = viewModel.routes

    val topLevelNavBarItems = remember {
        mapOf(
            MainRoute::class.qualifiedName!! to NavBarItemData(
                R.string.nav_label_main, MainRoute::class.qualifiedName!!, Icons.Rounded.Menu
            ),
            SettingsRoute::class.qualifiedName!! to NavBarItemData(
                R.string.nav_label_settings,
                SettingsRoute::class.qualifiedName!!,
                Icons.Rounded.Settings
            )
        )
    }


    Scaffold(bottomBar = {
        NavigationBar(modifier = Modifier.height(64.dp)) {
            val navBackStackEntry by navState.navHostController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            topLevelNavBarItems.forEach {
                val routeClassName = it.key
                val navBarItemData = it.value
                NavigationBarItem(icon = {
                    if (navBarItemData.icon != null) Icon(
                        navBarItemData.icon,
                        contentDescription = stringResource(navBarItemData.nameStringResourceId)
                    )
                },
                    label = { Text(stringResource(navBarItemData.nameStringResourceId)) },
                    selected = currentRoute?.startsWith(routeClassName) == true,
                    onClick = {
                        var route = topLevelRoutes[navBarItemData.screenRouteClass]
                        if (route == null)
                            throw NullPointerException("Route $route not found")
                        if (currentRoute?.startsWith(routeClassName) != true) navState.navigationTo(
                            route
                        )
                    })
            }
        }
    }) { innerPaddings ->
        NavHost(
            navState.navHostController,
            startDestination = topLevelRoutes[MainRoute::class.qualifiedName]!!
        ) {
            composable<MainRoute>(enterTransition = {
                fadeIn(tween(200))
            }, exitTransition = {
                fadeOut(tween(200))
            }) {
                MainScreen(
                    modifier = Modifier.padding(innerPaddings),
                    navState = mainNavState,
                    viewModel = mainViewModel
                )
            }
            composable<SettingsRoute>(enterTransition = {
                fadeIn(tween(200))
            }, exitTransition = {
                fadeOut(tween(200))
            }) {
                SettingsScreen(Modifier.padding(innerPaddings), settingsViewModel)
            }
        }
    }
}