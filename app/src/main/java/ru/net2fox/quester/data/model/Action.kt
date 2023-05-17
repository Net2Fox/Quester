package ru.net2fox.quester.data.model

import androidx.annotation.StringRes
import ru.net2fox.quester.Quester
import ru.net2fox.quester.R

enum class Action(@StringRes val descriptionString: Int) {
    CREATE(R.string.enum_action_create),
    EDIT(R.string.enum_action_edit),
    DELETE(R.string.enum_action_delete),
    MARK_COMPLETE(R.string.enum_action_mark_complete);

    override fun toString(): String {
        return Quester.getContext().resources.getString(this.descriptionString)
    }
}