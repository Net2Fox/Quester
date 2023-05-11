package ru.net2fox.quester.ui.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LeaderboardViewModelFactory  : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LeaderboardViewModel::class.java)) {
            return LeaderboardViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}