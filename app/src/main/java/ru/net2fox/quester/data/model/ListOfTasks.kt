package ru.net2fox.quester.data.model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class ListOfTasks(
    @get:Exclude
    var id: String? = null,
    @get:PropertyName("id")
    var incId: Long,
    var name: String,
    @get:Exclude
    var tasks: MutableList<Task>? = null
)
