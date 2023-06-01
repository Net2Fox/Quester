package ru.net2fox.quester.ui.auth.signup

/**
 * Результат регистрации: успех или сообщение об ошибке.
 */
data class SignUpResult(
    val success: Boolean? = null,
    val error: Int? = null
)