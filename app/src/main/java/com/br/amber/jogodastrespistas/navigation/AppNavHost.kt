package com.br.amber.jogodastrespistas.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.br.amber.jogodastrespistas.ui.screens.home.HomeScreen
import com.br.amber.jogodastrespistas.ui.screens.login.LoginScreen
import com.br.amber.jogodastrespistas.ui.screens.room.RoomScreen
import com.br.amber.jogodastrespistas.ui.screens.login.AuthViewModel

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel
) {
    val startDestination = if (authViewModel.isUserLoggedIn()) RoutesEnum.HOME.route else RoutesEnum.LOGIN.route

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(RoutesEnum.LOGIN.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(RoutesEnum.HOME.route) {
                        popUpTo(RoutesEnum.LOGIN.route) { inclusive = true }
                    }
                }
            )
        }

        composable(RoutesEnum.HOME.route) {
            LaunchedEffect(Unit) {
                if (!authViewModel.isUserLoggedIn()) {
                    navController.navigate(RoutesEnum.LOGIN.route) {
                        popUpTo(RoutesEnum.HOME.route) { inclusive = true }
                    }
                }
            }
            HomeScreen(navController)
        }

        composable(RoutesEnum.ROOM.route) {
            LaunchedEffect(Unit) {
                if (!authViewModel.isUserLoggedIn()) {
                    navController.navigate(RoutesEnum.LOGIN.route) {
                        popUpTo(RoutesEnum.ROOM.route) { inclusive = true }
                    }
                }
            }
            RoomScreen(navController)
        }
    }
}
