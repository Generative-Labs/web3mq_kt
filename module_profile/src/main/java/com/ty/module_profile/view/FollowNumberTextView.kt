package com.ty.module_profile.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.ty.module_profile.view.FollowNumberTextView
import com.ty.module_profile.ModuleProfile
import kotlin.jvm.Volatile
import com.ty.module_profile.fragment.MyProfileFragment
import kotlin.jvm.JvmOverloads
import com.ty.module_profile.ModuleProfile.OnLogoutEvent
import com.ty.module_profile.ModuleProfile.OnChatEvent
import com.ty.module_profile.R

class FollowNumberTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private var tv_followers_number: TextView? = null
    private var tv_following_number: TextView? = null

    init {
        initView(context)
    }

    private fun initView(context: Context) {
        val rootView: View =
            LayoutInflater.from(context).inflate(R.layout.view_follow_numbers, this)
        tv_followers_number = rootView.findViewById(R.id.tv_followers_number)
        tv_following_number = rootView.findViewById(R.id.tv_following_number)
    }

    fun setNumbers(followers_number: Int, following_number: Int) {
        tv_followers_number!!.text = followers_number.toString() + ""
        tv_following_number!!.text = following_number.toString() + ""
    }
}