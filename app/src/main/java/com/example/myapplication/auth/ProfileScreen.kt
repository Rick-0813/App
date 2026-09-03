package com.example.myapplication.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.MainViewModel
import com.example.myapplication.ui.components.CuteInfoDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onLogout: () -> Unit,
    onBackToMenu: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val currentUser = viewModel.currentUser

    var showEditDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    var showLinkGmailDialog by remember { mutableStateOf(false) }
    var gmailInput by remember { mutableStateOf("") }
    var linkError by remember { mutableStateOf("") }

    var newName by remember { mutableStateOf(currentUser?.name ?: "") }
    var newPhone by remember { mutableStateOf(currentUser?.phone ?: "") }
    var newPassword by remember { mutableStateOf("") }

    val primaryPurple = Color(0xFF7E57C2)
    val darkPurple = Color(0xFF512DA8)
    val softSurface = Color(0xFFF5F3FF)
    val textDark = Color(0xFF1E1B4B)

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(primaryPurple, darkPurple)
    )

    Box(modifier = Modifier.fillMaxSize().background(backgroundBrush)) {
        Scaffold(
            modifier = Modifier.statusBarsPadding(),
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text("Profile & Settings ✨", fontWeight = FontWeight.Black, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = onBackToMenu) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = softSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = CircleShape,
                            color = primaryPurple
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = currentUser?.name?.take(1)?.uppercase() ?: "U",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = currentUser?.name ?: "User",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = textDark
                        )
                        Text(
                            text = currentUser?.email.takeIf { !it.isNullOrBlank() } ?: "No email linked",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            color = Color(0xFFE0E7FF),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "Role: ${currentUser?.role ?: "Worker"} 👑",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = primaryPurple
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ProfileMenuItem(
                        title = if (currentUser?.role == "Employer") "Switch to Hunter Mode 🌈" else "Switch to Boss Mode 👑",
                        icon = Icons.Default.SwapHoriz,
                        iconTint = primaryPurple
                    ) {
                        val newRole = if (currentUser?.role == "Employer") "Worker" else "Employer"
                        viewModel.updateUserRole(newRole)


                        onBackToMenu()
                    }

                    ProfileMenuItem(
                        title = "Link Google Account (Gmail)",
                        icon = Icons.Default.Link,
                        iconTint = Color(0xFF2563EB)
                    ) {
                        gmailInput = currentUser?.email ?: ""
                        linkError = ""
                        showLinkGmailDialog = true
                    }

                    ProfileMenuItem(
                        title = "Edit Profile ✨",
                        icon = Icons.Default.Edit,
                        iconTint = primaryPurple
                    ) {
                        newName = currentUser?.name ?: ""
                        newPhone = currentUser?.phone ?: ""
                        newPassword = ""
                        showEditDialog = true
                    }

                    ProfileMenuItem(
                        title = "Terms of Service 📜",
                        icon = Icons.Default.Description,
                        iconTint = Color(0xFF16A34A)
                    ) {
                        showTermsDialog = true
                    }

                    ProfileMenuItem(
                        title = "Privacy Policy 🔐",
                        icon = Icons.Default.Lock,
                        iconTint = Color(0xFFD97706)
                    ) {
                        showPrivacyDialog = true
                    }

                    ProfileMenuItem(
                        title = "Log Out 🚪",
                        icon = Icons.Default.Logout,
                        iconTint = Color(0xFFEF4444)
                    ) {
                        onLogout()
                    }

                    ProfileMenuItem(
                        title = "Delete Account 🗑️",
                        icon = Icons.Default.DeleteForever,
                        iconTint = Color.Red
                    ) {
                        showDeleteConfirm = true
                    }
                }
            }
        }
    }

    if (showLinkGmailDialog) {
        AlertDialog(
            onDismissRequest = { showLinkGmailDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = { Text("Link Your Gmail ✨", fontWeight = FontWeight.Bold, color = textDark) },
            text = {
                Column {
                    OutlinedTextField(
                        value = gmailInput,
                        onValueChange = { gmailInput = it; linkError = "" },
                        label = { Text("Enter Gmail Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        textStyle = TextStyle(color = textDark, fontSize = 16.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = textDark,
                            unfocusedTextColor = textDark,
                            focusedBorderColor = primaryPurple,
                            unfocusedBorderColor = Color(0xFFF3E8FF)
                        )
                    )
                    if (linkError.isNotEmpty()) {
                        Text(text = linkError, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (gmailInput.contains("@gmail.com")) {
                            scope.launch {
                                viewModel.linkGoogleAccount(gmailInput, currentUser?.name ?: "User")
                                showLinkGmailDialog = false
                                gmailInput = ""
                            }
                        } else {
                            linkError = "Please enter a valid @gmail.com address"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryPurple),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Confirm Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLinkGmailDialog = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        )
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = { Text("Edit Profile ✏️", fontWeight = FontWeight.Bold, color = textDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newName,
                        onValueChange = { newName = it },
                        label = { Text("Full Name") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = textDark, fontSize = 16.sp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textDark, unfocusedTextColor = textDark)
                    )
                    OutlinedTextField(
                        value = newPhone,
                        onValueChange = { newPhone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = textDark, fontSize = 16.sp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textDark, unfocusedTextColor = textDark)
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password (Optional)") },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(color = textDark, fontSize = 16.sp),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = textDark, unfocusedTextColor = textDark)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.updateUserProfile(newName, currentUser?.email ?: "", newPhone, newPassword)
                            showEditDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primaryPurple)
                ) { Text("Save Changes") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancel", color = Color.Gray) }
            }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            title = { Text("Delete Account? ⚠️", fontWeight = FontWeight.Bold, color = textDark) },
            text = { Text("Are you sure you want to delete your account? All your profile information and data will be permanently erased.", color = Color(0xFF4B5563)) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCurrentUserAccount()
                        showDeleteConfirm = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) { Text("Delete Forever", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel", color = Color.Gray) }
            }
        )
    }

    if (showTermsDialog) {
        CuteInfoDialog(
            title = "Terms of Service 📜",
            content = "Welcome to JobBoom! By using our platform, you agree to treat everyone with respect, provide honest information, and use our magic for good.",
            onDismiss = { showTermsDialog = false }
        )
    }

    if (showPrivacyDialog) {
        CuteInfoDialog(
            title = "Privacy Policy 🔐",
            content = "We value your trust. Your data is strictly protected and only used to match you with your dream jobs.",
            onDismiss = { showPrivacyDialog = false }
        )
    }
}

@Composable
fun ProfileMenuItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconTint: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = iconTint.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
        }
    }
}