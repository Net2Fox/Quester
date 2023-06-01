package ru.net2fox.quester.ui.userprofile

import ru.net2fox.quester.data.model.Skill


/**
 * Результат запроса списка из базы данных: успех или сообщение об ошибке.
 */
class AddSkillResult (
    val success: Boolean? = null,
    val error: Int? = null
)