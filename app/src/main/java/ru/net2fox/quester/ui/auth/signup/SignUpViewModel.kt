package ru.net2fox.quester.ui.auth.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import ru.net2fox.quester.data.Result
import ru.net2fox.quester.R
import ru.net2fox.quester.data.auth.AuthRepository

class SignUpViewModel : ViewModel() {

    private val authRepository = AuthRepository.get()

    private val _signUpForm = MutableLiveData<SignUpFormState>()
    val signUpFormState: LiveData<SignUpFormState> = _signUpForm

    private val _signUpResult = MutableLiveData<SignUpResult>()
    val signUpResult: LiveData<SignUpResult> = _signUpResult

    suspend fun signUp(username: String, email: String, password: String) {
        val result = authRepository.signUp(username, email, password)

        if (result is Result.Success) {
            _signUpResult.postValue(SignUpResult(success = true))
        } else {
            when ((result as Result.Error).exception) {
                is FirebaseAuthUserCollisionException -> {
                    // Обработка ошибки, когда пользователь уже зарегистрирован
                    _signUpResult.postValue(SignUpResult(error = R.string.sign_up_failed_email))
                }
                is FirebaseNetworkException -> {
                    // Обработка ошибки если нет доступа в интернет
                    _signUpResult.postValue(SignUpResult(error = R.string.auth_failed_network))
                }
                is FirebaseAuthInvalidCredentialsException -> {
                    _signUpResult.postValue(SignUpResult(error = R.string.sign_up_failed_email_format))
                }
                else -> {
                    // Общая обработка ошибки
                    _signUpResult.postValue(SignUpResult(error = R.string.sign_up_failed))
                }
            }
        }
    }

    fun signUpDataChanged(username: String,email: String, password: String, passwordConfirm: String) {
        if (!isUserNameValid(username)) {
            _signUpForm.value = SignUpFormState(usernameError = R.string.invalid_username)
        } else if (!isEmailValid(email)) {
            _signUpForm.value = SignUpFormState(usernameError = R.string.invalid_email)
        } else if (!isPasswordValid(password)) {
            _signUpForm.value = SignUpFormState(passwordError = R.string.invalid_password)
        } else if (!isPasswordConfirmed(password, passwordConfirm)) {
            _signUpForm.value = SignUpFormState(passwordError = R.string.invalid_password_confirm)
        } else {
            _signUpForm.value = SignUpFormState(isDataValid = true)
        }
    }

    // Проверка достоверности имени пользователя
    private fun isUserNameValid(username: String): Boolean {
        return username.isNotBlank()
    }

    // Проверка достоверности электронной почты
    private fun isEmailValid(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Проверка валидности пароля
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }

    // Проверка подтверждения пароля
    private fun isPasswordConfirmed(password: String, passwordConfirm: String): Boolean {
        return password == passwordConfirm
    }
}