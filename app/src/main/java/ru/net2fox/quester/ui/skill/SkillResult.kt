package ru.net2fox.quester.ui.skill

import ru.net2fox.quester.data.model.UserSkill

/**
 * Результат запроса списка из базы данных: успех (данные о задаче) или сообщение об ошибке.
 */
class SkillResult(
    val success: UserSkill? = null,
    val error: Int? = null
)