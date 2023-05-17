package ru.net2fox.quester.data.model

import androidx.annotation.StringRes
import ru.net2fox.quester.Quester
import ru.net2fox.quester.R

enum class Object(@StringRes val nameString: Int) {
    LIST(R.string.enum_object_list),
    TASK(R.string.enum_object_task),
    SKILL(R.string.enum_object_skill),
    ACCOUNT(R.string.enum_object_account);

    override fun toString(): String {
        return Quester.getContext().resources.getString(this.nameString)
    }
}