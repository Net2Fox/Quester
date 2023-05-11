package ru.net2fox.quester.ui.auth.signin

/**
 * Состояние проверки данных формы входа в систему.
 */
data class SignInFormState(
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val isDataValid: Boolean = false
)