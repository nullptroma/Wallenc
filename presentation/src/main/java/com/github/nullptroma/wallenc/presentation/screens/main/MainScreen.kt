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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.toRoute
import com.github.nullptroma.wallenc.presentation.R
import com.github.nullptroma.wallenc.presentation.navigation.NavBarItemData
import com.github.nullptroma.wallenc.presentation.navigation.NavigationState
import com.github.nullptroma.wallenc.presentation.navigation.rememberNavigationState
import com.github.nullptroma.wallenc.presentation.screens.main.screens.local.vault.LocalVaultRoute
import com.github.nullptroma.wallenc.presentation.screens.main.screens.local.vault.LocalVaultScreen
import com.github.nullptroma.wallenc.presentation.screens.main.screens.local.vault.LocalVaultViewModel
import com.github.nullptroma.wallenc.presentation.screens.main.screens.remotes.RemoteVaultsRoute
import com.github.nullptroma.wallenc.presentation.screens.main.screens.remotes.RemoteVaultsScreen
import com.github.nullptroma.wallenc.presentation.screens.main.screens.remotes.RemoteVaultsViewModel
import com.github.nullptroma.wallenc.presentation.screens.shared.TextEditRoute
import com.github.nullptroma.wallenc.presentation.screens.shared.TextEditScreen


@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.runtime.Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    viewModel: MainViewModel = hiltViewModel(),
    navState: NavigationState = rememberNavigationState(),
) {
    val routes = viewModel.routes
    val localVaultViewModel: LocalVaultViewModel = hiltViewModel()
    val remoteVaultsViewModel: RemoteVaultsViewModel = hiltViewModel()

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
                            val route = routes[navBarItemData.screenRouteClass]
                                ?: throw NullPointerException("Route ${navBarItemData.screenRouteClass} not found")
                            if (currentRoute?.startsWith(routeClassName) != true)
                                navState.changeTop(
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
                val route: LocalVaultRoute = it.toRoute()
                LocalVaultScreen(
                    modifier = Modifier.padding(innerPaddings),
                    viewModel = localVaultViewModel
                ) { text ->
                    navState.push(TextEditRoute(text))
                }
            }
            composable<RemoteVaultsRoute>(enterTransition = {
                fadeIn(tween(200))
            }, exitTransition = {
                fadeOut(tween(200))
            }) {
                RemoteVaultsScreen(
                    modifier = Modifier.padding(innerPaddings),
                    viewModel = remoteVaultsViewModel
                )
            }
            composable<TextEditRoute> {
                val route: TextEditRoute = it.toRoute()
                TextEditScreen(route.text)
            }
        }
    }
}