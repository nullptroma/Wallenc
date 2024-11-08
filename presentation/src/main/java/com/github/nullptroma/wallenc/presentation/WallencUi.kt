package com.github.nullptroma.wallenc.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
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

@Composable
fun WallencNavRoot() {
    val navController = rememberNavController()
    Scaffold { innerPaddings ->
        NavHost(navController, startDestination = MainRoute()) {
            composable<MainRoute> {
                MainScreen(Modifier.padding(innerPaddings), onSettingsRoute = { settingsRoute ->
                    navController.navigate(settingsRoute)
                })
            }
            composable<SettingsRoute> {
                val route: SettingsRoute = it.toRoute()
                SettingsScreen(Modifier.padding(innerPaddings), route.text)
            }
        }
    }
}