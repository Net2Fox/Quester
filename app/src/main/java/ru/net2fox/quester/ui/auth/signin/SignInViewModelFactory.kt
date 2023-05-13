package ru.net2fox.quester.ui.auth.signin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Фабрика провайдера ViewModel для инстанцирования SignInViewModel.
 * Требуется, чтобы у данной SignInViewModel был непустой конструктор.
 */
class SignInViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignInViewModel::class.java)) {
            return SignInViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}