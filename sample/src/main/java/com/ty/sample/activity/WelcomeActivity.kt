package com.ty.sample.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.ty.module_login.ModuleLogin
import com.ty.module_login.interfaces.LoginSuccessCallback
import com.ty.sample.R

class WelcomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        ModuleLogin.onLoginSuccessCallback = object : LoginSuccessCallback {
            override fun onLoginSuccess() {
                val intent = Intent(this@WelcomeActivity, HomePageActivity::class.java)
                startActivity(intent)
            }
        }
        ModuleLogin.launch(this@WelcomeActivity)
    }
}