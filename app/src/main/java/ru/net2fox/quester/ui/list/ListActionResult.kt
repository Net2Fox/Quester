package ru.net2fox.quester.ui.list

import ru.net2fox.quester.data.model.ListOfTasks

//TODO Переписать это. Вообще можно убрать и сделать просто возврат true или false
data class ListActionResult(
    val success: Boolean? = null,
    val error: Int? = null
)
