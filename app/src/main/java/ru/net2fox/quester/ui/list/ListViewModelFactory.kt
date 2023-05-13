package ru.net2fox.quester.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Фабрика провайдера ViewModel для инстанцирования ListViewModel.
 * Требуется, чтобы у данной ListViewModel был непустой конструктор.
 */
class ListViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListViewModel::class.java)) {
            return ListViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}