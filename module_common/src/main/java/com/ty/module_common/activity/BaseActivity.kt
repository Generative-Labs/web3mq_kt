package com.ty.module_common.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.ty.module_common.R

open class BaseActivity : AppCompatActivity() {
    private var root_view: ViewGroup? = null
    private var cl_loading: ConstraintLayout? = null
    private var iv_loading: ImageView? = null
    private var loadAnimation: Animation? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.base_activity)
        root_view = findViewById(R.id.root)
        cl_loading = findViewById(R.id.cl_loading)
        iv_loading = findViewById(R.id.iv_loading)
        loadAnimation = AnimationUtils.loadAnimation(this, R.anim.rotate)
    }

    protected fun setContent(view_id: Int) {
        val view = LayoutInflater.from(this).inflate(view_id, null, false)
        root_view!!.addView(view, 0)
    }

    protected fun showLoading() {
        cl_loading!!.visibility = View.VISIBLE
        doLoadingAnimate()
    }

    protected fun hideLoading() {
        cl_loading!!.visibility = View.GONE
        stopLoadingAnimate()
    }

    private fun doLoadingAnimate() {
        iv_loading!!.startAnimation(loadAnimation)
    }

    private fun stopLoadingAnimate() {
        iv_loading!!.clearAnimation()
    }
}