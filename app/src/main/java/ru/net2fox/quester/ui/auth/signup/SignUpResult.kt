package ru.net2fox.quester.ui.auth.signup

/**
 * Результат аутентификации: успех или сообщение об ошибке.
 */
data class SignUpResult(
    val success: Boolean? = null,
    val error: Int? = null
)