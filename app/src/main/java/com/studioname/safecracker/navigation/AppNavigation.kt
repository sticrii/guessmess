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
import com.studioname.safecracker.ui.PickRoomScreen
import com.studioname.safecracker.viewmodel.DisplayNameVM
import com.studioname.safecracker.viewmodel.LobbyVM
import com.studioname.safecracker.viewmodel.MainMenuVM
import com.studioname.safecracker.viewmodel.PickRoomVM
import com.studioname.safecracker.ui.RoomHintsScreen
import com.studioname.safecracker.viewmodel.RoomHintsVM

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
            val lobbyCode = backStackEntry.arguments?.getString("lobbyCode") ?: ""
            val viewModel: LobbyVM = hiltViewModel()

            LobbyScreen(
                viewModel = viewModel,
                onLeave = {
                    navController.popBackStack()
                },
                onStart = {
                    navController.navigate("lobby/$lobbyCode/pickRoom") {
                        popUpTo("lobby/$lobbyCode") { inclusive = true } // so that we cant simply go back to lobby
                    }
                }
            )
        }

        composable(
            route = "lobby/{lobbyCode}/pickRoom",
            arguments = listOf(navArgument("lobbyCode") { type = NavType.StringType })
        ) { backStackEntry ->
            val lobbyCode = backStackEntry.arguments?.getString("lobbyCode") ?: ""
            val viewModel: PickRoomVM = hiltViewModel()

            PickRoomScreen(
                viewModel = viewModel,
                navigateToRoom = { roomName -> navController.navigate("lobby/$lobbyCode/room/$roomName")},
                onLeave = {
                    navController.navigate("main_menu") {
                        popUpTo("main_menu") { inclusive = true}
                    }
                }
            )
        }

        composable(
            route = "lobby/{lobbyCode}/room/{roomName}",
            arguments = listOf(
                navArgument("lobbyCode") { type = NavType.StringType },
                navArgument("roomName") { type = NavType.StringType }
            )
        ) {
            val viewModel: RoomHintsVM = hiltViewModel()
            RoomHintsScreen(viewModel = viewModel)
        }
    }
}
