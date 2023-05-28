package ru.net2fox.quester.ui.character

import ru.net2fox.quester.data.model.UserSkill

/**
 * Результат запроса списка из базы данных: успех (список навыков) или сообщение об ошибке.
 */
class CharacterSkillResult (
    val success: List<UserSkill>? = null,
    val error: Int? = null
)