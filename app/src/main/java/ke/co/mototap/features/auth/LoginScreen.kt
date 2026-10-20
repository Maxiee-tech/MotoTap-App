package ke.co.mototap.features.auth

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ke.co.mototap.R
import ke.co.mototap.core.ui.LegalPolicyLinks
import ke.co.mototap.ui.BrandLogo
import ke.co.mototap.ui.MotoButtonShape
import ke.co.mototap.ui.motoTextFieldColors
import ke.co.mototap.ui.theme.MotoInk
import ke.co.mototap.ui.theme.MotoMuted
import ke.co.mototap.ui.theme.MotoRed

private enum class LoginMethod { GOOGLE, PHONE, EMAIL }

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginSuccess: (String?) -> Unit,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit = {},
    onBack: () -> Unit = {},
    connectWithName: String? = null,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val phoneNumber by viewModel.phoneNumber.collectAsState()
    val phoneVerificationId by viewModel.phoneVerificationId.collectAsState()
    val context = LocalContext.current
    var passwordVisible by remember { mutableStateOf(false) }
    var method by remember { mutableStateOf<LoginMethod?>(null) }
    var smsCode by remember { mutableStateOf("") }

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

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding(),
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = maxHeight)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 56.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
        BrandLogo(modifier = Modifier.height(32.dp).fillMaxWidth(0.5f))

        Text(
            text = stringResource(id = R.string.login_title),
            color = MotoInk,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 28.dp),
        )
        Text(
            text = if (!connectWithName.isNullOrBlank()) {
                "Sign in to connect with $connectWithName"
            } else {
                stringResource(id = R.string.login_subtitle)
            },
            color = MotoMuted,
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 28.dp),
        )

        when (method) {
            null -> {
                ContinueMethodButton(
                    text = stringResource(id = R.string.continue_with_google),
                    icon = Icons.Default.AccountCircle,
                    enabled = uiState !is AuthUiState.Loading,
                    onClick = { viewModel.continueWithGoogle(context, creatingAccount = false) },
                )
                AuthOrDivider(modifier = Modifier.padding(vertical = 16.dp))
                ContinueMethodButton(
                    text = stringResource(id = R.string.continue_with_phone),
                    icon = Icons.Default.Phone,
                    enabled = uiState !is AuthUiState.Loading,
                    onClick = { method = LoginMethod.PHONE },
                )
                Spacer(modifier = Modifier.height(12.dp))
                ContinueMethodButton(
                    text = stringResource(id = R.string.continue_with_email),
                    icon = Icons.Default.Email,
                    enabled = uiState !is AuthUiState.Loading,
                    onClick = { method = LoginMethod.EMAIL },
                )
            }
            LoginMethod.EMAIL -> {
                OutlinedTextField(
                    value = email,
                    onValueChange = { viewModel.email.value = it },
                    label = { Text("Email") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MotoButtonShape,
                    colors = motoTextFieldColors(),
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { viewModel.password.value = it },
                    label = { Text("Password") },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = null,
                                tint = MotoMuted,
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MotoButtonShape,
                    colors = motoTextFieldColors(),
                )
                TextButton(onClick = onNavigateToForgotPassword) {
                    Text(stringResource(id = R.string.login_forgot_password), color = MotoRed)
                }
                Button(
                    onClick = { viewModel.signIn() },
                    enabled = uiState !is AuthUiState.Loading && email.isNotBlank() && password.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
                    shape = MotoButtonShape,
                ) {
                    if (uiState is AuthUiState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                    } else {
                        Text("Sign in", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
            LoginMethod.PHONE -> {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { viewModel.phoneNumber.value = it },
                    label = { Text("Phone Number") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    shape = MotoButtonShape,
                    colors = motoTextFieldColors(),
                )
                if (!phoneVerificationId.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = smsCode,
                        onValueChange = { smsCode = it.filter { ch -> ch.isDigit() }.take(8) },
                        label = { Text("SMS code") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = MotoButtonShape,
                        colors = motoTextFieldColors(),
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        val activity = context as? Activity
                        if (activity == null) return@Button
                        if (phoneVerificationId.isNullOrBlank()) {
                            viewModel.startPhoneSignIn(activity, creatingAccount = false)
                        } else {
                            viewModel.confirmPhoneSignIn(smsCode, creatingAccount = false)
                        }
                    },
                    enabled = uiState !is AuthUiState.Loading && phoneNumber.isNotBlank() &&
                        (phoneVerificationId.isNullOrBlank() || smsCode.length >= 4),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
                    shape = MotoButtonShape,
                ) {
                    if (uiState is AuthUiState.Loading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                    } else {
                        Text(
                            text = if (phoneVerificationId.isNullOrBlank()) "Send code" else "Verify and sign in",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                        )
                    }
                }
            }
            LoginMethod.GOOGLE -> { }
        }

        if (uiState is AuthUiState.Error) {
            Text(
                text = (uiState as AuthUiState.Error).message,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        LegalPolicyLinks(
            modifier = Modifier.padding(top = 24.dp),
            showDeleteLink = true,
        )

        TextButton(
            onClick = {
                viewModel.resetState()
                onNavigateToSignUp()
            },
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
        ) {
            Text(
                text = stringResource(id = R.string.login_create_account),
                color = MotoMuted,
                textAlign = TextAlign.Center,
            )
            }
            }
        }

        IconButton(
            onClick = {
                if (method != null) {
                    method = null
                    smsCode = ""
                    viewModel.phoneVerificationId.value = null
                    viewModel.resetUiState()
                } else {
                    onBack()
                }
            },
            modifier = Modifier.align(Alignment.TopStart),
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MotoInk)
        }
    }
}

@Composable
fun ContinueMethodButton(
    text: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = MotoButtonShape,
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MotoInk),
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MotoRed)
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}
