package com.zou.module_chat.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ty.module_common.config.Constants
import com.ty.module_common.fragment.BaseFragment
import com.ty.module_common.view.Web3MQListView
import com.ty.web3mq.Web3MQChats
import com.ty.web3mq.Web3MQMessageManager
import com.ty.web3mq.http.beans.ChatsBean
import com.ty.web3mq.interfaces.ChatsMessageCallback
import com.ty.web3mq.interfaces.GetChatsCallback
import com.zou.module_chat.R
import com.zou.module_chat.activity.MessageActivity
import com.zou.module_chat.adapter.ChatsAdapter
import com.zou.module_chat.bean.ChatItem
import com.zou.module_chat.utils.Tools
import web3mq.Message

//import web3mq.Message;
object ChatsFragment : BaseFragment(), ChatsMessageCallback {
    private val TAG = "ChatsFragment"
    private val INIT_CHATS_SIZE = 100
    private var chats: ArrayList<ChatItem> = ArrayList<ChatItem>()
    private var chatsAdapter: ChatsAdapter? = null
    private var recycler_view_chats: Web3MQListView? = null
    private var toNewMessageListener: ToNewMessageListener? = null
    private var iv_new_message: ImageView? = null
    private var hidden = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(R.layout.fragment_chats)
    }

    override fun onBaseCreateView() {
        super.onBaseCreateView()
        initView()
        setListener()

    }

    override fun onResume() {
        super.onResume()
        requestData()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        Log.i(TAG, "hidden:$hidden")
        super.onHiddenChanged(hidden)
        this.hidden = hidden
        if(!hidden){
            requestData()
        }
    }



    fun setToNewMessageListener(toNewMessageListener: ToNewMessageListener?) {
        this.toNewMessageListener = toNewMessageListener
    }

    override fun onMessage(message: Message.Web3MQMessage) {
        Log.i(TAG, "MessageType:" + message.getMessageType())
        Log.i(TAG, "MessageId:" + message.getMessageId())
        Log.i(TAG, "MessageType:" + message.getMessageType())
        Log.i(TAG, "ComeFrom:" + message.getComeFrom())
        Log.i(TAG, "Payload:" + message.getPayload().toStringUtf8())
        Log.i(TAG, "PayloadType:" + message.getPayloadType())
        Log.i(TAG, "ContentTopic:" + message.getContentTopic())
        var exist = false
        for (i in chats.indices) {
            val chatItem: ChatItem = chats[i]
            when (chatItem.chat_type) {
                ChatItem.CHAT_TYPE_USER -> if (message.getComeFrom().equals(chatItem.chatid) && message.getContentTopic().startsWith("user:")) {
                    chatItem.unreadCount += 1
                    chatItem.content = message.getPayload().toStringUtf8()
                    chatItem.timestamp = message.getTimestamp()
                    chatsAdapter!!.notifyItemChanged(i)
                    //                        Tools.updateChatItem(chatItem.chatid,chatItem.content,chatItem.timestamp,chatItem.unreadCount);
                    exist = true
                }
                ChatItem.CHAT_TYPE_GROUP -> if (message.getContentTopic().equals(chatItem.chatid) && message.getContentTopic().startsWith("group:")) {
                    chatItem.unreadCount += 1
                    chatItem.content = message.getPayload().toStringUtf8()
                    chatItem.timestamp = message.getTimestamp()
                    chatsAdapter!!.notifyItemChanged(i)
                    //                        Tools.updateChatItem(chatItem.chatid,chatItem.content,chatItem.timestamp,chatItem.unreadCount);
                    exist = true
                }
            }
        }
        if (!exist) {
            requestData()
        }
        Tools.saveChatItemList(chats)
    }

    interface ToNewMessageListener {
        fun toNewMessageModule()
    }

    private fun requestData() {
        Web3MQChats.getChats(1, INIT_CHATS_SIZE, object : GetChatsCallback {
            override fun onSuccess(chatsBean: ChatsBean) {
                recycler_view_chats!!.setRefreshing(false)
                chats.clear()
                for (chatBean in chatsBean.result!!) {
                    val chatItem = ChatItem()
                    chatItem.chat_type = chatBean.chat_type
                    chatItem.chatid = chatBean.topic
                    chatItem.title = chatBean.chat_name
                    //TODO get timestamp and content from local storage
//                    ArrayList<MessageItem> itemList = Tools.getMessageItemList(chatItem.chatid);
//                    if(itemList!=null && itemList.size()>0){
//                        MessageItem item = itemList.get(0);
//                        chatItem.timestamp = item.timestamp;
//                        chatItem.content = item.content;
//                    }
                    chats.add(chatItem)
                }
                fixChatItemWithLocal()
                updateView()
                Tools.saveChatItemList(chats)
            }

            override fun onFail(error: String) {
                Toast.makeText(getActivity(), "request chats error:$error", Toast.LENGTH_SHORT)
                    .show()
                recycler_view_chats!!.setRefreshing(false)
            }
        })
    }

    private fun fixChatItemWithLocal() {
        val local_chats: ArrayList<ChatItem>? = Tools.chatItemList
        if (local_chats==null || local_chats.size == 0) {
            return
        }
        for (i in chats.indices) {
            for (chatItemLocal in local_chats) {
                if (chats[i].chatid.equals(chatItemLocal.chatid)) {
                    val chatItem: ChatItem = chats[i]
                    chatItem.content = chatItemLocal.content
                    chatItem.timestamp = chatItemLocal.timestamp
                    chatItem.unreadCount = chatItemLocal.unreadCount
                }
            }
        }
    }

    private fun initView() {
        recycler_view_chats = rootView!!.findViewById(R.id.recycler_view_chats)
        iv_new_message = rootView!!.findViewById(R.id.iv_new_message)
        recycler_view_chats!!.setEmptyIcon(R.mipmap.ic_chats_empty)
        recycler_view_chats!!.setEmptyMessage("Your message list is empty")
    }

    private fun updateView() {
        if (chats.size == 0) {
            recycler_view_chats!!.showEmptyView()
        } else {
            recycler_view_chats!!.hideEmptyView()
        }
        chatsAdapter = ChatsAdapter(chats)
        recycler_view_chats!!.setAdapter(chatsAdapter)
        chatsAdapter!!.setOnItemClickListener(object : ChatsAdapter.OnItemClickListener{
            override fun onItemClick(position: Int) {
                val chatItem: ChatItem = chats[position]
                val chat_type: String = chatItem.chat_type!!
                val chat_id: String = chatItem.chatid!!
                if (chatItem.unreadCount != 0) {
                    chatItem.unreadCount = 0
                    chatsAdapter!!.notifyItemChanged(position)
                }
                val intent = Intent(activity!!, MessageActivity::class.java)
                intent.putExtra(Constants.ROUTER_KEY_CHAT_TYPE, chat_type)
                intent.putExtra(Constants.ROUTER_KEY_CHAT_ID, chat_id)
                activity!!.startActivity(intent)
//                ARouter.getInstance().build(RouterPath.CHAT_MESSAGE)
//                    .withString(Constants.ROUTER_KEY_CHAT_TYPE, chat_type)
//                    .withString(Constants.ROUTER_KEY_CHAT_ID, chat_id).navigation()
            }
        })
    }

    private fun setListener() {
        recycler_view_chats!!.setOnRefreshListener(object : SwipeRefreshLayout.OnRefreshListener{
            override fun onRefresh() {
                requestData()
            }
        })
        iv_new_message!!.setOnClickListener {
            if (toNewMessageListener != null) {
                toNewMessageListener!!.toNewMessageModule()
            }
        }
        //TODO 监听websocket更新列表
        setChatsCallback()
    }

    fun setChatsCallback() {
        Web3MQMessageManager.setChatsMessageCallback(this)
    }
}