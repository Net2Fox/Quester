package ru.net2fox.quester.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

class UserLog (
    @get:Exclude
    var strId: String? = null,
    @get:PropertyName("id")
    var id: Long,
    @get:PropertyName("user")
    var userRef: DocumentReference,
    @get:Exclude
    var user: User? = null,
    @get:PropertyName("object")
    var objectRef: DocumentReference,
    @get:Exclude
    var obj: Any? = null,
    @get:ServerTimestamp
    var datetime: Timestamp? = null,
    var action: Action,
    var objectType: Object
)