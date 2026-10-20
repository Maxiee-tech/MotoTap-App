package ke.co.mototap.navigation

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ke.co.mototap.R
import ke.co.mototap.ui.BrandLogo
import ke.co.mototap.ui.MotoButtonShape
import ke.co.mototap.ui.theme.MotoInk
import ke.co.mototap.ui.theme.MotoMuted
import ke.co.mototap.ui.theme.MotoRed
import ke.co.mototap.ui.theme.MotoScreen

@Composable
fun UserSelectionScreen(
    onRequestService: () -> Unit,
    onProvideService: () -> Unit,
    onProvideParts: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MotoScreen)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        BrandLogo(modifier = Modifier.height(32.dp).fillMaxWidth(0.5f))
        Text(
            text = stringResource(id = R.string.i_want_to),
            style = MaterialTheme.typography.headlineSmall.copy(
                color = MotoInk,
                fontWeight = FontWeight.ExtraBold
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 28.dp, bottom = 8.dp)
        )
        Text(
            text = "Choose how you use MotoTap",
            color = MotoMuted,
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Button(
            onClick = onRequestService,
            colors = ButtonDefaults.buttonColors(containerColor = MotoRed),
            shape = MotoButtonShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Request Service (Driver)",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onProvideService,
            shape = MotoButtonShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Provide Service (Mechanic)",
                color = MotoInk,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onProvideParts,
            shape = MotoButtonShape,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = "Provide Parts (Parts Dealer)",
                color = MotoInk,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
    }
}
