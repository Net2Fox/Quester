package ru.net2fox.quester.ui.tasks.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Фабрика провайдера ViewModel для инстанцирования ListViewModel.
 * Требуется, чтобы у данной ListViewModel был непустой конструктор.
 */
class TaskViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
            return TaskViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}