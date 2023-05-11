package ru.net2fox.quester.ui.skill

/**
 * Результат запроса списка из базы данных: успех (данные о задаче) или сообщение об ошибке.
 */
data class SkillActionResult(
    val success: Boolean? = null,
    val error: Int? = null
)