package ru.net2fox.quester.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

class UserLog (
    @get:Exclude
    var strId: String? = null,
    var id: Long,
    var userRef: DocumentReference,
    @get:Exclude
    var user: User? = null,
    @get:Exclude
    var userName: String? = null,
    var objectRef: DocumentReference,
    @get:Exclude
    var objectName: String? = null,
    @get:Exclude
    var obj: Any? = null,
    @get:ServerTimestamp
    var datetime: Timestamp? = null,
    var action: Action,
    var objectType: Object
)