package ke.co.mototap.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ke.co.mototap.R
import ke.co.mototap.ui.theme.MotoCardLine
import ke.co.mototap.ui.theme.MotoInk
import ke.co.mototap.ui.theme.MotoMuted
import ke.co.mototap.ui.theme.MotoRed

val MotoButtonShape = RoundedCornerShape(16.dp)
val MotoCardShape = RoundedCornerShape(18.dp)

@Composable
fun BrandLogo(
    modifier: Modifier = Modifier,
) {
    Image(
        painter = painterResource(id = R.drawable.brand_logo_wide),
        contentDescription = "MotoTap",
        contentScale = ContentScale.Fit,
        modifier = modifier.height(28.dp),
    )
}

@Composable
fun BrandLogoRow(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BrandLogo()
    }
}

@Composable
fun motoTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    focusedTextColor = MotoInk,
    unfocusedTextColor = MotoInk,
    focusedBorderColor = MotoRed,
    unfocusedBorderColor = MotoCardLine,
    cursorColor = MotoRed,
    focusedLabelColor = MotoMuted,
    unfocusedLabelColor = MotoMuted,
    focusedContainerColor = androidx.compose.ui.graphics.Color.White,
    unfocusedContainerColor = androidx.compose.ui.graphics.Color.White,
)
