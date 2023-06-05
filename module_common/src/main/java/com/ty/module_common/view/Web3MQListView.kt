package com.ty.module_common.view

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.ty.module_common.R
import com.ty.module_common.utils.CommonUtils

class Web3MQListView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    SwipeRefreshLayout(context, attrs) {
    private var cl_empty_view: ConstraintLayout? = null
    private var recyclerview: RecyclerView? = null
    private var iv_empty_icon: ImageView? = null
    private var tv_empty_message: TextView? = null

    init {
        initView(context)
    }

    private fun initView(context: Context) {
        val rootView: View = LayoutInflater.from(context).inflate(R.layout.view_web3mq_list, this)
        cl_empty_view = rootView.findViewById(R.id.cl_empty_view)
        recyclerview = rootView.findViewById(R.id.recyclerview)
        iv_empty_icon = rootView.findViewById(R.id.iv_empty_icon)
        tv_empty_message = rootView.findViewById(R.id.tv_empty_message)
        recyclerview!!.setLayoutManager(
            LinearLayoutManager(
                context,
                LinearLayoutManager.VERTICAL,
                false
            )
        )
        val start: Int = CommonUtils.dp2px(context, 70F)
        val end: Int = CommonUtils.dp2px(context, 130F)
        setProgressViewOffset(false, start, end)
    }

    fun setAdapter(adapter: RecyclerView.Adapter<*>?) {
        recyclerview!!.setAdapter(adapter)
    }

    fun setEmptyIcon(drawable: Int) {
        iv_empty_icon!!.setImageResource(drawable)
    }

    fun setEmptyMessage(message: String?) {
        tv_empty_message!!.text = message
    }

    fun showEmptyView() {
        cl_empty_view!!.visibility = VISIBLE
    }

    fun hideEmptyView() {
        cl_empty_view!!.visibility = GONE
    }

    fun scrollTo(position: Int) {
        recyclerview!!.post(Runnable { recyclerview!!.scrollToPosition(position) })
    }
}