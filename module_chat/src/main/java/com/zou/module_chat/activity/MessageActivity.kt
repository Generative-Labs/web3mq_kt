package com.zou.module_chat.activity

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.ty.module_common.activity.BaseActivity
import com.ty.module_common.config.Constants
import com.ty.module_common.utils.CommonUtils
import com.ty.module_common.view.Web3MQListView
import com.ty.web3mq.Web3MQChats
import com.ty.web3mq.Web3MQMessageManager
import com.ty.web3mq.Web3MQUser
import com.ty.web3mq.http.beans.MessagesBean
import com.ty.web3mq.interfaces.GetMessageHistoryCallback
import com.ty.web3mq.interfaces.MessageCallback
import com.ty.web3mq.interfaces.UpdateMyChatCallback
import com.ty.web3mq.utils.DefaultSPHelper
import com.ty.web3mq.websocket.bean.MessageBean
import com.zou.module_chat.R
import com.zou.module_chat.adapter.MessagesAdapter
import com.zou.module_chat.bean.MessageItem
import com.zou.module_chat.fragment.InviteGroupFragment
import com.zou.module_chat.utils.Tools
import java.util.ArrayList

class MessageActivity : BaseActivity() {
    var chat_id: String? = null
    var chat_type: String? = null
    private val messageList: ArrayList<MessageItem> = ArrayList<MessageItem>()
    private var adapter: MessagesAdapter? = null
    private var list_message: Web3MQListView? = null
    private var tv_title_user_id: TextView? = null
    private var btn_send: ImageButton? = null
    private var btn_add_member: ImageButton? = null
    private var btn_more: ImageButton? = null
    private var et_message: EditText? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(R.layout.activity_message)
        chat_type = intent.getStringExtra(Constants.ROUTER_KEY_CHAT_TYPE)
        chat_id = intent.getStringExtra(Constants.ROUTER_KEY_CHAT_ID)
        initView()
        setListener()
        observeMessageReceive()
        requestData(1, PAGE_SIZE)
    }

    private fun requestData(page: Int, size: Int) {
        Web3MQMessageManager.getMessageHistory(page, size, chat_id!!, object: GetMessageHistoryCallback {
                override fun onSuccess(messagesBean: MessagesBean) {
                    messageList.clear()
                    if (messagesBean.total > 0) {
                        list_message!!.hideEmptyView()
                        for (msg in messagesBean.result!!) {
                            val messageItem = MessageItem()
                            messageItem.from = msg.from
                            messageItem.content = String(Base64.decode(msg.payload, Base64.DEFAULT))
                            messageItem.timestamp = msg.timestamp
                            messageItem.isMine =
                                DefaultSPHelper.getUserID().equals(msg.from)
                            messageList.add(0, messageItem)
                        }
                        updateView()
                    } else {
                        list_message!!.showEmptyView()
                        val timeStamp = System.currentTimeMillis()
                        Web3MQChats.updateMyChat(
                            timeStamp,
                            chat_id!!,
                            Constants.CHAT_TYPE_USER,
                            object : UpdateMyChatCallback {
                                override fun onSuccess() {
                                    Toast.makeText(
                                        this@MessageActivity,
                                        "update success",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                override fun onFail(error: String) {
                                    Toast.makeText(
                                        this@MessageActivity,
                                        "update error : $error",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                    }
                    //                list_message.setRefreshing(false);
                }

                override fun onFail(error: String?) {
//                list_message.setRefreshing(false);
                }
            })

//        if(chat_type.equals(Constants.CHAT_TYPE_GROUP)){
//            Web3MQGroup.getInstance().getGroupMembers(1,500,chat_id, new GetGroupMembersCallback() {
//                @Override
//                public void onSuccess(GroupMembersBean groupMembersBean) {
//                    if(groupMembersBean.total>0){
//
//                    }
//                }
//
//                @Override
//                public void onFail(String error) {
//
//                }
//            });
//        }
    }

    private fun initView() {
        tv_title_user_id = findViewById(R.id.tv_title_user_id)
        tv_title_user_id!!.text = chat_id
        list_message = findViewById(R.id.list_message)
        btn_send = findViewById(R.id.btn_send)
        et_message = findViewById(R.id.et_message)
        btn_add_member = findViewById(R.id.btn_add_member)
        btn_more = findViewById(R.id.btn_more)
        when (chat_type) {
            Constants.CHAT_TYPE_USER -> {
                btn_add_member!!.visibility = View.GONE
                btn_more!!.visibility = View.GONE
            }
            Constants.CHAT_TYPE_GROUP -> {
                btn_add_member!!.visibility = View.VISIBLE
                btn_more!!.visibility = View.VISIBLE
            }
        }
    }

    private fun setListener() {
        btn_send!!.setOnClickListener(View.OnClickListener {
            val message = et_message!!.text.toString()
            if (TextUtils.isEmpty(message)) {
                return@OnClickListener
            }
            Web3MQMessageManager.sendMessage(message, chat_id!!, true)
            val messageItem = MessageItem()
            messageItem.from = Web3MQUser.myUserId
            messageItem.content = message
            messageItem.timestamp = System.currentTimeMillis()
            messageItem.isMine = true
            updateMessage(messageItem)
            CommonUtils.hideKeyboard(this@MessageActivity)
            et_message!!.setText("")
        })
        btn_add_member!!.setOnClickListener {
            InviteGroupFragment.setGroupID(chat_id!!)
            InviteGroupFragment.show(getSupportFragmentManager(), "invite people")
        }
        btn_more!!.setOnClickListener {
            val intent: Intent = Intent(this@MessageActivity, RoomSettingsActivity::class.java)
            intent.putExtra(Constants.INTENT_GROUP_ID, chat_id)
            startActivity(intent)
        }
    }

    private fun observeMessageReceive() {
        when (chat_type) {
            Constants.CHAT_TYPE_USER -> Web3MQMessageManager.addDMCallback(chat_id!!, object : MessageCallback{
                    override fun onMessage(message: MessageBean) {
                        val messageItem = MessageItem()
                        messageItem.from = message.from
                        messageItem.content = message.payload
                        messageItem.timestamp = message.timestamp
                        messageItem.isMine = false
                        updateMessage(messageItem)
                    }
                })
            Constants.CHAT_TYPE_GROUP -> Web3MQMessageManager.addGroupMessageCallback(chat_id!!, object : MessageCallback{
                    override fun onMessage(message: MessageBean) {
                        val messageItem = MessageItem()
                        messageItem.from = message.from
                        messageItem.content = message.payload
                        messageItem.timestamp = message.timestamp
                        messageItem.isMine = false
                        updateMessage(messageItem)
                    }
                })
        }
    }

    private fun updateMessage(messageItem: MessageItem) {
        messageList.add(messageItem)
        list_message!!.hideEmptyView()
        if (adapter == null) {
            adapter = MessagesAdapter(messageList, this@MessageActivity)
            list_message!!.setAdapter(adapter)
        } else {
            adapter!!.notifyItemInserted(messageList.size)
        }
        list_message!!.scrollTo(messageList.size - 1)
        Tools.updateChatItem(chat_id, messageItem.content!!, messageItem.timestamp, 0)
    }

    private fun updateView() {
        if (messageList.size > 0) {
            list_message!!.hideEmptyView()
        } else {
            list_message!!.showEmptyView()
            return
        }
        if (adapter == null) {
            adapter = MessagesAdapter(messageList, this@MessageActivity)
            list_message!!.setAdapter(adapter)
        } else {
            adapter!!.notifyDataSetChanged()
        }
        list_message!!.scrollTo(messageList.size - 1)
        Tools.updateChatItem(
            chat_id,
            messageList[messageList.size - 1].content!!,
            messageList[messageList.size - 1].timestamp,
            0
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        when (chat_type) {
            Constants.CHAT_TYPE_USER -> Web3MQMessageManager.removeDMCallback(chat_id!!)
            Constants.CHAT_TYPE_GROUP -> Web3MQMessageManager.removeGroupMessageCallback(chat_id!!)
        }
    }

    companion object {
        private const val PAGE_SIZE = 100
    }
}