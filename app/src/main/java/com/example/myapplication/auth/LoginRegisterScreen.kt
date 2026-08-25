package com.example.myapplication.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.myapplication.MainViewModel
import com.example.myapplication.ui.components.CuteInfoDialog
import kotlinx.coroutines.launch

@Composable
fun LoginRegisterScreen(
    viewModel: MainViewModel,
    onLoginSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var step by remember { mutableIntStateOf(1) }
    var isPhoneMode by remember { mutableStateOf(false) }

    var accountInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }
    var isNewUser by remember { mutableStateOf(false) }
    
    var showGoogleDialog by remember { mutableStateOf(false) }
    var showAddGmailDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    val primaryPurple = Color(0xFF7E57C2)
    val darkPurple = Color(0xFF512DA8)
    val softPurple = Color(0xFFF3E8FF)
    val textDark = Color(0xFF1E1B4B)

    val brandGradient = Brush.verticalGradient(
        colors = listOf(primaryPurple, darkPurple)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brandGradient)
    ) {
        Box(
            modifier = Modifier
                .offset(x = (-50).dp, y = (-50).dp)
                .size(200.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.1f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // 同步桌面图标风格的 Logo
            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 12.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(brandGradient), // 使用紫色渐变背景
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "JB",
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White // 白色文字
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "JobBoom",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Text(
                text = "Find joy in your work",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (errorMessage.isNotEmpty()) {
                        Surface(
                            color = Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = errorMessage,
                                color = Color.Red,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    if (step == 1) {
                        CuteTextField(
                            value = accountInput,
                            onValueChange = { accountInput = it; errorMessage = "" },
                            label = if (isPhoneMode) "Phone Number" else "Email / Username",
                            icon = if (isPhoneMode) Icons.Default.Phone else Icons.Default.Email,
                            isPhone = isPhoneMode
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (accountInput.isBlank()) {
                                    errorMessage = "Please enter your details!"
                                } else {
                                    scope.launch {
                                        val existingUser = viewModel.checkUserExists(accountInput)
                                        if (existingUser != null) {
                                            isNewUser = false
                                            nameInput = existingUser.name
                                        } else {
                                            isNewUser = true
                                            nameInput = accountInput
                                        }
                                        step = 2
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryPurple)
                        ) {
                            Text("Next Step ✨", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = softPurple)
                            Text("  or  ", color = Color.Gray, fontSize = 12.sp)
                            HorizontalDivider(modifier = Modifier.weight(1f), color = softPurple)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        OutlinedButton(
                            onClick = { showGoogleDialog = true },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, softPurple)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("G", fontWeight = FontWeight.Black, color = Color(0xFF4285F4), fontSize = 18.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Continue with Google", color = textDark)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(onClick = { 
                            isPhoneMode = !isPhoneMode
                            accountInput = ""
                        }) {
                            Text(
                                text = if (isPhoneMode) "Use Email instead" else "Continue with Phone Number",
                                color = primaryPurple,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (step == 2) {
                        Text(
                            text = if (isNewUser) "Create Account 🌈" else "Welcome back, $nameInput!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textDark,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )

                        if (isNewUser) {
                            CuteTextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                label = "Full Name",
                                icon = Icons.Default.Person
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        CuteTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it; errorMessage = "" },
                            label = "Password",
                            icon = Icons.Default.Lock,
                            isPassword = true
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                if (passwordInput.isBlank()) {
                                    errorMessage = "Password required!"
                                    return@Button
                                }
                                scope.launch {
                                    val result = if (isNewUser) {
                                        viewModel.registerUser(nameInput, accountInput, passwordInput, "Worker")
                                    } else {
                                        viewModel.loginUser(accountInput, passwordInput)
                                    }
                                    if (result.isSuccess) onLoginSuccess() 
                                    else errorMessage = result.exceptionOrNull()?.message ?: "Error"
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryPurple)
                        ) {
                            Text(if (isNewUser) "Register & Go! 🚀" else "Sign In 🌸", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        TextButton(onClick = { step = 1 }) {
                            Text("Go Back", color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            val annotatedAgreement = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)) {
                    append("By joining, you agree to our ")
                }
                pushStringAnnotation(tag = "TERMS", annotation = "terms")
                withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)) {
                    append("Terms")
                }
                pop()
                withStyle(style = SpanStyle(color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)) {
                    append(" & ")
                }
                pushStringAnnotation(tag = "PRIVACY", annotation = "privacy")
                withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)) {
                    append("Privacy")
                }
                pop()
            }

            ClickableText(
                text = annotatedAgreement,
                onClick = { offset ->
                    annotatedAgreement.getStringAnnotations(tag = "TERMS", start = offset, end = offset)
                        .firstOrNull()?.let { showTermsDialog = true }
                    annotatedAgreement.getStringAnnotations(tag = "PRIVACY", start = offset, end = offset)
                        .firstOrNull()?.let { showPrivacyDialog = true }
                },
                style = MaterialTheme.typography.bodySmall.copy(textAlign = TextAlign.Center),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (showGoogleDialog) {
            GoogleAccountDialog(
                onDismiss = { showGoogleDialog = false },
                onAccountSelected = { email, name ->
                    showGoogleDialog = false
                    scope.launch {
                        viewModel.loginWithGoogle(email, name)
                        onLoginSuccess()
                    }
                },
                onAddAnotherAccount = {
                    showGoogleDialog = false
                    showAddGmailDialog = true
                }
            )
        }

        if (showAddGmailDialog) {
            AddGmailDialog(
                onDismiss = { showAddGmailDialog = false },
                onAccountAdded = { email, name ->
                    showAddGmailDialog = false
                    scope.launch {
                        viewModel.loginWithGoogle(email, name)
                        onLoginSuccess()
                    }
                }
            )
        }

        if (showTermsDialog) {
            CuteInfoDialog(
                title = "Terms of Service 📜",
                content = "Welcome to JobBoom! By using our platform, you agree to treat everyone with respect, provide honest information, and use our magic for good. Let's build amazing careers together!",
                onDismiss = { showTermsDialog = false }
            )
        }

        if (showPrivacyDialog) {
            CuteInfoDialog(
                title = "Privacy Policy 🔐",
                content = "We value your trust. Your data is strictly protected and only used to match you with your dream jobs. We never share your magic secrets with third parties.",
                onDismiss = { showPrivacyDialog = false }
            )
        }
    }
}

@Composable
fun GoogleAccountDialog(
    onDismiss: () -> Unit,
    onAccountSelected: (String, String) -> Unit,
    onAddAnotherAccount: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "G",
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF4285F4)
                )
                Text(
                    text = "Choose an account",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1B4B),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                Text(
                    text = "to continue to JobBoom",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                AccountRow("Derrick Tan", "derrick.t@gmail.com", Color(0xFF7E57C2)) {
                    onAccountSelected("derrick.t@gmail.com", "Derrick Tan")
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF3E8FF))
                
                AccountRow("Work Account", "jobboom.pro@gmail.com", Color(0xFF16A34A)) {
                    onAccountSelected("jobboom.pro@gmail.com", "Work Account")
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFF3E8FF))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onAddAnotherAccount() }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = Color(0xFFF3E8FF)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, tint = Color(0xFF7E57C2), modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "Use another account", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF1E1B4B))
                }

                Spacer(modifier = Modifier.height(20.dp))
                
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun AddGmailDialog(
    onDismiss: () -> Unit,
    onAccountAdded: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Sign in with Google", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Enter your Gmail to continue", fontSize = 14.sp, color = Color.Gray)
                
                Spacer(modifier = Modifier.height(24.dp))

                CuteTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Your Name",
                    icon = Icons.Default.Person
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                CuteTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Gmail Address",
                    icon = Icons.Default.Email
                )

                if (error.isNotEmpty()) {
                    Text(text = error, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(top = 8.dp))
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (email.contains("@gmail.com") && name.isNotBlank()) {
                            onAccountAdded(email, name)
                        } else {
                            error = "Please enter a valid Gmail and Name"
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E57C2))
                ) {
                    Text("Continue", fontWeight = FontWeight.Bold)
                }
                
                TextButton(onClick = onDismiss) {
                    Text("Back", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun AccountRow(name: String, email: String, avatarColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = avatarColor
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(name.take(1), color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E1B4B))
            Text(text = email, fontSize = 13.sp, color = Color.Gray)
        }
    }
}

@Composable
fun CuteTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    isPhone: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(label, color = Color.Gray.copy(alpha = 0.5f)) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = Color(0xFF7E57C2)) },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = if (isPhone) KeyboardType.Phone else KeyboardType.Text
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF7E57C2),
            unfocusedBorderColor = Color(0xFFF3E8FF),
            focusedContainerColor = Color(0xFFFDFBFF),
            unfocusedContainerColor = Color(0xFFFDFBFF)
        )
    )
}
