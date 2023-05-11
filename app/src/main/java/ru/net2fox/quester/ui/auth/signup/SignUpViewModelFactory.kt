package ru.net2fox.quester.ui.auth.signup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Фабрика провайдера ViewModel для инстанцирования SignUpViewModel.
 * Требуется, чтобы у данной SignUpViewModel был непустой конструктор.
 */
class SignUpViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
            return SignUpViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}