package com.ty.module_profile.activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.ty.module_common.activity.BaseActivity
import com.ty.module_common.config.Constants
import com.ty.module_profile.R

class MyProfileEditActivity : BaseActivity() {
    var tv_nickname: TextView? = null
    var nickname: String? = null
    var iv_back: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(R.layout.activity_my_profile_edit)
        nickname = intent.getStringExtra(Constants.ROUTER_KEY_NICKNAME)
        tv_nickname = findViewById(R.id.tv_nickname)
        iv_back = findViewById(R.id.iv_back)
        tv_nickname!!.text = nickname
        iv_back!!.setOnClickListener { finish() }
    }
}