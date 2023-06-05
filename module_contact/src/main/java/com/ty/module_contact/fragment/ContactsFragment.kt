package com.ty.module_contact.fragment

import android.os.Bundle
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.tabs.TabLayout
import com.ty.module_common.fragment.BaseFragment
import com.ty.module_contact.R

object ContactsFragment : BaseFragment() {
    private var tabLayout: TabLayout? = null
    private var currentFragment: Fragment? = null
    private var iv_new_message: ImageView? = null
    private var toNewMessageListener: ToNewMessageListener? = null
    private const val FRAGMENT_ID_FOLLOWER = 0
    private const val FRAGMENT_ID_FOLLOWING = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(R.layout.fragment_contacts)
    }

    override fun onBaseCreateView() {
        super.onBaseCreateView()
        initView()
        setListener()
    }

    private fun initView() {
        tabLayout = rootView!!.findViewById(R.id.tab_layout)
        iv_new_message = rootView!!.findViewById(R.id.iv_new_message)
        tabLayout!!.addTab(tabLayout!!.newTab().setText("Followers").setId(FRAGMENT_ID_FOLLOWER))
        tabLayout!!.addTab(tabLayout!!.newTab().setText("Following").setId(FRAGMENT_ID_FOLLOWING))
        switchContent(FollowersFragment.getInstance())
    }

    private fun setListener() {
        iv_new_message!!.setOnClickListener { // new message
            if (toNewMessageListener != null) {
                toNewMessageListener!!.toNewMessageModule()
            }
        }
        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                handleTab(tab)
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {
                handleTab(tab)
            }
        })
    }

    private fun handleTab(tab: TabLayout.Tab) {
        when (tab.id) {
            FRAGMENT_ID_FOLLOWER -> switchContent(FollowersFragment.getInstance())
            FRAGMENT_ID_FOLLOWING -> switchContent(FollowingFragment.getInstance())
        }
    }

    fun setToNewMessageListener(toNewMessageListener: ToNewMessageListener?) {
        this.toNewMessageListener = toNewMessageListener
    }

    fun switchContent(to: Fragment) {
        val transaction: FragmentTransaction = getChildFragmentManager().beginTransaction()
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

    interface ToNewMessageListener {
        fun toNewMessageModule()
    }

}