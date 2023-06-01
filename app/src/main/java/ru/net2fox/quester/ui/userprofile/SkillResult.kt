package ru.net2fox.quester.ui.userprofile

import ru.net2fox.quester.data.model.Skill


/**
 * Результат запроса списка из базы данных: успех (список навыков) или сообщение об ошибке.
 */
class SkillResult (
    val success: List<Skill>? = null,
    val error: Int? = null
)