package com.example.myapplication.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.MainViewModel

@Composable
fun LoginRegisterScreen(
    viewModel: MainViewModel,
    onLoginSuccess: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }

    var accountInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf("") }
    var successMessage by remember { mutableStateOf("") }
    var isNewUser by remember { mutableStateOf(false) }

    var showTermsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    val primaryOrange = Color(0xFFC725F5)
    val textDark = Color(0xFF030100)
    val textSub = Color(0xFF64748B)
    val bgLight = Color(0xFFF8FAFC)
    val borderLight = Color(0xFFE2E8F0)

    val inputColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = textDark,
        unfocusedTextColor = textDark,
        focusedBorderColor = primaryOrange,
        unfocusedBorderColor = borderLight,
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgLight)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            IconButton(
                onClick = {
                    step = 1
                    accountInput = ""
                    passwordInput = ""
                    nameInput = ""
                    errorMessage = ""
                    successMessage = ""
                },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Welcome to JobBoom",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = textDark
            )

            Spacer(modifier = Modifier.height(28.dp))

            if (errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }
            if (successMessage.isNotEmpty()) {
                Text(
                    text = successMessage,
                    color = Color(0xFF16A34A),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            if (step == 1) {
                Text(
                    text = "Email / Username",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = textSub,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = accountInput,
                    onValueChange = {
                        accountInput = it
                        errorMessage = ""
                    },
                    placeholder = { Text("Please Enter Your Email or Username", color = Color(0xFF94A3B8)) },
                    colors = inputColors,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        if (accountInput.isBlank()) {
                            errorMessage = "Please Enter Your Email or Username！"
                        } else {
                            errorMessage = ""
                            val registeredList = viewModel.registeredUsers.value
                            val existing = registeredList.find {
                                it.email.equals(accountInput.trim(), ignoreCase = true) ||
                                        it.name.equals(accountInput.trim(), ignoreCase = true)
                            }

                            if (existing != null) {
                                isNewUser = false
                                nameInput = existing.name
                            } else {
                                isNewUser = true
                                nameInput = accountInput.trim()
                            }
                            step = 2
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryOrange)
                ) {
                    Text("Next", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = borderLight)
                    Text(
                        text = "  or  ",
                        fontSize = 13.sp,
                        color = Color(0xFF94A3B8)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = borderLight)
                }

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.loginUser("Google User", "123456", "Worker")
                        onLoginSuccess()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, borderLight)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "G ",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF839D12)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Continue use Google account", fontSize = 15.sp, color = textDark, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.loginUser("Facebook User", "123456", "Worker")
                        onLoginSuccess()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, borderLight)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "f ",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF1877F2)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Continue use Facebook account", fontSize = 15.sp, color = textDark, fontWeight = FontWeight.Medium)
                    }
                }
            }

            if (step == 2) {
                Text(
                    text = if (isNewUser) "New User Registration" else "Welcome back: $nameInput",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryOrange,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                if (isNewUser) {
                    Text("Full name / Username", fontSize = 14.sp, color = textSub, modifier = Modifier.padding(bottom = 6.dp))
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        colors = inputColors,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text("Password", fontSize = 14.sp, color = textSub, modifier = Modifier.padding(bottom = 6.dp))
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it; errorMessage = "" },
                    placeholder = { Text("Please enter your password", color = Color(0xFF94A3B8)) },
                    colors = inputColors,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (passwordInput.isBlank()) {
                            errorMessage = "Please enter your password. / Password is required."
                            return@Button
                        }

                        if (isNewUser) {
                            val regResult = viewModel.registerUser(nameInput, accountInput, passwordInput, "Worker")
                            regResult.onSuccess {
                                viewModel.loginUser(accountInput, passwordInput, "Worker")
                                onLoginSuccess()
                            }.onFailure {
                                errorMessage = it.message ?: "Registration failed. Please try again"
                            }
                        } else {
                            val loginResult = viewModel.loginUser(accountInput, passwordInput, "Worker")
                            loginResult.onSuccess {
                                onLoginSuccess()
                            }.onFailure {
                                errorMessage = it.message ?: "Incorrect password or account not found！"
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryOrange)
                ) {
                    Text(
                        text = if (isNewUser) "Confirm Registration & Log In" else "Confirm & Sign In",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = { step = 1; errorMessage = "" },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Back to edit email / account", color = textSub, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            val annotatedAgreement = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color(0xFF94A3B8), fontSize = 11.sp)) {
                    append("Logging in indicates your acceptance ")
                }
                pushStringAnnotation(tag = "TERMS", annotation = "terms")
                withStyle(style = SpanStyle(color = primaryOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)) {
                    append("Terms of Service")
                }
                pop()
                withStyle(style = SpanStyle(color = Color(0xFF94A3B8), fontSize = 11.sp)) {
                    append(" & ")
                }
                pushStringAnnotation(tag = "PRIVACY", annotation = "privacy")
                withStyle(style = SpanStyle(color = primaryOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)) {
                    append("Privacy Policy")
                }
                pop()
            }

            ClickableText(
                text = annotatedAgreement,
                onClick = { offset ->
                    annotatedAgreement.getStringAnnotations(tag = "TERMS", start = offset, end = offset)
                        .firstOrNull()?.let {
                            showTermsDialog = true
                        }
                    annotatedAgreement.getStringAnnotations(tag = "PRIVACY", start = offset, end = offset)
                        .firstOrNull()?.let {
                            showPrivacyDialog = true
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                style = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }


        if (showTermsDialog) {
            AlertDialog(
                onDismissRequest = { showTermsDialog = false },
                containerColor = Color.White,
                title = { Text("Terms of Service", fontWeight = FontWeight.Bold, color = textDark) },
                text = {
                    Text(
                        "By using the JobBoom platform, you agree to provide accurate personal and recruitment information and to comply with the platform's Job Seeker Agreement.",
                        fontSize = 14.sp,
                        color = textDark
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showTermsDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryOrange)
                    ) {
                        Text("Agree", color = Color.White)
                    }
                }
            )
        }

        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                containerColor = Color.White,
                title = { Text("Privacy Policy", fontWeight = FontWeight.Bold, color = textDark) },
                text = {
                    Text(
                        "We value your privacy. Your personal information is used exclusively for job matching and application tracking, and will never be shared with third parties.",
                        fontSize = 14.sp,
                        color = textDark
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showPrivacyDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryOrange)
                    ) {
                        Text("Learn More", color = Color.White)
                    }
                }
            )
        }
    }
}