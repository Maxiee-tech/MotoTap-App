package ke.co.mototap.core.notify

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import org.json.JSONObject

object AppNotificationCenter {
    private const val PREFS = "mototap_in_app_notifications"
    private const val MAX_ITEMS = 80

    private val _items = MutableStateFlow<List<AppNotification>>(emptyList())
    val items: StateFlow<List<AppNotification>> = _items.asStateFlow()

    private var prefs: SharedPreferences? = null
    private var userId: String = ""

    fun attach(context: Context, uid: String?) {
        prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val next = uid.orEmpty()
        if (next == userId) return
        userId = next
        _items.value = if (next.isBlank()) emptyList() else load()
    }

    fun upsert(notification: AppNotification) {
        if (userId.isBlank()) return
        val current = _items.value.toMutableList()
        val index = current.indexOfFirst { it.id == notification.id }
        if (index >= 0) {
            val existing = current[index]
            val newer = notification.createdAtMillis > existing.createdAtMillis
            current[index] = existing.copy(
                title = notification.title,
                body = notification.body,
                targetId = notification.targetId.ifBlank { existing.targetId },
                extra = notification.extra.ifBlank { existing.extra },
                createdAtMillis = maxOf(existing.createdAtMillis, notification.createdAtMillis),
                read = if (newer) notification.read else existing.read,
            )
        } else {
            current.add(0, notification)
        }
        _items.value = current
            .sortedByDescending { it.createdAtMillis }
            .take(MAX_ITEMS)
        persist()
    }

    fun markRead(id: String) {
        if (userId.isBlank()) return
        var changed = false
        _items.value = _items.value.map {
            if (it.id == id && !it.read) {
                changed = true
                it.copy(read = true)
            } else it
        }
        if (changed) persist()
    }

    fun markAllRead() {
        if (userId.isBlank()) return
        if (_items.value.none { !it.read }) return
        _items.value = _items.value.map { it.copy(read = true) }
        persist()
    }

    fun markReadWhere(predicate: (AppNotification) -> Boolean) {
        if (userId.isBlank()) return
        var changed = false
        _items.value = _items.value.map {
            if (!it.read && predicate(it)) {
                changed = true
                it.copy(read = true)
            } else it
        }
        if (changed) persist()
    }

    private fun key() = "items_$userId"

    private fun persist() {
        val array = JSONArray()
        _items.value.forEach { n ->
            array.put(
                JSONObject()
                    .put("id", n.id)
                    .put("title", n.title)
                    .put("body", n.body)
                    .put("createdAtMillis", n.createdAtMillis)
                    .put("kind", n.kind.name)
                    .put("targetId", n.targetId)
                    .put("extra", n.extra)
                    .put("read", n.read),
            )
        }
        prefs?.edit()?.putString(key(), array.toString())?.apply()
    }

    private fun load(): List<AppNotification> {
        val raw = prefs?.getString(key(), null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (i in 0 until array.length()) {
                    val o = array.getJSONObject(i)
                    add(
                        AppNotification(
                            id = o.optString("id"),
                            title = o.optString("title"),
                            body = o.optString("body"),
                            createdAtMillis = o.optLong("createdAtMillis"),
                            kind = runCatching {
                                AppNotificationKind.valueOf(o.optString("kind"))
                            }.getOrDefault(AppNotificationKind.JOB),
                            targetId = o.optString("targetId"),
                            extra = o.optString("extra"),
                            read = o.optBoolean("read"),
                        ),
                    )
                }
            }.sortedByDescending { it.createdAtMillis }
        }.getOrDefault(emptyList())
    }
}
