package ke.co.mototap.navigation

import androidx.compose.foundation.Image
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ke.co.mototap.R
import ke.co.mototap.ui.MotoButtonShape
import ke.co.mototap.ui.theme.MotoInk
import ke.co.mototap.ui.theme.MotoMuted
import ke.co.mototap.ui.theme.MotoRed

@Composable
fun SplashScreen(
    onFindMechanic: () -> Unit,
    onListBusiness: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.brand_logo_wide),
            contentDescription = "MotoTap",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth(0.86f),
        )

        Text(
            text = stringResource(id = R.string.splash_tagline),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = MotoMuted,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp),
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onFindMechanic,
            colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
            shape = MotoButtonShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Find a mechanic",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onListBusiness,
            shape = MotoButtonShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "List my garage or shop",
                color = MotoInk,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
