package ru.net2fox.quester.ui.list

import ru.net2fox.quester.data.model.ListOfTasks

/**
 * Результат запроса списка из базы данных: успех (данные о списке) или сообщение об ошибке.
 */
data class ListResult(
    val success: List<ListOfTasks>? = null,
    val error: Int? = null
)