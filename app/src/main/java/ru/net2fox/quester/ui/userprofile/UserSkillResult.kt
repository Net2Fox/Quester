package ru.net2fox.quester.ui.userprofile

import ru.net2fox.quester.data.model.UserSkill

/**
 * Результат запроса списка из базы данных: успех (список навыков) или сообщение об ошибке.
 */
data class UserSkillResult (
    val success: List<UserSkill>? = null,
    val error: Int? = null
)