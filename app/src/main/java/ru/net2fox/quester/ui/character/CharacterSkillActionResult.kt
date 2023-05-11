package ru.net2fox.quester.ui.character

/**
 * Результат запроса списка из базы данных: успех (данные о задаче) или сообщение об ошибке.
 */
data class CharacterSkillActionResult(
    val success: Boolean? = null,
    val error: Int? = null
)