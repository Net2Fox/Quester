package ru.net2fox.quester.data.model

import java.util.Locale

data class Skill(
    var id: String,
    var nameRU: String,
    var nameEN: String
) {
    override fun toString(): String {
        return if (Locale.getDefault().language.equals(Locale("ru").language)) {
            nameRU
        } else {
            nameEN
        }
    }
}
