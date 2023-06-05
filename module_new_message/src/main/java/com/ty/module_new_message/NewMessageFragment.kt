package com.ty.module_new_message

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ty.module_common.config.Constants
import com.ty.module_common.view.Web3MQListView
import com.ty.module_new_message.adapter.FriendListAdapter
import com.ty.web3mq.Web3MQFollower
import com.ty.web3mq.Web3MQGroup
import com.ty.web3mq.http.beans.FollowerBean
import com.ty.web3mq.http.beans.FollowersBean
import com.ty.web3mq.http.beans.GroupBean
import com.ty.web3mq.interfaces.CreateGroupCallback
import com.ty.web3mq.interfaces.GetMyFollowingCallback
import com.ty.web3mq.interfaces.InvitationGroupCallback
import com.ty.web3mq.interfaces.SendFriendRequestCallback
import com.ty.web3mq.utils.ConvertUtil
import java.util.ArrayList

object NewMessageFragment: BottomSheetDialogFragment() {
    private var mBehavior: BottomSheetBehavior<*>? = null
    private var list_friends: Web3MQListView? = null
    private val shownFollower: ArrayList<FollowerBean> = ArrayList<FollowerBean>()
    private var allFollower: ArrayList<FollowerBean> = ArrayList<FollowerBean>()
    private var adapter: FriendListAdapter? = null
    private var btn_add_friends: Button? = null
    private var btn_create_room: Button? = null
    private var dialog: BottomSheetDialog? = null
    private var et_search: EditText? = null
    private var iv_back: ImageView? = null
    private var toMessageListener: ToMessageListener? = null
    private var userid: String? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        if (arguments != null) {
            userid = requireArguments().getString(Constants.ROUTER_KEY_USER_ID)
        }
        val view: View
        if (userid == null) {
            requestData()
            view = View.inflate(context, R.layout.dialog_bottom_sheet_new_message, null)
            initNewMessageView(view)
        } else {
            view = View.inflate(context, R.layout.dialog_bottom_sheet_add_friends, null)
            initAddFriendsView(view)
        }
        dialog!!.setContentView(view)
        mBehavior = BottomSheetBehavior.from(view.parent as View)
        return dialog!!
    }

    override fun onStart() {
        super.onStart()
        mBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onDismiss(dialog: DialogInterface) {
        userid = null
        super.onDismiss(dialog)
    }

    fun setToMessageListener(toMessageListener: ToMessageListener?) {
        this.toMessageListener = toMessageListener
    }

    interface ToMessageListener {
        fun toMessage(chat_type: String?, chat_id: String?)
    }

    private fun initNewMessageView(rootView: View) {
        list_friends = rootView.findViewById(R.id.list_friends)
        btn_add_friends = rootView.findViewById(R.id.btn_add_friends)
        btn_create_room = rootView.findViewById(R.id.btn_create_room)
        et_search = rootView.findViewById(R.id.et_search)
        iv_back = rootView.findViewById(R.id.iv_back)
        list_friends!!.setEmptyMessage("No friends")
        btn_add_friends!!.setOnClickListener(View.OnClickListener {
            val view = View.inflate(context, R.layout.dialog_bottom_sheet_add_friends, null)
            initAddFriendsView(view)
            dialog!!.setContentView(view)
        })
        btn_create_room!!.setOnClickListener(View.OnClickListener {
            val view = View.inflate(context, R.layout.dialog_bottom_sheet_create_room_step1, null)
            initCreateRoomStep1(view)
            dialog!!.setContentView(view)
        })
        et_search!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (TextUtils.isEmpty(s.toString())) {
                    filterEachFollower()
                    updateList(FriendListAdapter.STYLE_DEFAULT)
                } else {
                    filterFollowerByString(s.toString())
                    updateList(FriendListAdapter.STYLE_SEARCH)
                }
            }
        })
        iv_back!!.setOnClickListener(View.OnClickListener { dismiss() })
        requestData()
    }

    private fun initCreateRoomStep1(rootView: View) {
        val list_create_room: RecyclerView =
            rootView.findViewById<RecyclerView>(R.id.list_create_room)
        list_create_room.setLayoutManager(
            LinearLayoutManager(
                activity,
                LinearLayoutManager.VERTICAL,
                false
            )
        )
        val btn_next = rootView.findViewById<Button>(R.id.btn_next)
        val iv_cancel = rootView.findViewById<ImageView>(R.id.iv_cancel)
        val iv_back = rootView.findViewById<ImageView>(R.id.iv_back)
        val friendListAdapter = FriendListAdapter(allFollower, requireActivity())
        friendListAdapter.setStyle(FriendListAdapter.STYLE_CREATE_ROOM)
        list_create_room.setAdapter(friendListAdapter)
        btn_next.setOnClickListener {
            if (friendListAdapter.getCheckedFollower().size > 0) {
                val view =
                    View.inflate(context, R.layout.dialog_bottom_sheet_create_room_step2, null)
                initCreateRoomStep2(view, friendListAdapter.getCheckedFollower())
                dialog!!.setContentView(view)
            } else {
                Toast.makeText(activity, "please check someone", Toast.LENGTH_SHORT).show()
            }
        }
        iv_back.setOnClickListener {
            val view = View.inflate(context, R.layout.dialog_bottom_sheet_new_message, null)
            initNewMessageView(view)
            dialog!!.setContentView(view)
        }
        iv_cancel.setOnClickListener { dismiss() }
    }

    private fun initCreateRoomStep2(rootView: View, checkedFollower: ArrayList<FollowerBean>) {
        val recyclerview_room_member: RecyclerView =
            rootView.findViewById<RecyclerView>(R.id.recyclerview_room_member)
        recyclerview_room_member.setLayoutManager(
            LinearLayoutManager(
                activity,
                LinearLayoutManager.VERTICAL,
                false
            )
        )
        val btn_create = rootView.findViewById<Button>(R.id.btn_create)
        val iv_cancel = rootView.findViewById<ImageView>(R.id.iv_cancel)
        val iv_back = rootView.findViewById<ImageView>(R.id.iv_back)
        val et_room_name = rootView.findViewById<EditText>(R.id.et_room_name)
        val adapter = FriendListAdapter(checkedFollower, requireContext())
        adapter.setStyle(FriendListAdapter.STYLE_DEFAULT)
        recyclerview_room_member.setAdapter(adapter)
        btn_create.setOnClickListener { //TODO create
            val group_name = et_room_name.text.toString()
            val invite_user_id = ArrayList<String>()
            for (follower in checkedFollower) {
                invite_user_id.add(follower.userid!!)
            }
            createGroup(group_name, invite_user_id)
        }
        iv_back.setOnClickListener {
            val view = View.inflate(context, R.layout.dialog_bottom_sheet_create_room_step1, null)
            initCreateRoomStep1(view)
            dialog!!.setContentView(view)
        }
        iv_cancel.setOnClickListener { dismiss() }
    }

    private fun createGroup(group_name: String, userIds: ArrayList<String>) {
        Web3MQGroup.createGroup(group_name, object : CreateGroupCallback {
            override fun onSuccess(groupBean: GroupBean) {
                inviteMember(groupBean.groupid!!, userIds)
            }

            override fun onFail(error: String) {
                Toast.makeText(activity, "create group fail error:$error", Toast.LENGTH_SHORT)
                    .show()
            }
        })
    }

    private fun inviteMember(group_id: String, userIds: ArrayList<String>) {
        val ids = arrayOf<String>()
        Web3MQGroup.invite(group_id, userIds.toArray(ids), object : InvitationGroupCallback{
                override fun onSuccess(invitationGroupBean: GroupBean) {
                    dismiss()
                    if (toMessageListener != null) {
                        toMessageListener!!.toMessage(
                            Constants.CHAT_TYPE_GROUP,
                            invitationGroupBean.groupid
                        )
                    }
                }

                override fun onFail(error: String) {
                    Toast.makeText(activity, "invite member fail error:$error", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun updateList(style: String) {
        if (shownFollower.size > 0) {
            list_friends!!.hideEmptyView()
            //            if(adapter==null){
            adapter = FriendListAdapter(shownFollower, requireActivity())
            adapter!!.setStyle(style)
            adapter!!.setOnItemClickListener(object : FriendListAdapter.OnItemClickListener{
                override fun onItemClick(position: Int) {
                    val bean: FollowerBean = shownFollower[position]
                    dismiss()
                    if (toMessageListener != null) {
                        toMessageListener!!.toMessage(Constants.CHAT_TYPE_USER, bean.userid)
                    }
                }
            })
            list_friends!!.setAdapter(adapter)
            //            }else{
//                adapter.setStyle(style);
//                adapter.notifyDataSetChanged();
//            }
        } else {
            list_friends!!.showEmptyView()
        }
    }

    private fun initAddFriendsView(rootView: View) {
        val iv_back = rootView.findViewById<ImageView>(R.id.iv_back)
        val iv_cancel = rootView.findViewById<ImageView>(R.id.iv_cancel)
        val et_user_id = rootView.findViewById<EditText>(R.id.et_user_id)
        val et_invitation_note = rootView.findViewById<EditText>(R.id.et_invitation_note)
        val btn_add = rootView.findViewById<Button>(R.id.btn_add)
        btn_add.setOnClickListener {
            val user_id = et_user_id.text.toString()
            val invitation_note = et_invitation_note.text.toString()
            val timeStamp = System.currentTimeMillis()
            sendFriendRequest(user_id, invitation_note, timeStamp)
        }
        iv_back.setOnClickListener {
            val view = View.inflate(context, R.layout.dialog_bottom_sheet_new_message, null)
            initNewMessageView(view)
            dialog!!.setContentView(view)
        }
        iv_cancel.setOnClickListener { dismiss() }
    }

    private fun sendFriendRequest(user_id: String, invitation_note: String, timeStamp: Long) {
        Web3MQFollower.sendFriendRequest(
            user_id,
            timeStamp,
            invitation_note,
            object : SendFriendRequestCallback{
                override fun onSuccess() {
                    Toast.makeText(activity, "send friend request success", Toast.LENGTH_SHORT)
                        .show()
                    dismiss()
                }

                override fun onFail(error: String?) {
                    Toast.makeText(activity, "send friend request Fail", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun requestData() {
        Web3MQFollower.getFollowerAndFollowing(0, 500, object : GetMyFollowingCallback {
                override fun onSuccess(response: String) {
                    shownFollower.clear()
                    val followersBean: FollowersBean =
                        ConvertUtil.convertJsonToFollowersBean(response)!!
                    allFollower = followersBean.user_list!!
                    filterEachFollower()
                    updateList(FriendListAdapter.STYLE_DEFAULT)
                }

                override fun onFail(error: String) {
                    Toast.makeText(activity, "request data error: $error", Toast.LENGTH_SHORT)
                        .show()
                    list_friends!!.showEmptyView()
                }
            })
    }

    private fun filterEachFollower() {
        shownFollower.clear()
        for (bean in allFollower) {
            if (bean.follow_status.equals(Constants.FOLLOW_STATUS_EACH)) {
                shownFollower.add(bean)
            }
        }
    }

    private fun filterFollowerByString(prefix: String) {
        shownFollower.clear()
        for (bean in allFollower) {
            if (bean.userid!!.startsWith(prefix)) {
                shownFollower.add(bean)
            }
        }
    }
}