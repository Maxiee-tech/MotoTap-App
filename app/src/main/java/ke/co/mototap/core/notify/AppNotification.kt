package ke.co.mototap.core.notify

enum class AppNotificationKind {
    MESSAGE,
    JOB,
    INCOMING_REQUEST,
}

data class AppNotification(
    val id: String,
    val title: String,
    val body: String,
    val createdAtMillis: Long,
    val kind: AppNotificationKind,
    val targetId: String,
    val extra: String = "",
    val read: Boolean = false,
)
