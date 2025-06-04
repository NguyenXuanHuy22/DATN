package com.example.datn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.datn.ui.theme.DATNTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
//        setContent {
////            DATNTheme {
////                val navController = rememberNavController()
////                NavHost(navController = navController, startDestination = "splash") {
////                    composable("splash") {
////                        SplashScreen(
////                            onSplashFinished = {
////                                navController.navigate("intro") {
////                                    popUpTo("splash") { inclusive = true }
////                                }
////                            }
////                        )
////                    }
////                    composable("intro") {
////                        IntroScreenContent(
////                            onNavigateToLogin = { navController.navigate("login") },
////                            onNavigateToRegister = { navController.navigate("register") }
////                        )
////                    }
////                    composable("login") {
//                        LoginScreenContent(
////                            onLoginSuccess = {
////                                navController.navigate("home") {
////                                    popUpTo("login") { inclusive = true }
////                                }
////                            },
////                            onNavigateToRegister = {
////                                navController.navigate("register")
////                            }
////                        )
////                    }
////                    composable("register") {
////                        RegisterScreenContent(
////                            onRegisterSuccess = {
////                                navController.navigate("home") {
////                                    popUpTo("register") { inclusive = true }
////                                }
////                            },
////                            onNavigateToLogin = {
////                                navController.navigate("login")
////                            }
////                        )
////                    }
////                    composable("home") {
////                        HomeScreen(navController)
////                    }
////                    composable(
////                        route = "productDetail/{productId}",
////                        arguments = listOf(navArgument("productId") { type = NavType.StringType })
////                    ) { backStackEntry ->
////                        val productId = backStackEntry.arguments?.getString("productId") ?: ""
////                        ProductDetailScreen(productId)
////                    }
////                }
////            }
//        }
    }
}
