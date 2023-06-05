package com.ty.module_common.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.ty.module_common.R

open class BaseFragment : Fragment() {
    protected var rootView: ConstraintLayout? = null
    private var layoutId = 0
    private var cl_loading: ConstraintLayout? = null
    private var iv_loading: ImageView? = null
    private var loadAnimation: Animation? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    open fun setContent(layoutId: Int) {
        this.layoutId = layoutId
    }

    open fun onBaseCreateView() {}
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView = inflater.inflate(R.layout.base_fragment, null) as ConstraintLayout
        val view = LayoutInflater.from(activity).inflate(layoutId, rootView, false)
        cl_loading = rootView!!.findViewById(R.id.cl_loading)
        iv_loading = rootView!!.findViewById(R.id.iv_loading)
        rootView!!.addView(view)
        loadAnimation = AnimationUtils.loadAnimation(
            activity,
            R.anim.rotate
        )
        onBaseCreateView()
        return rootView
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun showLoadingDialog() {
        cl_loading!!.visibility = View.VISIBLE
        doLoadingAnimate()
    }

    fun hideLoadingDialog() {
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