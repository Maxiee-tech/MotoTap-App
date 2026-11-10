package ke.co.mototap.core.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import ke.co.mototap.ui.theme.MotoRed

fun Context.openExternalUri(uriString: String) {
    val raw = uriString.trim()
    if (raw.isEmpty()) return
    val uri = Uri.parse(raw)
    val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try {
        startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(this, "No app available to open this link", Toast.LENGTH_SHORT).show()
    } catch (_: Exception) {
        Toast.makeText(this, "Could not open link", Toast.LENGTH_SHORT).show()
    }
}

private val URL_REGEX = Regex(
    """(?i)\b((?:https?://|www\.)[^\s<>]+|[a-z0-9._%+\-]+@[a-z0-9.\-]+\.[a-z]{2,})""",
)

@Composable
fun LinkifiedText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color,
    style: TextStyle = LocalTextStyle.current,
    linkColor: Color = MotoRed,
) {
    val context = LocalContext.current
    val annotated = buildAnnotatedString {
        var last = 0
        URL_REGEX.findAll(text).forEach { match ->
            val value = match.value.trimEnd('.', ',', ';', ')', ']')
            if (match.range.first > last) {
                append(text.substring(last, match.range.first))
            }
            val uri = when {
                value.contains("@") && !value.startsWith("http", ignoreCase = true) -> "mailto:$value"
                value.startsWith("www.", ignoreCase = true) -> "https://$value"
                else -> value
            }
            pushStringAnnotation("url", uri)
            withStyle(SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)) {
                append(value)
            }
            pop()
            last = match.range.first + value.length
        }
        if (last < text.length) append(text.substring(last))
    }
    ClickableText(
        text = annotated,
        modifier = modifier,
        style = style.copy(color = color),
        onClick = { offset ->
            annotated.getStringAnnotations("url", offset, offset).firstOrNull()?.let {
                context.openExternalUri(it.item)
            }
        },
    )
}
