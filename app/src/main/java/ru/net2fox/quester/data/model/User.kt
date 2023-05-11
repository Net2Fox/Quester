package ru.net2fox.quester.data.model

import com.google.firebase.firestore.Exclude

data class User(
    var name: String,
    var experience: Int,
    var level: Int,
    @get:Exclude
    var skills: MutableList<Skill>? = null
)
