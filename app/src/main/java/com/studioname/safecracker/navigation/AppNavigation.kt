package com.studioname.safecracker.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.studioname.safecracker.ui.DisplayNameScreen
import com.studioname.safecracker.ui.MainMenuScreen
import com.studioname.safecracker.ui.RoomLobbyScreen
import com.studioname.safecracker.viewmodel.DisplayNameVM
import com.studioname.safecracker.viewmodel.MainMenuVM
import com.studioname.safecracker.viewmodel.RoomLobbyVM

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
                    navController.navigate("room_lobby/$code")
                }
            )
        }

        composable(
            route = "room_lobby/{roomCode}",
            arguments = listOf(navArgument("roomCode") { type = NavType.StringType })
        ) { backStackEntry ->
            val roomCode = backStackEntry.arguments?.getString("roomCode").orEmpty()
            val viewModel: RoomLobbyVM = hiltViewModel()

            RoomLobbyScreen(
                roomCode = roomCode,
                viewModel = viewModel,
                onLeave = {
                    navController.popBackStack()
                }
            )
        }
    }
}