package com.zou.module_chat.utils

import com.google.gson.reflect.TypeToken
import com.ty.web3mq.utils.DefaultSPHelper
import com.zou.module_chat.bean.ChatItem
import java.lang.reflect.Type
import java.util.ArrayList

object Tools {
    //    public static void saveMessageItemList(String chatId, ArrayList<MessageItem> messageItems){
    //        DefaultSPHelper.getInstance().put(chatId,messageItems);
    //    }
    //
    //    public static ArrayList<MessageItem> getMessageItemList(String chatId){
    //        Type listType = new TypeToken<ArrayList<MessageItem>>(){}.getType();
    //        ArrayList<MessageItem> itemList = (ArrayList<MessageItem>) DefaultSPHelper.getInstance().getObject(chatId,listType);
    //        return itemList;
    //    }
    fun saveChatItemList(chatItems: ArrayList<ChatItem>?) {
        DefaultSPHelper.put("ChatItem", chatItems)
    }

    val chatItemList: ArrayList<ChatItem>?
        get() {
            val listType: Type =
                object : TypeToken<ArrayList<ChatItem?>?>() {}.getType()
            if (DefaultSPHelper.getObject("ChatItem", listType)==null){
                return null
            }
            return  DefaultSPHelper.getObject("ChatItem", listType) as ArrayList<ChatItem>
        }

    // call in message activity
    fun updateChatItem(chatId: String?, content: String, timestamp: Long, unReadCount: Int) {
        val chatItems: ArrayList<ChatItem>? = chatItemList
        if (chatItems == null || chatItems.size == 0) {
            return
        }
        for (i in chatItems.indices) {
            val item: ChatItem = chatItems[i]
            if (item.chatid.equals(chatId)) {
                item.content = content
                item.unreadCount = unReadCount
                item.timestamp = timestamp
            }
        }
        saveChatItemList(chatItems)
    }
}