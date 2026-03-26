package com.example.mototap.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mototap.R
import com.example.mototap.ui.theme.MotoRed
import com.google.firebase.auth.FirebaseAuth

@Composable
fun SplashScreen(
    onGetStarted: () -> Unit,
    onAutoNavigate: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Check for existing session on splash
    LaunchedEffect(Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // If already logged in, navigate after a short delay
            // Role fetching will happen in the NavHost via repository
            onAutoNavigate(currentUser.uid)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "MOTOTAP",
            style = MaterialTheme.typography.headlineLarge.copy(
                color = MotoRed,
                fontWeight = FontWeight.Bold,
                letterSpacing = 4.sp
            ),
            textAlign = TextAlign.Center
        )
        
        Text(
            text = stringResource(id = R.string.splash_tagline),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = Color.White,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onGetStarted,
            colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
            shape = MaterialTheme.shapes.extraSmall,
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(48.dp)
        ) {
            Text(
                text = stringResource(id = R.string.get_started),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
