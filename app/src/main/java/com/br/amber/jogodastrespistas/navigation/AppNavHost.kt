package com.br.amber.jogodastrespistas.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
                        /*
                        popUpTo navega para um novo destino e remove da pilha todos os destinos acima do que você indicar nele.
                        Para evitar que o usuário volte para telas anteriores (como login).
                        */
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

        composable(
            route = RoutesEnum.ROOM.route,
            arguments = listOf(navArgument("roomId") { type = NavType.StringType })
        ) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId")

            LaunchedEffect(Unit) {
                if (!authViewModel.isUserLoggedIn()) {
                    navController.navigate(RoutesEnum.LOGIN.route) {
                        popUpTo(RoutesEnum.ROOM.route) { inclusive = true }
                    }
                }
            }

            // Agora RoomScreen recebe o roomId
            if (roomId != null) {
                RoomScreen(navController = navController, roomId = roomId)
            }
        }

    }
}
