package com.ty.module_profile.activity

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.RadioGroup.OnCheckedChangeListener
import android.widget.Toast
import com.ty.module_common.activity.BaseActivity
import com.ty.module_profile.ModuleProfile
import com.ty.module_profile.R
import com.ty.web3mq.Web3MQPermission
import com.ty.web3mq.interfaces.UpdateUserPermissionCallback
import com.ty.web3mq.utils.DefaultSPHelper

class MyProfileSettingActivity : BaseActivity() {
    var btn_logout: Button? = null
    var iv_back: ImageView? = null
    var rg_msg_permission: RadioGroup? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(R.layout.activity_my_profile_settings)
        btn_logout = findViewById(R.id.btn_logout)
        iv_back = findViewById(R.id.iv_back)
        rg_msg_permission = findViewById(R.id.rg_msg_permission)

        iv_back!!.setOnClickListener { finish() }
        btn_logout!!.setOnClickListener {
            DefaultSPHelper.clear()
            ModuleProfile.onLogoutEvent?.onLogout()
        }
        rg_msg_permission!!.setOnCheckedChangeListener(object: OnCheckedChangeListener{
            override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
                when (checkedId){
                    R.id.rb_anyone -> updateChatPermission(Web3MQPermission.CHAT_PERMISSION_PUBLIC)
                    R.id.rb_following -> updateChatPermission(Web3MQPermission.CHAT_PERMISSION_FOLLOWING)
                    R.id.rb_follower -> updateChatPermission(Web3MQPermission.CHAT_PERMISSION_FOLLOWER)
                    R.id.rb_friends -> updateChatPermission(Web3MQPermission.CHAT_PERMISSION_FRIEND)
                }
            }
        })
    }

    fun updateChatPermission(chatPermission: String){
        Web3MQPermission.updateChatPermission(chatPermission, object:
            UpdateUserPermissionCallback{
            override fun onSuccess() {
                Toast.makeText(this@MyProfileSettingActivity, "update success", Toast.LENGTH_SHORT).show()
            }

            override fun onFail(error: String) {
                Toast.makeText(this@MyProfileSettingActivity, "update error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}