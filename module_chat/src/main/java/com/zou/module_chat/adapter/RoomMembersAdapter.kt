package com.zou.module_chat.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ty.web3mq.http.beans.MemberBean
import com.zou.module_chat.R
import java.util.ArrayList

class RoomMembersAdapter(memberBeans: ArrayList<MemberBean>, context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    private val memberBeans: ArrayList<MemberBean>
    private var onAddPeopleClickListener: OnAddPeopleClickListener? = null
    private val context: Context

    init {
        this.memberBeans = memberBeans
        this.context = context
    }

    fun setOnAddPeopleClickListener(onAddPeopleClickListener: OnAddPeopleClickListener?) {
        this.onAddPeopleClickListener = onAddPeopleClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_ADD_PEOPLE) {
            val v: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_room_member_add_people, parent, false)
            AddPeopleViewHolder(v)
        } else {
            val v: View = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_room_member, parent, false)
            MembersViewHolder(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        var realPosition = 0
        if (position > 0) {
            realPosition = position - 1
        }
        val memberBean: MemberBean = memberBeans[realPosition]
        if (holder is AddPeopleViewHolder) {
            val viewHolder = holder as AddPeopleViewHolder
            viewHolder.itemView.setOnClickListener(View.OnClickListener {
                if (onAddPeopleClickListener != null) {
                    onAddPeopleClickListener!!.onAddPeopleClick()
                }
            })
        } else if (holder is MembersViewHolder) {
            val viewHolder = holder as MembersViewHolder
            //            Glide.with(context).load()
//            viewHolder.iv_avatar
            viewHolder.tv_userid.setText(memberBean.userid)
        }
    }

    internal inner class AddPeopleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    internal inner class MembersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var iv_avatar: ImageView
        var tv_userid: TextView

        init {
            iv_avatar = itemView.findViewById(R.id.iv_avatar)
            tv_userid = itemView.findViewById(R.id.tv_userid)
        }
    }

    interface OnAddPeopleClickListener {
        fun onAddPeopleClick()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) {
            VIEW_TYPE_ADD_PEOPLE
        } else {
            VIEW_TYPE_MEMBER
        }
    }

    override fun getItemCount(): Int {
        return memberBeans.size + 1
    }

    companion object {
        private const val VIEW_TYPE_ADD_PEOPLE = 0
        private const val VIEW_TYPE_MEMBER = 1
    }
}