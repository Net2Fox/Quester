package ru.net2fox.quester.data.model

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Exclude

data class Task(
    @get:Exclude
    var strId: String? = null,
    var id: Long,
    @get:Exclude
    var listId: String? = null, //TODO Сделать чтоб с Firebase запрашивался список, который передаётся в TaskFragment?
    var name: String,
    var difficulty: Difficulty,
    var description: String? = null,
    @field:JvmField
    var isExecuted: Boolean,
    @field:JvmField
    var isDeleted: Boolean = false,
    @get:Exclude
    var listUserSkills: MutableList<UserSkill>? = null,
    var skills: MutableList<DocumentReference>? = mutableListOf()
)
