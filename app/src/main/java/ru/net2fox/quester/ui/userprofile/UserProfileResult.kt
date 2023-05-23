package ru.net2fox.quester.ui.userprofile

import ru.net2fox.quester.data.model.User
import ru.net2fox.quester.data.model.UserLog

data class UserProfileResult(
    val success: User? = null,
    val error: Int? = null
)