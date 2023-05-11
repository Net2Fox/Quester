package ru.net2fox.quester.ui.auth.signin

/**
 * Результат аутентификации: успех или сообщение об ошибке.
 */
data class SignInResult(
    val success: Boolean? = null,
    val error: Int? = null
)