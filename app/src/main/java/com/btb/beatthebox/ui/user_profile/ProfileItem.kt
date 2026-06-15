package com.btb.beatthebox.ui.user_profile


sealed class ProfileItem {

    data class Info(
        val icon: Int,
        val label: String,
        val value: String?
    ) : ProfileItem()

    data class EditablePhone(
        val icon: Int,
        val label: String,
        val phone: String?
    ) : ProfileItem()

    data class Action(
        val icon: Int,
        val label: String
    ) : ProfileItem()

    object Logout : ProfileItem()
    object DeleteAccount : ProfileItem()
}
