package com.studioname.safecracker.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.studioname.safecracker.ui.DisplayNameScreen
import com.studioname.safecracker.ui.LobbyScreen
import com.studioname.safecracker.ui.MainMenuScreen
import com.studioname.safecracker.viewmodel.DisplayNameVM
import com.studioname.safecracker.viewmodel.LobbyVM
import com.studioname.safecracker.viewmodel.MainMenuVM

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "display_name") {
        composable("display_name") {
            val viewModel: DisplayNameVM = hiltViewModel()
            DisplayNameScreen(navController = navController, viewModel = viewModel)
        }

        composable("main_menu") {
            val viewModel: MainMenuVM = hiltViewModel()
            MainMenuScreen(
                viewModel = viewModel,
                onLobbyJoined = { code ->
                    navController.navigate("lobby/$code")
                }
            )
        }

        composable(
            route = "lobby/{lobbyCode}",
            arguments = listOf(navArgument("lobbyCode") { type = NavType.StringType })
        ) { backStackEntry ->
            val lobbyCode = backStackEntry.arguments?.getString("lobbyCode").orEmpty()
            val viewModel: LobbyVM = hiltViewModel()

            LobbyScreen(
                lobbyCode = lobbyCode,
                viewModel = viewModel,
                onLeave = {
                    navController.popBackStack()
                }
            )
        }
    }
}
