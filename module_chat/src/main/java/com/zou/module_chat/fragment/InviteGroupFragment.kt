package com.zou.module_chat.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ty.module_common.config.Constants
import com.ty.web3mq.Web3MQFollower
import com.ty.web3mq.Web3MQGroup
import com.ty.web3mq.http.beans.FollowerBean
import com.ty.web3mq.http.beans.FollowersBean
import com.ty.web3mq.http.beans.GroupBean
import com.ty.web3mq.interfaces.GetMyFollowingCallback
import com.ty.web3mq.interfaces.InvitationGroupCallback
import com.ty.web3mq.utils.ConvertUtil
import com.zou.module_chat.R
import com.zou.module_chat.adapter.InviteGroupAdapter

object InviteGroupFragment : BottomSheetDialogFragment() {
    private const val TAG = "InviteGroupFragment"
    private var mBehavior: BottomSheetBehavior<*>? = null
    private var list_invite_group: RecyclerView? = null
    private var adapter: InviteGroupAdapter? = null
    private var dialog: BottomSheetDialog? = null
    private var allFollower: ArrayList<FollowerBean> = ArrayList<FollowerBean>()
    private val shownFollower: ArrayList<FollowerBean> = ArrayList<FollowerBean>()
    private val group_id: String? = null
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        requestData()
        val view = View.inflate(context, R.layout.dialog_bottom_sheet_invite_group, null)
        initView(view)
        dialog!!.setContentView(view)
        mBehavior = BottomSheetBehavior.from(view.parent as View)
        return dialog!!
    }

    override fun onStart() {
        super.onStart()
        mBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
    }

    private fun requestData() {
        Web3MQFollower.getFollowerAndFollowing(0, 500, object : GetMyFollowingCallback{
                override fun onSuccess(response: String) {
                    shownFollower.clear()
                    val followersBean: FollowersBean =
                        ConvertUtil.convertJsonToFollowersBean(response)!!
                    allFollower = followersBean.user_list!!
                    filterEachFollower()
                    adapter = InviteGroupAdapter(shownFollower, activity!!)
                    list_invite_group!!.setAdapter(adapter!!)
                }

                override fun onFail(error: String) {
                    Toast.makeText(activity, "request data error: $error", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun initView(rootView: View) {
        list_invite_group = rootView.findViewById<RecyclerView>(R.id.list_invite_group)
        list_invite_group!!.setLayoutManager(
            LinearLayoutManager(
                activity,
                LinearLayoutManager.VERTICAL,
                false
            )
        )
        val btn_add = rootView.findViewById<Button>(R.id.btn_add)
        val iv_back = rootView.findViewById<ImageView>(R.id.iv_back)
        btn_add.setOnClickListener {
            if (adapter!!.getCheckedFollower().size > 0) {
                val invite_user_id = ArrayList<String>()
                for (follower in adapter!!.getCheckedFollower()) {
                    invite_user_id.add(follower.userid!!)
                }
                val bundle = arguments
                val group_id = bundle!!.getString("group_id")
                inviteMember(group_id, invite_user_id)
            }
        }
        iv_back.setOnClickListener { dismiss() }
    }

    private fun filterEachFollower() {
        shownFollower.clear()
        for (bean in allFollower) {
            if (bean.follow_status.equals(Constants.FOLLOW_STATUS_EACH)) {
                shownFollower.add(bean)
            }
        }
    }

    private fun inviteMember(group_id: String?, userIds: ArrayList<String>) {
        val ids = arrayOf<String>()
        Web3MQGroup.invite(group_id!!, userIds.toArray(ids), object : InvitationGroupCallback {
                override fun onSuccess(invitationGroupBean: GroupBean) {
                    dismiss()
                }

                override fun onFail(error: String) {
                    Toast.makeText(activity, "invite member fail error:$error", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }
}