package ke.co.mototap.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ke.co.mototap.R
import ke.co.mototap.ui.BrandLogo
import ke.co.mototap.ui.MotoButtonShape
import ke.co.mototap.ui.motoTextFieldColors
import ke.co.mototap.ui.theme.MotoInk
import ke.co.mototap.ui.theme.MotoRed
import ke.co.mototap.ui.theme.MotoScreen

@Composable
fun AuthOrDivider(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Color.Gray.copy(alpha = 0.4f)
        )
        Text(
            text = stringResource(id = R.string.login_or_divider),
            color = Color.Gray,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 12.dp)
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = Color.Gray.copy(alpha = 0.4f)
        )
    }
}

@Composable
fun ContinueWithGoogleButton(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ContinueMethodButton(
        text = stringResource(id = R.string.continue_with_google),
        icon = Icons.Default.AccountCircle,
        enabled = enabled,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val email by viewModel.email.collectAsState()
    var successMessage by remember { mutableStateOf<String?>(null) }
    val successText = stringResource(id = R.string.forgot_password_success)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MotoScreen)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BrandLogo(modifier = Modifier.height(36.dp).fillMaxWidth(0.55f))
        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = stringResource(id = R.string.forgot_password_title),
            style = MaterialTheme.typography.headlineMedium.copy(
                color = MotoInk,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = stringResource(id = R.string.forgot_password_subtitle),
            style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
        )

        OutlinedTextField(
            value = email,
            onValueChange = {
                viewModel.email.value = it
                successMessage = null
            },
            label = { Text("Email", color = Color.Gray) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = MotoButtonShape,
            colors = motoTextFieldColors(),
        )

        Button(
            onClick = {
                successMessage = null
                viewModel.sendPasswordReset {
                    successMessage = successText
                }
            },
            enabled = uiState !is AuthUiState.Loading && email.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp)
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
            shape = MotoButtonShape
        ) {
            if (uiState is AuthUiState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = stringResource(id = R.string.forgot_password_button),
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

        successMessage?.let { message ->
            Text(
                text = message,
                color = Color(0xFF4CAF50),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp),
            )
        }

        AuthOrDivider(modifier = Modifier.padding(vertical = 16.dp))

        TextButton(onClick = {
            viewModel.resetUiState()
            onBackToLogin()
        }) {
            Text(
                text = stringResource(id = R.string.forgot_password_back),
                color = MotoRed
            )
        }
    }
}
