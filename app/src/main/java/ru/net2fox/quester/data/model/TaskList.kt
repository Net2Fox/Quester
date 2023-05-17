package ru.net2fox.quester.data.model

import com.google.firebase.firestore.Exclude

data class TaskList(
    @get:Exclude
    var strId: String? = null,
    var id: Long,
    var name: String,
    @get:Exclude
    var tasks: MutableList<Task>? = null,
    @field:JvmField
    var isDeleted: Boolean = false
)
