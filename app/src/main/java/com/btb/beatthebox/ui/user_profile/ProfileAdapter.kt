package com.btb.beatthebox.ui.user_profile

import com.btb.beatthebox.R
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class ProfileAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var items: List<ProfileItem> = emptyList()

    var onEditPhone: (() -> Unit)? = null
    var onActionClick: ((String) -> Unit)? = null
    var onLogout: (() -> Unit)? = null
    var onDeleteAccount: (() -> Unit)? = null

    override fun getItemViewType(position: Int): Int = when (items[position]) {
        is ProfileItem.Info -> 0
        is ProfileItem.EditablePhone -> 1
        is ProfileItem.Action -> 2
        is ProfileItem.Logout -> 3
        is ProfileItem.DeleteAccount -> 4
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            0 -> InfoVH(inflater.inflate(R.layout.item_profile_info, parent, false))
            1 -> PhoneVH(inflater.inflate(R.layout.item_profile_editable_phone, parent, false))
            2 -> ActionVH(inflater.inflate(R.layout.item_profile_action, parent, false))
            3 -> LogoutVH(inflater.inflate(R.layout.item_profile_logout, parent, false))
            else -> DeleteAccountVH(inflater.inflate(R.layout.item_profile_delete, parent, false));
        }
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is ProfileItem.Info -> (holder as InfoVH).bind(item)
            is ProfileItem.EditablePhone -> (holder as PhoneVH).bind(item)
            is ProfileItem.Action -> (holder as ActionVH).bind(item)
            is ProfileItem.Logout -> (holder as LogoutVH).bind()
            is ProfileItem.DeleteAccount -> (holder as DeleteAccountVH).bind()
        }
    }

    class InfoVH(view: View) : RecyclerView.ViewHolder(view) {
        private val icon: ImageView = view.findViewById(R.id.icon)
        private val label: TextView = view.findViewById(R.id.label)
        private val value: TextView = view.findViewById(R.id.value)

        fun bind(item: ProfileItem.Info) {
            icon.setImageResource(item.icon)
            label.text = item.label
            value.text = item.value ?: "-"
        }
    }

    inner class PhoneVH(view: View) : RecyclerView.ViewHolder(view) {
        private val icon: ImageView = view.findViewById(R.id.icon)
        private val label: TextView = view.findViewById(R.id.label)
        private val value: TextView = view.findViewById(R.id.value)
        private val edit: ImageButton = view.findViewById(R.id.edit)

        fun bind(item: ProfileItem.EditablePhone) {
            icon.setImageResource(item.icon)
            label.text = item.label
            value.text = item.phone ?: "Add phone number"
            edit.setOnClickListener { onEditPhone?.invoke() }
        }
    }

    inner class ActionVH(view: View) : RecyclerView.ViewHolder(view) {
        private val icon: ImageView = view.findViewById(R.id.icon)
        private val label: TextView = view.findViewById(R.id.label)

        fun bind(item: ProfileItem.Action) {
            icon.setImageResource(item.icon)
            label.text = item.label
            itemView.setOnClickListener { onActionClick?.invoke(item.label) }
        }
    }

    inner class LogoutVH(view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {
            itemView.setOnClickListener { onLogout?.invoke() }
        }
    }

    inner class DeleteAccountVH(view: View) : RecyclerView.ViewHolder(view) {
        fun bind() {
            itemView.setOnClickListener { onDeleteAccount?.invoke() }
        }
    }
}
