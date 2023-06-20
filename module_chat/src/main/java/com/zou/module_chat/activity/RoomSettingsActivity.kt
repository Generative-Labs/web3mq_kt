package com.zou.module_chat.activity

import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ty.module_common.activity.BaseActivity
import com.ty.module_common.config.Constants
import com.ty.web3mq.Web3MQGroup
import com.ty.web3mq.http.beans.GroupMembersBean
import com.ty.web3mq.interfaces.GetGroupMembersCallback
import com.zou.module_chat.R
import com.zou.module_chat.adapter.RoomMembersAdapter
import com.zou.module_chat.fragment.InviteGroupFragment

class RoomSettingsActivity : BaseActivity() {
    private var iv_back: ImageView? = null
    private var list_members: RecyclerView? = null
    private var cl_group_avatar: ConstraintLayout? = null
    private var cl_room_name: ConstraintLayout? = null
    private var group_id: String? = null
    private var adapter: RoomMembersAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent(R.layout.activity_room_settings)
        group_id = getIntent().getStringExtra(Constants.INTENT_GROUP_ID)
        if (group_id != null) {
            initView()
            setListener()
            requestData()
        } else {
            Toast.makeText(this@RoomSettingsActivity, "no group id", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initView() {
        iv_back = findViewById(R.id.iv_back)
        cl_group_avatar = findViewById(R.id.cl_group_avatar)
        cl_room_name = findViewById(R.id.cl_room_name)
        list_members = findViewById(R.id.list_members)
        list_members!!.setLayoutManager(
            LinearLayoutManager(
                this,
                LinearLayoutManager.VERTICAL,
                false
            )
        )
    }

    private fun setListener() {
        iv_back!!.setOnClickListener { finish() }
        cl_group_avatar!!.setOnClickListener {
            //TODO change avatar
        }
        cl_room_name!!.setOnClickListener {
            //TODO change name
        }
    }

    private fun requestData() {
        Web3MQGroup.getGroupMembers(1, 500, group_id!!, object : GetGroupMembersCallback{
                override fun onSuccess(groupMembersBean: GroupMembersBean) {
                    if (groupMembersBean.result!!.size > 0) {
                        adapter = RoomMembersAdapter(groupMembersBean.result!!, this@RoomSettingsActivity)
                        adapter!!.setOnAddPeopleClickListener(object : RoomMembersAdapter.OnAddPeopleClickListener{
                            override fun onAddPeopleClick() {
                                InviteGroupFragment.setGroupID(group_id!!)
                                InviteGroupFragment.show(getSupportFragmentManager(), "invite people")
                            }
                        })
                        list_members!!.setAdapter(adapter)
                    }
                }

                override fun onFail(error: String) {
                    Toast.makeText(
                        this@RoomSettingsActivity,
                        "get group members error: $error",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}