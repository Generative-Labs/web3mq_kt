package com.ty.module_login.view

import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import kotlin.jvm.JvmOverloads
import com.ty.module_login.R

class InputPwdView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    private var et_pwd: EditText? = null
    private var emptyWatcher: EmptyWatcher? = null
    private var iv_show_hide_pwd: ImageView? = null
    private var isOpenEye = false

    init {
        initView(context)
    }

    private fun initView(context: Context) {
        val rootView: View = LayoutInflater.from(context).inflate(R.layout.view_input_pwd, this)
        et_pwd = rootView.findViewById(R.id.et_pwd)
        iv_show_hide_pwd = rootView.findViewById(R.id.iv_show_hide_pwd)
        et_pwd!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                if (emptyWatcher != null) {
                    emptyWatcher!!.onEmptyChange(TextUtils.isEmpty(et_pwd!!.getText().toString()))
                }
            }
        })
        iv_show_hide_pwd!!.setOnClickListener(OnClickListener {
            if (!isOpenEye) {
                isOpenEye = true
                iv_show_hide_pwd!!.setImageResource(R.mipmap.ic_pwd_visible)
                //密码可见
                et_pwd!!.setTransformationMethod(HideReturnsTransformationMethod.getInstance())
                et_pwd!!.setSelection(et_pwd!!.getText().toString().length)
            } else {
                isOpenEye = false
                iv_show_hide_pwd!!.setImageResource(R.mipmap.ic_pwd_invisible)
                //密码不可见
                et_pwd!!.setTransformationMethod(PasswordTransformationMethod.getInstance())
                et_pwd!!.setSelection(et_pwd!!.getText().toString().length)
            }
        })
    }

    fun initHint(id: Int) {
        et_pwd!!.setHint(id)
    }

    fun setEmptyWatcher(emptyWatcher: EmptyWatcher?) {
        this.emptyWatcher = emptyWatcher
    }

    interface EmptyWatcher {
        fun onEmptyChange(empty: Boolean)
    }

    val pwd: String
        get() = et_pwd!!.text.toString()
}