package ru.net2fox.quester.data.model

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Exclude

data class User(
    @get:Exclude
    var id: String? = null,
    var name: String,
    var experience: Int,
    var level: Int,
    @field:JvmField
    var isModerator: Boolean = false,
    @field:JvmField
    var isDeleted: Boolean = false,
    @field:JvmField
    var isBlocked: Boolean = false,
    var listsCount: Long = -1,
    var skillsCount: Long = -1,
    var tasksCount: Long = -1,
    @get:Exclude
    var userSkills: MutableList<UserSkill>? = null,
    var achievementsRefs: MutableList<DocumentReference>? = mutableListOf(),
    @get:Exclude
    var achievements: MutableList<Achievement>? = null
)
