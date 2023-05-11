package ru.net2fox.quester.data.model

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class Task(
    @get:Exclude
    var id: String? = null,
    @get:PropertyName("id")
    var incId: Long,
    @get:Exclude
    var listId: String? = null, //TODO Сделать чтоб с Firebase запрашивался список, который передаётся в TaskFragment?
    var name: String,
    var difficulty: Difficulty,
    var description: String? = null,
    @field:JvmField
    var isExecuted: Boolean,
    @get:Exclude
    var listSkills: MutableList<Skill>? = null,
    var skills: MutableList<DocumentReference>? = mutableListOf()
)
