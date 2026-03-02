package com.lankasmartmart.app.presentation.auth

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.lankasmartmart.app.R
import com.lankasmartmart.app.presentation.components.TopographicBackground
import com.lankasmartmart.app.ui.theme.WelcomeScreenGreen
import com.lankasmartmart.app.ui.theme.White
import com.lankasmartmart.app.util.Resource

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRemembered by remember { mutableStateOf(false) }
    
    val loginState by viewModel.loginState.collectAsState()
    val resetPasswordState by viewModel.resetPasswordState.collectAsState() // Observe reset state
    
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }
    
    val context = LocalContext.current

    LaunchedEffect(loginState) {
        when (val state = loginState) {
            is Resource.Success -> {
                if (state.data == true) {
                    Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    val hasSelectedCity = prefs.getBoolean("has_selected_city", false)
                    val hasLocation = prefs.getBoolean("has_location", false)
                    val targetRoute = when {
                        !hasSelectedCity -> "select_city"
                        hasSelectedCity && !hasLocation -> "location_setup"
                        else -> "home"
                    }

                    navController.navigate(targetRoute) {
                        popUpTo("login") { inclusive = true }
                    }
                    viewModel.resetState()
                }
            }
            is Resource.Error -> {
                Toast.makeText(context, state.message ?: "Something went wrong", Toast.LENGTH_LONG).show()
                viewModel.resetState()
            }
            else -> {}
        }
    }

    LaunchedEffect(resetPasswordState) {
        when (val state = resetPasswordState) {
            is Resource.Success -> {
                if (state.data == true) {
                    Toast.makeText(context, "Reset link sent to your email!", Toast.LENGTH_LONG).show()
                    showForgotPasswordDialog = false
                    viewModel.resetPasswordStateReset()
                }
            }
            is Resource.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetPasswordStateReset()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp) 
                .background(WelcomeScreenGreen)
        ) {
            TopographicBackground(
                modifier = Modifier.fillMaxSize(),
                color = White.copy(alpha = 0.1f)
            )
            
            // Wavy Bottom for Header
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                
                // Draw white shape at the bottom to cut out the green
                val path = Path().apply {
                    moveTo(0f, h)
                    lineTo(w, h)
                    lineTo(w, h * 0.65f)
                    
                    // Simple S curve
                    lineTo(w, h * 0.5f) 
                    cubicTo(
                         w * 0.7f, h * 0.85f, 
                         w * 0.3f, h * 0.3f, 
                         0f, h * 0.6f       
                    )
                    close()
                }
                 drawPath(path = path, color = Color.White)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(180.dp))

            // Title
            Column {
                Text(
                    text = "Sign in",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = WelcomeScreenGreen
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Welcome back! Please enter your details to continue.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(4.dp)
                        .background(WelcomeScreenGreen)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Fields
            LoginTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "lankasmartmart@gmail.com",
                icon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            LoginTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = "enter your password",
                icon = Icons.Default.Lock,
                isPassword = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Remember me & Forgot Password
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                 Row(verticalAlignment = Alignment.CenterVertically) {
                    // Custom checkbox color to match green
                    Checkbox(
                         checked = isRemembered, 
                         onCheckedChange = { isRemembered = it },
                         colors = CheckboxDefaults.colors(
                             checkedColor = WelcomeScreenGreen,
                             uncheckedColor = WelcomeScreenGreen,
                             checkmarkColor = Color.White
                         )
                    )
                    Text(
                        text = "Remember Me", 
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                 }

                Text(
                    text = "Forgot Password?",
                    color = WelcomeScreenGreen,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { 
                        if (email.isNotBlank()) forgotPasswordEmail = email
                        showForgotPasswordDialog = true 
                    }
                )
            }
            
            if (showForgotPasswordDialog) {
                AlertDialog(
                    onDismissRequest = { 
                        if (resetPasswordState !is Resource.Loading) {
                            showForgotPasswordDialog = false 
                        }
                    },
                    title = { Text("Reset Password") },
                    text = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text("Enter your email address to receive a password reset link.")
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = forgotPasswordEmail,
                                onValueChange = { forgotPasswordEmail = it },
                                label = { Text("Email") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = resetPasswordState !is Resource.Loading,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = WelcomeScreenGreen,
                                    focusedLabelColor = WelcomeScreenGreen,
                                    cursorColor = WelcomeScreenGreen
                                )
                            )
                            if (resetPasswordState is Resource.Loading) {
                                Spacer(modifier = Modifier.height(16.dp))
                                CircularProgressIndicator(
                                    modifier = Modifier.align(Alignment.CenterHorizontally),
                                    color = WelcomeScreenGreen
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { 
                                viewModel.resetPassword(forgotPasswordEmail)
                            },
                            enabled = forgotPasswordEmail.isNotBlank() && resetPasswordState !is Resource.Loading,
                            colors = ButtonDefaults.buttonColors(containerColor = WelcomeScreenGreen)
                        ) {
                            Text("Send Link")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showForgotPasswordDialog = false },
                            enabled = resetPasswordState !is Resource.Loading
                        ) {
                            Text("Cancel", color = Color.Gray)
                        }
                    },
                    containerColor = Color.White,
                    titleContentColor = Color.Black,
                    textContentColor = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // Login Button
            Button(
                onClick = { viewModel.login(email, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = WelcomeScreenGreen),
                shape = MaterialTheme.shapes.medium,
                enabled = loginState !is Resource.Loading
            ) {
                if (loginState is Resource.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(text = "Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
             // Google Sign-In
            // Google Sign-In
            val token = stringResource(R.string.default_web_client_id)
            val launcher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)
                    account.idToken?.let { idToken ->
                        Toast.makeText(context, "Verifying with Firebase...", Toast.LENGTH_SHORT).show()
                        viewModel.signInWithGoogle(idToken)
                    } ?: run {
                        Toast.makeText(context, "Sign-In Failed: No ID Token retrieved.", Toast.LENGTH_LONG).show()
                    }
                } catch (e: ApiException) {
                    Log.w("Auth", "Google Sign-In API Exception: ${e.statusCode}", e)
                    val message = when(e.statusCode) {
                        7 -> "Network Error: Check internet connection (Error 7)"
                        12500 -> "Sign-In Error: 12500 (Update Google Play Services)"
                        12501 -> "Sign-In Cancelled by User"
                        10 -> "Sign-In Error: 10 (Configuration Error - Check SHA-1)"
                        else -> "Google Sign-In Error: ${e.statusCode}"
                    }
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                } catch (e: Exception) {
                    Log.e("Auth", "Google Sign-In error", e)
                    Toast.makeText(context, "Sign-In Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            
             OutlinedButton(
                onClick = {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(token)
                        .requestEmail()
                        .build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    
                    try {
                        // Launch directly (Faster, allows auto-sign-in if configured)
                        launcher.launch(googleSignInClient.signInIntent)
                    } catch (e: Exception) {
                        Log.e("Auth", "Google Sign-In launch failed", e)
                        Toast.makeText(context, "Launch failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = MaterialTheme.shapes.medium,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray)
            ) {
                 Icon(
                     painter = painterResource(id = R.drawable.ic_google_logo), 
                     contentDescription = "Google Logo",
                     modifier = Modifier.size(24.dp),
                     tint = Color.Unspecified
                 )
                 Spacer(modifier = Modifier.width(8.dp))
                 Text("Sign in with Google", color = Color.Black.copy(alpha = 0.7f))
             }
             
             Spacer(modifier = Modifier.height(24.dp))

             // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Don't have an Account ? ",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Text(
                    text = "Sign up",
                    color = WelcomeScreenGreen,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable {
                        navController.navigate("signup")
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (loginState is Resource.Loading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = WelcomeScreenGreen)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Authenticating...",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = { viewModel.resetState() }) {
                    Text("Cancel", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}


@Composable
private fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isPassword: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Gray
        )
        
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = WelcomeScreenGreen,
                unfocusedIndicatorColor = Color.Gray,
                cursorColor = WelcomeScreenGreen,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            ),
            placeholder = { 
                Text(
                    text = placeholder, 
                    color = Color.LightGray, 
                    fontSize = 14.sp
                ) 
            },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(
                            text = if (passwordVisible) "Hide" else "Show",
                            style = MaterialTheme.typography.labelMedium,
                            color = WelcomeScreenGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = keyboardOptions,
            singleLine = true
        )
    }
}
