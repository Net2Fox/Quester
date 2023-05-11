package ru.net2fox.quester.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class Skill(
    @get:Exclude
    var id: String? = null,
    @get:PropertyName("id")
    var incId: Long,
    var name: String,
    var experience: Int = 0,
    var needExperience: Int = 100,
    var level: Int = 1
) {
    override fun toString(): String {
        return name
    }
}
