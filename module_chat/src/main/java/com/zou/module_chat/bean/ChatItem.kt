package com.zou.module_chat.bean

class ChatItem {
    var title: String? = null
    var content: String? = null
    var timestamp: Long = 0
    var chatid: String? = null
    var chat_type: String? = null
    var unreadCount = 0

    companion object {
        const val CHAT_TYPE_USER = "user"
        const val CHAT_TYPE_GROUP = "group"
    }
}