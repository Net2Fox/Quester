package ru.net2fox.quester.data.model

import androidx.annotation.StringRes
import ru.net2fox.quester.Quester
import ru.net2fox.quester.R

enum class Difficulty(@StringRes val nameString: Int, val addExp: Int) {
    EASY(R.string.easy_difficulty, 5),
    MEDIUM(R.string.medium_difficulty, 15),
    HARD(R.string.hard_difficulty, 30);

    override fun toString(): String {
        return Quester.getContext().resources.getString(this.nameString)
    }
}