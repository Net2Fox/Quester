package ru.net2fox.quester.data.model

import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.Exclude
import java.util.Locale

data class Achievement(
    @get:Exclude
    var id: String? = null,
    var nameRU: String,
    var nameEN: String,
    var skillLevel: Long,
    val skillRef: DocumentReference? = null,
    @get:Exclude
    var skill: Skill? = null
) {
    override fun toString(): String {
        return if (Locale.getDefault().language.equals(Locale("ru").language)) {
            nameRU
        } else {
            nameEN
        }
    }
}
