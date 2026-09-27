package com.studioname.safecracker.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.studioname.safecracker.ui.DisplayNameScreen
import com.studioname.safecracker.ui.MainMenuScreen
import com.studioname.safecracker.viewmodel.DisplayNameVM
import com.studioname.safecracker.viewmodel.MainMenuVM
import dagger.hilt.android.lifecycle.HiltViewModel

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
            MainMenuScreen(viewModel = viewModel)

            // TODO: needs onCodeSubmitted callback function logic, similar to above
        }
    }
}