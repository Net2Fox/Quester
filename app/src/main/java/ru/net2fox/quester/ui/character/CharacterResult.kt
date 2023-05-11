package ru.net2fox.quester.ui.character

import ru.net2fox.quester.data.model.Skill
import ru.net2fox.quester.data.model.User

/**
 * Результат запроса списка из базы данных: успех (User) или сообщение об ошибке.
 */
class CharacterResult (
    val success: User? = null,
    val error: Int? = null
)