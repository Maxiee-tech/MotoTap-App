package ke.co.mototap.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ke.co.mototap.R
import ke.co.mototap.ui.theme.MotoRed

object LegalUrls {
    const val PRIVACY = "https://mototap.co.ke/privacy-policy"
    const val TERMS = "https://mototap.co.ke/terms"
    const val DELETE_ACCOUNT = "https://mototap.co.ke/delete-account"
}

object ContactInfo {
    const val SLOGAN = "TAP IT • FIX IT • GO WITH CONFIDENCE"
    const val HEADING = "Connect with us"
    const val COMPANY = "MotoTap Autosystems, Nairobi, Kenya"
    const val OFFICE_ADDRESS = "The Mall, Westlands"
    const val OFFICE_LAT = -1.264376580170016
    const val OFFICE_LNG = 36.80267270222807
    const val OFFICE_MAP_EMBED =
        "https://www.google.com/maps/embed?pb=!1m18!1m12!1m3!1d260.83752933780715!2d36.80267270222807!3d-1.264376580170016!2m3!1f0!2f0!3f0!3m2!1i1024!2i768!4f13.1!3m3!1m2!1s0x182f17401ef1b0db%3A0xcc0b439a0875f500!2sThe%20Mall%2C%20Westlands!5e0!3m2!1sen!2ske!4v1792493778236!5m2!1sen!2ske"
    const val EMAIL_SUPPORT = "support@mototap.co.ke"
    const val EMAIL_LAB = "themechlabcompany@gmail.com"
    const val PHONE_1 = "0758677968"
    const val PHONE_1_E164 = "+254758677968"
    const val PHONE_2 = "0101452886"
    const val PHONE_2_E164 = "+254101452886"
    const val WHATSAPP_1 = "https://wa.me/+254758677968"
    const val WHATSAPP_2 = "https://wa.me/+254101452886"
    const val INSTAGRAM = "https://www.instagram.com/mototap_autosystems"
    const val X = "https://x.com/mototap01"
    const val THREADS = "https://www.threads.net/@mototap_autosystems"
}

@Composable
fun LegalPolicyLinks(
    modifier: Modifier = Modifier,
    textColor: Color = Color.Gray,
    showDeleteLink: Boolean = true,
) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val annotated = buildAnnotatedString {
            append("By continuing you agree to our ")
            pushStringAnnotation("terms", LegalUrls.TERMS)
            pushStyle(SpanStyle(color = MotoRed, textDecoration = TextDecoration.Underline))
            append("Terms")
            pop()
            pop()
            append(" and ")
            pushStringAnnotation("privacy", LegalUrls.PRIVACY)
            pushStyle(SpanStyle(color = MotoRed, textDecoration = TextDecoration.Underline))
            append("Privacy Policy")
            pop()
            pop()
            append(".")
        }
        ClickableText(
            text = annotated,
            style = androidx.compose.ui.text.TextStyle(
                color = textColor,
                textAlign = TextAlign.Center,
                fontSize = 12.sp
            ),
            modifier = Modifier.fillMaxWidth(),
            onClick = { offset ->
                annotated.getStringAnnotations(offset, offset).firstOrNull()?.let {
                    uriHandler.openUri(it.item)
                }
            },
        )
        if (showDeleteLink) {
            Spacer(modifier = Modifier.height(4.dp))
            TextButton(onClick = { uriHandler.openUri(LegalUrls.DELETE_ACCOUNT) }) {
                Text(
                    text = stringResource(R.string.legal_delete_account_web),
                    color = textColor,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
