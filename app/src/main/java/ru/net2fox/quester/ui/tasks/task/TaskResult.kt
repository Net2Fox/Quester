package ru.net2fox.quester.ui.tasks.task

import ru.net2fox.quester.data.model.Task

/**
 * Результат запроса списка из базы данных: успех (данные о задаче) или сообщение об ошибке.
 */
data class TaskResult(
    val success: List<Task>? = null,
    val error: Int? = null
)