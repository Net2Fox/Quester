package ru.net2fox.quester.ui.userprofile

import ru.net2fox.quester.data.model.Achievement

/**
 * Результат запроса списка из базы данных: успех (список достижений) или сообщение об ошибке.
 */
data class UserAchievementResult (
    val success: List<Achievement>? = null,
    val error: Int? = null
)