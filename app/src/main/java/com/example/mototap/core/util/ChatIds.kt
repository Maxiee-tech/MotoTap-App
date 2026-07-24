package com.example.mototap.core.util

/** Room-ID helpers matching mototap_web `js/utils/geo.js`. */
object ChatIds {
    const val MAX_MESSAGE_LENGTH = 200

    /** Canonical room id used by the website (sorted UIDs). */
    fun roomId(userIdA: String, userIdB: String): String {
        val sorted = listOf(userIdA, userIdB).sorted()
        return "chat_${sorted[0]}_${sorted[1]}"
    }

    /** Every conversation id variant that may hold the same thread (web + legacy Android). */
    fun allRoomIds(userIdA: String, userIdB: String): List<String> {
        val sorted = roomId(userIdA, userIdB)
        val forward = "chat_${userIdA}_${userIdB}"
        val reverse = "chat_${userIdB}_${userIdA}"
        return listOf(sorted, forward, reverse).distinct()
    }

    fun partnerIdFromRoomId(roomId: String, currentUserId: String): String? {
        val parts = roomId.trim().split("_")
        if (parts.size < 3 || parts[0] != "chat") return null
        val a = parts[1]
        val b = parts[2]
        return when (currentUserId) {
            a -> b
            b -> a
            else -> null
        }
    }
}
