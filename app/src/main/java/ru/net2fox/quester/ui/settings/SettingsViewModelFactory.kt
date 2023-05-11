package ru.net2fox.quester.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.net2fox.quester.ui.list.ListViewModel

/**
 * Фабрика провайдера ViewModel для инстанцирования SettingsViewModel.
 * Требуется, чтобы у данной ListViewModel был непустой конструктор.
 */
class SettingsViewModelFactory  : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}