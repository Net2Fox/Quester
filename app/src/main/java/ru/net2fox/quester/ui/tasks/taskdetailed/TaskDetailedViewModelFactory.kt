package ru.net2fox.quester.ui.tasks.taskdetailed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Фабрика провайдера ViewModel для инстанцирования TaskDetailedViewModel.
 * Требуется, чтобы у данной TaskDetailedViewModel был непустой конструктор.
 */
class TaskDetailedViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TaskDetailedViewModel::class.java)) {
            return TaskDetailedViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}