package ru.net2fox.quester.ui.auth.signup

/**
 * Состояние проверки данных формы регистрации в систему.
 */
data class SignUpFormState(
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val isDataValid: Boolean = false
)