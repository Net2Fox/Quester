package ru.net2fox.quester.data.model

import com.google.firebase.firestore.Exclude

data class User(
    var name: String,
    var experience: Int,
    var level: Int,
    @field:JvmField
    var isModerator: Boolean = false,
    var listsCount: Long = -1,
    var skillsCount: Long = -1,
    var tasksCount: Long = -1,
    @get:Exclude
    var skills: MutableList<Skill>? = null
)
