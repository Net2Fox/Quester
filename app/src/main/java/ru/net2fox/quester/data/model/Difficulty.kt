package ru.net2fox.quester.data.model

import androidx.annotation.StringRes
import ru.net2fox.quester.Quester
import ru.net2fox.quester.R

enum class Difficulty(@StringRes val nameString: Int) {
    EASY(R.string.easy_difficulty),
    MEDIUM(R.string.medium_difficulty),
    HARD(R.string.hard_difficulty);

    override fun toString(): String {
        return Quester.getContext().resources.getString(this.nameString)
    }
}