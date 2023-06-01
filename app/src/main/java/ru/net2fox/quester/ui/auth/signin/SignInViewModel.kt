package ru.net2fox.quester.ui.auth.signin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.util.Patterns
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import ru.net2fox.quester.data.Result
import ru.net2fox.quester.R
import ru.net2fox.quester.data.auth.AuthRepository
import ru.net2fox.quester.data.model.FirebaseBlockedAccountException
import ru.net2fox.quester.data.model.FirebaseDeletedAccountException

class SignInViewModel : ViewModel() {

    private val authRepository = AuthRepository.get()

    private val _signInForm = MutableLiveData<SignInFormState>()
    val signInFormState: LiveData<SignInFormState> = _signInForm

    private val _signInResult = MutableLiveData<SignInResult>()
    val signInResult: LiveData<SignInResult> = _signInResult

    suspend fun signIn(email: String, password: String) {
        val result = authRepository.signIn(email, password)

        if (result is Result.Success) {
            _signInResult.postValue(SignInResult(success = true))
        } else {
            when ((result as Result.Error).exception) {
                is FirebaseAuthInvalidUserException -> {
                    // Обработка ошибки, когда пользователь с таким email не найден
                    _signInResult.postValue(SignInResult(error = R.string.sign_in_failed_email))
                }
                is FirebaseAuthInvalidCredentialsException -> {
                    // Обработка ошибки, когда пользователь ввёл неверный пароль
                    _signInResult.postValue(SignInResult(error = R.string.sign_in_failed_password))
                }
                is FirebaseNetworkException -> {
                    // Обработка ошибки если нет доступа в интернет
                    _signInResult.postValue(SignInResult(error = R.string.auth_failed_network))
                }
                is FirebaseDeletedAccountException -> {
                    // Обработка ошибки удалённого аккаунта
                    _signInResult.postValue(SignInResult(error = R.string.sign_in_failed_account_deleted))
                }
                is FirebaseBlockedAccountException -> {
                    // Обработка ошибки заблокированного аккаунта
                    _signInResult.postValue(SignInResult(error = R.string.sign_in_failed_account_blocked))
                }
                else -> {
                    // Общая обработка ошибки
                    _signInResult.postValue(SignInResult(error = R.string.sign_in_failed))
                }
            }
        }
    }

    fun signInDataChanged(email: String, password: String) {
        if (!isEmailValid(email)) {
            _signInForm.value = SignInFormState(usernameError = R.string.invalid_email)
        } else if (!isPasswordValid(password)) {
            _signInForm.value = SignInFormState(passwordError = R.string.invalid_password)
        } else {
            _signInForm.value = SignInFormState(isDataValid = true)
        }
    }

    // Проверка адреса почты
    private fun isEmailValid(username: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(username).matches()
    }

    // Проверка пароля
    private fun isPasswordValid(password: String): Boolean {
        return password.length > 5
    }
}