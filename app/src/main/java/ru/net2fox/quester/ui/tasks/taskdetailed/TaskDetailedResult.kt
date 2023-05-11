package ru.net2fox.quester.ui.tasks.taskdetailed

import ru.net2fox.quester.data.model.Task

/**
 * Результат запроса списка из базы данных: успех (данные о задаче) или сообщение об ошибке.
 */
data class TaskDetailedResult(
    val success: Task? = null,
    val error: Int? = null
)