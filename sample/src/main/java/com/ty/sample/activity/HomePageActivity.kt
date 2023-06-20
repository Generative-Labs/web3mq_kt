package com.ty.sample.activity

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ty.module_common.Web3MQUI
import com.ty.module_common.config.Constants
import com.ty.module_common.utils.CommonUtils
import com.ty.module_contact.fragment.ContactsFragment
import com.ty.module_login.ModuleLogin
import com.ty.module_new_message.NewMessageFragment
import com.ty.module_notification.fragment.NotificationFragment
import com.ty.module_profile.ModuleProfile
import com.ty.module_profile.fragment.MyProfileFragment
import com.ty.sample.R
import com.ty.web3mq.Web3MQClient
import com.ty.web3mq.Web3MQNotification
import com.ty.web3mq.http.beans.NotificationBean
import com.ty.web3mq.interfaces.ConnectCallback
import com.ty.web3mq.interfaces.NotificationMessageCallback
import com.ty.web3mq.interfaces.OnConnectCommandCallback
import com.ty.web3mq.interfaces.OnWebsocketClosedCallback
import com.zou.module_chat.ModuleChat
import com.zou.module_chat.fragment.ChatsFragment
import java.util.ArrayList

class HomePageActivity : AppCompatActivity() {
    private var currentFragment: Fragment? = null
    private var bottom_navigation_view: BottomNavigationView? = null
    private var cl_reconnect: ConstraintLayout? = null
    private var btn_reconnect: Button? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)
        Web3MQClient.startConnect(object : ConnectCallback{
            override fun onSuccess() {
                sendConnectCommand()
            }

            override fun onFail(error: String) {
                Toast.makeText(this@HomePageActivity, "init fail", Toast.LENGTH_SHORT).show()
            }

            override fun alreadyConnected() {
                sendConnectCommand()
            }
        })
        Web3MQClient.setOnWebsocketClosedCallback(object : OnWebsocketClosedCallback {
            override fun onClose() {
                    //TODO showReconnectDialog
                    cl_reconnect!!.visibility = View.VISIBLE
                }
            })
        initView()
        setListener()
        listenToNotificationMessageEvent()
        //        listenToChatMessageEvent();
    }

    private fun sendConnectCommand() {
        Web3MQClient.sendConnectCommand(object : OnConnectCommandCallback{
            override fun onConnectCommandResponse() {
                Log.i(TAG, "onConnectCommandResponse Success")
                cl_reconnect!!.visibility = View.GONE
            }
        })
    }

    private fun initView() {
        bottom_navigation_view = findViewById(R.id.bottom_navigation_view)
        cl_reconnect = findViewById(R.id.cl_reconnect)
        btn_reconnect = findViewById(R.id.btn_reconnect)
        switchContent(ChatsFragment)
    }

    private fun setListener() {
        btn_reconnect!!.setOnClickListener {
            Web3MQClient.reconnect()
            sendConnectCommand()
            cl_reconnect!!.visibility = View.GONE
        }

        bottom_navigation_view!!.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_chats -> {
                    switchContent(ChatsFragment)
                    listenToNotificationMessageEvent()
                    hideRedDot(0)
                }
                R.id.navigation_contact -> {
                    switchContent(ContactsFragment)
                    listenToNotificationMessageEvent()
                }
                R.id.navigation_notifications -> {
                    switchContent(NotificationFragment.getInstance())
                    hideRedDot(2)
                    NotificationFragment.getInstance().listenToNotificationMessageEvent()
                }
                R.id.navigation_profile -> {
                    switchContent(MyProfileFragment.getInstance())
                    listenToNotificationMessageEvent()
                }
            }
            true
        }
        ContactsFragment.setToNewMessageListener(object : ContactsFragment.ToNewMessageListener {
            override fun toNewMessageModule() {
                //new message
                NewMessageFragment.show(supportFragmentManager, "new message")
            }
        })
        ChatsFragment.setToNewMessageListener(object : ChatsFragment.ToNewMessageListener{
            override fun toNewMessageModule() {
                NewMessageFragment.show(supportFragmentManager, "new message")
            }
        })
        NewMessageFragment.setToMessageListener(object : NewMessageFragment.ToMessageListener{
            override fun toMessage(chat_type: String?, chat_id: String?) {
                ModuleChat.toMessageUI(this@HomePageActivity, chat_type, chat_id)
            }
        })
        ModuleProfile.onChatEvent = (object : ModuleProfile.OnChatEvent {
            override fun onChat(userid: String?) {
                ModuleChat.toMessageUI(this@HomePageActivity, Constants.CHAT_TYPE_USER, userid)
            }
        })
        ModuleProfile.onLogoutEvent = (object : ModuleProfile.OnLogoutEvent{
            override fun onLogout() {
                ModuleLogin.launch(this@HomePageActivity)
            }
        })
        ModuleChat.toNewMessageRequestListener = (object : ModuleChat.ToNewMessageRequestListener {
                override fun toRequestFollow(userid: String?) {
                    val newMessageFragment: NewMessageFragment = NewMessageFragment
                    val bundle = Bundle()
                    bundle.putString(Constants.ROUTER_KEY_USER_ID, userid)
                    newMessageFragment.setArguments(bundle)
                    newMessageFragment.show(supportFragmentManager, "new message")
                }
            })
    }

    private fun listenToNotificationMessageEvent() {
        Web3MQNotification.setOnNotificationMessageEvent(object : NotificationMessageCallback {
                override fun onNotificationMessage(response: ArrayList<NotificationBean>) {
                    Log.i(TAG,"onNotificationMessage")
                    showRedDot(2)
                }
            })
    }

    fun switchContent(to: Fragment) {
        val transaction = supportFragmentManager
            .beginTransaction()
        if (currentFragment == null) {
            transaction.add(R.id.fl_contacts_content, to).commitAllowingStateLoss()
            currentFragment = to
            return
        }
        if (currentFragment !== to) {
            if (!to.isAdded) { // 先判断是否被add过
                transaction.hide(currentFragment!!).add(R.id.fl_contacts_content, to)
                    .commitAllowingStateLoss() // 隐藏当前的fragment，add下一个到Activity中
            } else {
                transaction.hide(currentFragment!!).show(to)
                    .commitAllowingStateLoss() // 隐藏当前的fragment，显示下一个
            }
            currentFragment = to
        }
    }

    fun showRedDot(position: Int) {
        var menuView: BottomNavigationMenuView? = null
        for (i in 0 until bottom_navigation_view!!.childCount) {
            val child = bottom_navigation_view!!.getChildAt(i)
            if (child is BottomNavigationMenuView) {
                menuView = child
                break
            }
        }
        if (menuView != null) {
            val params: FrameLayout.LayoutParams =
                FrameLayout.LayoutParams(CommonUtils.dp2px(this, 8F), CommonUtils.dp2px(this, 8F))
            params.gravity = Gravity.RIGHT
            params.rightMargin = CommonUtils.dp2px(this, 25F)
            params.topMargin = CommonUtils.dp2px(this, 7F)
            val itemView = menuView.getChildAt(position) as BottomNavigationItemView
            val dotView = ImageView(this)
            dotView.setImageResource(R.mipmap.ic_red_dot)
            itemView.addView(dotView, params)
        }
    }

    private fun hideRedDot(position: Int) {
        var menuView: BottomNavigationMenuView? = null
        for (i in 0 until bottom_navigation_view!!.childCount) {
            val child = bottom_navigation_view!!.getChildAt(i)
            if (child is BottomNavigationMenuView) {
                menuView = child
                break
            }
        }
        if (menuView != null) {
            val itemView = menuView.getChildAt(position) as BottomNavigationItemView
            if (itemView.childCount > 2) {
                itemView.removeViewAt(itemView.childCount - 1)
            }
        }
    }

    companion object {
        private const val TAG = "HomePageActivity"
    }
}