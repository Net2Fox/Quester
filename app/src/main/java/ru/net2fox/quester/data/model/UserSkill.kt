package ru.net2fox.quester.data.model

import com.google.firebase.firestore.Exclude

data class UserSkill(
    @get:Exclude
    var strId: String? = null,
    var id: Long,
    var name: String,
    var experience: Int = 0,
    var needExperience: Int = 100,
    var level: Int = 1,
    @field:JvmField
    var isDeleted: Boolean = false,
) {
    override fun toString(): String {
        return name
    }
}
