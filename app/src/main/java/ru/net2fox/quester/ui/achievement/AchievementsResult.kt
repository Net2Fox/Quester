package ru.net2fox.quester.ui.achievement

import ru.net2fox.quester.data.model.Achievement

class AchievementsResult (
    val success: List<Achievement>? = null,
    val error: Int? = null
)