package ru.net2fox.quester.ui.skill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class SkillViewModelFactory : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SkillViewModel::class.java)) {
            return SkillViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}