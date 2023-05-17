package ru.net2fox.quester.ui.moderator.log

import ru.net2fox.quester.data.model.Task
import ru.net2fox.quester.data.model.UserLog

/**
 * Результат запроса списка из базы данных: успех (логи) или сообщение об ошибке.
 */
data class LogResult(
    val success: List<UserLog>? = null,
    val error: Int? = null
)