package com.github.nullptroma.wallenc.presentation.screens.main

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.github.nullptroma.wallenc.presentation.R
import com.github.nullptroma.wallenc.presentation.navigation.NavBarItemData
import com.github.nullptroma.wallenc.presentation.navigation.NavigationState
import com.github.nullptroma.wallenc.presentation.navigation.rememberNavigationState
import com.github.nullptroma.wallenc.presentation.screens.main.screens.local.vault.LocalVaultRoute
import com.github.nullptroma.wallenc.presentation.screens.main.screens.local.vault.LocalVaultScreen
import com.github.nullptroma.wallenc.presentation.screens.main.screens.remotes.RemoteVaultsRoute
import com.github.nullptroma.wallenc.presentation.screens.main.screens.remotes.RemoteVaultsScreen


@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.runtime.Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
    navState: NavigationState = rememberNavigationState(),
    routes: MutableMap<String, MainRoute> = rememberSaveable {
        mutableMapOf(
            LocalVaultRoute::class.qualifiedName!! to LocalVaultRoute(),
            RemoteVaultsRoute::class.qualifiedName!! to RemoteVaultsRoute()
        )
    }
) {
    val topLevelNavBarItems = remember {
        mapOf(
            LocalVaultRoute::class.qualifiedName!! to NavBarItemData(
                R.string.nav_label_local_vault, LocalVaultRoute::class.qualifiedName!!, null
            ),
            RemoteVaultsRoute::class.qualifiedName!! to NavBarItemData(
                R.string.nav_label_remote_vaults, RemoteVaultsRoute::class.qualifiedName!!, null
            )
        )
    }

    Scaffold(modifier = modifier, contentWindowInsets = WindowInsets(0.dp), bottomBar = {
        Column {
            NavigationBar(modifier = Modifier.height(48.dp)) {
                val navBackStackEntry by navState.navHostController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                topLevelNavBarItems.forEach {
                    val routeClassName = it.key
                    val navBarItemData = it.value
                    NavigationBarItem(modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                        icon = { Text(stringResource(navBarItemData.nameStringResourceId)) },
                        selected = currentRoute?.startsWith(routeClassName) == true,
                        onClick = {
                            var route = routes[navBarItemData.screenRouteClass]
                            if (route == null)
                                throw NullPointerException("Route $route not found")
                            if (currentRoute?.startsWith(routeClassName) != true) navState.navigationTo(
                                route
                            )
                        })
                }
            }
            HorizontalDivider()
        }
    }) { innerPaddings ->

        NavHost(
            navState.navHostController,
            startDestination = routes[LocalVaultRoute::class.qualifiedName]!!
        ) {
            composable<LocalVaultRoute>(enterTransition = {
                fadeIn(tween(200))
            }, exitTransition = {
                fadeOut(tween(200))
            }) {
                LocalVaultScreen(modifier = Modifier.padding(innerPaddings))
            }
            composable<RemoteVaultsRoute>(enterTransition = {
                fadeIn(tween(200))
            }, exitTransition = {
                fadeOut(tween(200))
            }) {
                RemoteVaultsScreen(modifier = Modifier.padding(innerPaddings))
            }
        }
    }
}