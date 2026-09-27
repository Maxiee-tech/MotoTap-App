package com.example.mototap.core.ui

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
import com.example.mototap.R
import com.example.mototap.ui.theme.MotoRed

object LegalUrls {
    const val PRIVACY = "https://mototap.co.ke/privacy-policy"
    const val TERMS = "https://mototap.co.ke/terms"
    const val DELETE_ACCOUNT = "https://mototap.co.ke/delete-account"
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
