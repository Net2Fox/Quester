package ru.net2fox.quester.ui.skill

import ru.net2fox.quester.data.model.Skill

/**
 * Результат запроса списка из базы данных: успех (данные о задаче) или сообщение об ошибке.
 */
class SkillResult(
    val success: Skill? = null,
    val error: Int? = null
)