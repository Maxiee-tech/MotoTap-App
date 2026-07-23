package com.example.mototap.features.auth

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mototap.R
import com.example.mototap.ui.theme.MotoRed

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (String?) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    // Key fix: Use current state directly in LaunchedEffect to trigger navigation
    LaunchedEffect(uiState) {
        Log.d("LoginScreen", "uiState change detected: $uiState")
        if (uiState is AuthUiState.Success) {
            val success = uiState as AuthUiState.Success
            Log.d("LoginScreen", "Success detected, role: ${success.role}, resumeSignUp: ${success.resumeSignUp}")
            if (success.resumeSignUp) {
                onNavigateToSignUp()
                viewModel.resetUiState()
            } else {
                onLoginSuccess(success.role)
                viewModel.resetState()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "MOTO TAP",
            style = MaterialTheme.typography.headlineLarge.copy(
                color = MotoRed,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            ),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = stringResource(id = R.string.login_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = stringResource(id = R.string.login_subtitle),
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
        )

        OutlinedTextField(
            value = email,
            onValueChange = { viewModel.email.value = it },
            label = { Text("Email", color = Color.Gray) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = MotoRed,
                unfocusedBorderColor = Color.Gray
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { viewModel.password.value = it },
            label = { Text("Password", color = Color.Gray) },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = description, tint = Color.Gray)
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = MotoRed,
                unfocusedBorderColor = Color.Gray
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(
                onClick = onNavigateToForgotPassword,
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = stringResource(id = R.string.login_forgot_password),
                    color = MotoRed,
                    fontSize = 14.sp
                )
            }
        }

        Button(
            onClick = { viewModel.signIn() },
            enabled = uiState !is AuthUiState.Loading && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
            shape = MaterialTheme.shapes.extraSmall
        ) {
            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = stringResource(id = R.string.login_button),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        if (uiState is AuthUiState.Error) {
            Text(
                text = (uiState as AuthUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        AuthOrDivider(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp))

        TextButton(
            onClick = {
                viewModel.resetState()
                onNavigateToSignUp()
            },
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text(
                text = stringResource(id = R.string.login_create_account),
                color = MotoRed
            )
        }
    }
}
