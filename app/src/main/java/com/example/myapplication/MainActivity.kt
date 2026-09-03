package com.example.myapplication

import android.net.http.HttpResponseCache.install
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

// --- 你的其他 Screen Imports (请保留) ---
import com.example.myapplication.auth.LoginRegisterScreen
import com.example.myapplication.auth.ProfileScreen
import com.example.myapplication.auth.RoleSelectionScreen
import com.example.myapplication.employer.CompanyDetailsScreen
import com.example.myapplication.employer.EmployerScreen
import com.example.myapplication.myfeature.MyJobDetailsScreen
import com.example.myapplication.worker.JobSearchScreen
import com.example.myapplication.ui.theme.MyApplicationTheme

// --- Supabase Imports (Practical 9) ---[cite: 1]
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable


const val SUPABASE_URL = "https://oikfmjxtrsspyzyyicuz.supabase.co"
const val SUPABASE_KEY = "sb_secret_rApl2aM0kkeHcqNZG0ccwQ_rEs8StaA"

val supabase = createSupabaseClient(
    supabaseUrl = SUPABASE_URL,
    supabaseKey = SUPABASE_KEY
) {
    install(Postgrest)
}

@Serializable
data class User(
    val id: Int,
    val name: String,
    val email: String
)

@Serializable
data class UserInput(
    val name: String,
    val email: String
)

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
                onSelectEmployer = { navController.navigate("employer_dashboard") },
                onGoToSupabase = { navController.navigate("supabase_contacts") }
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

        // Your assigned Company Details screen is safely registered here
        composable(route = "company_details") {
            CompanyDetailsScreen(viewModel = sharedViewModel, navController = navController)
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

        composable(route = "supabase_contacts") {
            UserListScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserListScreen(onBack: () -> Unit) {
    var users by remember { mutableStateOf<List<User>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedUser by remember { mutableStateOf<User?>(null) }

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    fun showMessage(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    suspend fun fetchUsers() {
        isLoading = true
        try {
            users = withContext(Dispatchers.IO) {
                supabase.from("contact").select().decodeList<User>()
            }
        } catch (e: Exception) {
            showMessage("Failed to fetch users: " + e.message)
        } finally {
            isLoading = false
        }
    }

    suspend fun addUser() {
        if (name.isBlank() || email.isBlank()) return
        isLoading = true
        try {
            val newUser = withContext(Dispatchers.IO) {
                supabase.from("contact")
                    .insert(UserInput(name.trim(), email.trim())) { select() }
                    .decodeSingle<User>()
            }
            users = users + newUser
            name = ""
            email = ""
            showMessage("User added successfully!")
        } catch (e: Exception) {
            showMessage("Failed to add user: " + e.message)
        } finally {
            isLoading = false
        }
    }

    suspend fun updateUser() {
        val current = selectedUser ?: return
        if (name.isBlank() && email.isBlank()) return
        isLoading = true
        try {
            val updated = withContext(Dispatchers.IO) {
                supabase.from("contact").update({
                    set("name", if (name.isNotBlank()) name.trim() else current.name)
                    set("email", if (email.isNotBlank()) email.trim() else current.email)
                }) {
                    select()
                    filter { eq("id", current.id) }
                }.decodeSingle<User>()
            }
            users = users.map { if (it.id == updated.id) updated else it }
            name = ""
            email = ""
            selectedUser = null
            showMessage("User updated successfully!")
        } catch (e: Exception) {
            showMessage("Failed to update user: " + e.message)
        } finally {
            isLoading = false
        }
    }

    suspend fun deleteUser(userId: Int) {
        isLoading = true
        try {
            withContext(Dispatchers.IO) {
                supabase.from("contact").delete {
                    filter { eq("id", userId) }
                }
            }
            users = users.filterNot { it.id == userId }
            showMessage("User deleted successfully!")
        } catch (e: Exception) {
            showMessage("Failed to delete user: " + e.message)
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        fetchUsers()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Supabase User App") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = {
                    scope.launch {
                        if (selectedUser != null) updateUser() else addUser()
                    }
                }) {
                    Text(if (selectedUser != null) "Update User" else "Add User")
                }
                if (selectedUser != null) {
                    TextButton(onClick = {
                        selectedUser = null
                        name = ""
                        email = ""
                    }) {
                        Text("Cancel")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                    users.isEmpty() -> Text(
                        text = "No users found. Add one!",
                        modifier = Modifier.align(Alignment.Center)
                    )
                    else -> LazyColumn {
                        items(users, key = { user -> user.id }) { user ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                ListItem(
                                    leadingContent = {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            modifier = Modifier.size(40.dp),
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(user.name.take(1).uppercase())
                                            }
                                        }
                                    },
                                    headlineContent = { Text(user.name) },
                                    supportingContent = { Text(user.email) },
                                    trailingContent = {
                                        Row {
                                            IconButton(onClick = {
                                                selectedUser = user
                                                name = user.name
                                                email = user.email
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Edit",
                                                    tint = Color.Blue
                                                )
                                            }
                                            IconButton(onClick = {
                                                scope.launch { deleteUser(user.id) }
                                            }) {
                                                Icon(
                                                    imageVector = Icons.Default.Delete,
                                                    contentDescription = "Delete",
                                                    tint = Color.Red
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
