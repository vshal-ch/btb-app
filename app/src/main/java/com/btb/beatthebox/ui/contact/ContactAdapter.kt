package com.btb.beatthebox.ui.contact

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.btb.beatthebox.R
import com.btb.beatthebox.data.model.Contact
import com.google.android.material.button.MaterialButton

class ContactAdapter(
    private val contacts: List<Contact>,
    private val onEmailClick: (String) -> Unit,
    private val onPhoneClick: (String) -> Unit,
    private val onWhatsAppClick: (String) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    inner class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvName)
        val tvEmail: TextView = view.findViewById(R.id.tvEmail)
        val emailContainer: View = view.findViewById(R.id.emailContainer)
        val phoneContainer: LinearLayout = view.findViewById(R.id.phoneContainer)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.tvName.text = contact.name

        // Email
        if (!contact.email.isNullOrEmpty()) {
            holder.tvEmail.text = contact.email
            holder.emailContainer.visibility = View.VISIBLE
            holder.emailContainer.setOnClickListener { onEmailClick(contact.email!!) }
        } else {
            holder.emailContainer.visibility = View.GONE
        }

        // Clear old phone views
        holder.phoneContainer.removeAllViews()

        // Add phone buttons dynamically
        for (phone in contact.phones) {
            val button = MaterialButton(
                holder.itemView.context,
                null,
                com.google.android.material.R.attr.materialButtonOutlinedStyle
            ).apply {
                text = phone.phoneNo
                setPadding(16, 8, 16, 8)
                when (phone.type) {
                    "tel" -> {
                        icon = ContextCompat.getDrawable(context, R.drawable.baseline_phone_24)
                        setOnClickListener { onPhoneClick(phone.phoneNo) }
                    }

                    "whatsapp" -> {
                        icon = ContextCompat.getDrawable(context, R.drawable.ic_whatsapp)
                        setOnClickListener { onWhatsAppClick(phone.phoneNo) }
                    }

                    else -> {
                        icon = null
                    }
                }

                iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
            }

            holder.phoneContainer.addView(button)
        }
    }

    override fun getItemCount() = contacts.size
}
