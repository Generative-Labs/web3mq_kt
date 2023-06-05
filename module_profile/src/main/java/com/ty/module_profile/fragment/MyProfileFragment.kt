package com.ty.module_profile.fragment

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.bumptech.glide.Glide
import com.ty.module_common.config.Constants
import com.ty.module_common.fragment.BaseFragment
import com.ty.module_common.utils.CommonUtils
import com.ty.module_profile.R
import com.ty.module_profile.activity.MyProfileEditActivity
import com.ty.module_profile.activity.MyProfileSettingActivity
import com.ty.module_profile.view.FollowNumberTextView
import com.ty.web3mq.Web3MQUser
import com.ty.web3mq.http.beans.ProfileBean
import com.ty.web3mq.interfaces.GetMyProfileCallback

class MyProfileFragment : BaseFragment() {
    private var iv_avatar: ImageView? = null
    private var tv_edit: TextView? = null
    private var btn_setting: ImageButton? = null
    private var btn_copy: ImageButton? = null
    private var tv_wallet_address: TextView? = null
    private var tv_follow_number: FollowNumberTextView? = null
    private var wallet_address: String? = null
    private var nickname: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(R.layout.fragment_my_profile)
    }

    override fun onBaseCreateView() {
        super.onBaseCreateView()
        initView()
        setListener()
        requestData()
    }

    private fun requestData() {
        Web3MQUser.getMyProfile(object : GetMyProfileCallback{
            override fun onSuccess(profileBean: ProfileBean) {
                val avatar_url: String = profileBean.avatar_url!!
                if (!TextUtils.isEmpty(avatar_url)) {
                    Glide.with(context).load(avatar_url).into(iv_avatar)
                }
                nickname = profileBean.nickname
                wallet_address = profileBean.wallet_address
                tv_wallet_address!!.text = wallet_address
                tv_follow_number!!.setNumbers(
                    profileBean.stats!!.total_followers,
                    profileBean.stats!!.total_following
                )
            }

            override fun onFail(error: String) {
                Toast.makeText(getActivity(), "get profile error:$error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun initView() {
        iv_avatar = rootView!!.findViewById(R.id.iv_avatar)
        tv_edit = rootView!!.findViewById(R.id.tv_edit)
        btn_setting = rootView!!.findViewById(R.id.btn_setting)
        btn_copy = rootView!!.findViewById(R.id.btn_copy)
        tv_wallet_address = rootView!!.findViewById(R.id.tv_wallet_address)
        tv_follow_number = rootView!!.findViewById(R.id.tv_follow_number)
    }

    private fun setListener() {
        btn_copy!!.setOnClickListener {
            CommonUtils.copy(requireContext(), wallet_address)
            Toast.makeText(activity, "copied", Toast.LENGTH_SHORT).show()
        }
        tv_edit!!.setOnClickListener {
            val intent = Intent(requireActivity(), MyProfileEditActivity::class.java)
            startActivity(intent)
        }
        btn_setting!!.setOnClickListener {
            val intent = Intent(requireActivity(), MyProfileSettingActivity::class.java)
            intent.putExtra(Constants.ROUTER_KEY_NICKNAME, nickname)
            startActivity(intent)
        }
    }

    companion object {
        fun getInstance() = MyProfileFragment()
    }
}