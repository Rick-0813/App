package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.myapplication.auth.LoginRegisterScreen
import com.example.myapplication.auth.ProfileScreen
import com.example.myapplication.auth.RoleSelectionScreen
import com.example.myapplication.employer.EmployerScreen
import com.example.myapplication.myfeature.MyJobDetailsScreen
import com.example.myapplication.worker.JobSearchScreen
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val sharedViewModel: MainViewModel = viewModel()

    NavHost(navController = navController, startDestination = "login") {

        composable(route = "login") {
            LoginRegisterScreen(
                viewModel = sharedViewModel,
                onLoginSuccess = { navController.navigate("role_selection") }
            )
        }

        composable(route = "role_selection") {
            RoleSelectionScreen(
                viewModel = sharedViewModel,
                onSelectWorker = { navController.navigate("worker_search") },
                onSelectEmployer = { navController.navigate("employer_dashboard") }
            )
        }

        composable(route = "worker_search") {
            JobSearchScreen(viewModel = sharedViewModel, navController = navController)
        }

        composable(route = "employer_dashboard") {
            EmployerScreen(viewModel = sharedViewModel, navController = navController)
        }

        composable(
            route = "job_details/{jobId}/{applicationId}",
            arguments = listOf(
                navArgument("jobId") { type = NavType.IntType },
                navArgument("applicationId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            MyJobDetailsScreen(
                viewModel = sharedViewModel,
                jobId = backStackEntry.arguments?.getInt("jobId") ?: -1,
                applicationId = backStackEntry.arguments?.getInt("applicationId") ?: -1,
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = "worker_profile") {
            ProfileScreen(
                viewModel = sharedViewModel,
                onLogout = {
                    sharedViewModel.logout()
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onBackToMenu = {
                    val isEmployer = sharedViewModel.currentUser?.role == "Employer"
                    if (isEmployer) {
                        navController.navigate("employer_dashboard") {
                            popUpTo("employer_dashboard") { inclusive = true }
                        }
                    } else {
                        navController.navigate("worker_search") {
                            popUpTo("worker_search") { inclusive = true }
                        }
                    }
                }
            )
        }
    }
}
