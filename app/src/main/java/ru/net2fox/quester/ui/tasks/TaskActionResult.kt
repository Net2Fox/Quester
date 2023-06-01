package ru.net2fox.quester.ui.tasks

/**
 * Результат запроса списка из базы данных: успех или сообщение об ошибке.
 */
data class TaskActionResult(
    val success: Boolean? = null,
    val error: Int? = null
)
