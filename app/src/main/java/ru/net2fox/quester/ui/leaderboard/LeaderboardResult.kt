package ru.net2fox.quester.ui.leaderboard

import ru.net2fox.quester.data.model.User

class LeaderboardResult (
    val success: List<User>? = null,
    val error: Int? = null
)