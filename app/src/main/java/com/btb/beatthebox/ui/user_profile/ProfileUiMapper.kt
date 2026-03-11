package com.btb.beatthebox.ui.user_profile

import android.util.Log
import com.btb.beatthebox.R
import com.btb.beatthebox.data.model.User

object ProfileUiMapper {

    fun map(user: User): List<ProfileItem> {
        Log.d("ProfileUiMapper", "User email: ${user.email} phone: ${user.phoneNo} new user: ${user.newUser} name: ${user.name}")

        return listOf(
            ProfileItem.Info(
                icon = R.drawable.outline_alternate_email_24,
                label = "Email",
                value = user.email
            ),
            ProfileItem.EditablePhone(
                icon = R.drawable.phone,
                label = "Phone",
                phone = user.phoneNo
            ),
            ProfileItem.Action(R.drawable.booking_history, "Booking History"),
            ProfileItem.Action(R.drawable.settings, "Settings"),
            ProfileItem.Action(R.drawable.help, "FAQ & Help"),
            ProfileItem.Action(R.drawable.ic_privacy, "Privacy & Others"),
            ProfileItem.Logout
        )
    }

}