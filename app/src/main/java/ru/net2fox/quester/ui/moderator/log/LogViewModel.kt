package ru.net2fox.quester.ui.moderator.log

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import ru.net2fox.quester.R
import ru.net2fox.quester.data.Result
import ru.net2fox.quester.data.database.ModeratorRepository
import ru.net2fox.quester.data.model.Action
import ru.net2fox.quester.data.model.Object
import ru.net2fox.quester.data.model.UserLog

class LogViewModel : ViewModel() {

    private val moderatorRepository = ModeratorRepository.get()

    private val _logResult = MutableLiveData<LogResult>()
    val logResult: LiveData<LogResult> = _logResult

    suspend fun getLogs() {
        val result = moderatorRepository.getLogs()

        if (result is Result.Success) {
            val query: QuerySnapshot = result.data
            val mutableLog: MutableList<UserLog> = mutableListOf()
            for (document in query) {
                val userRef = document.getDocumentReference("userRef")!!
                val objectRef = document.getDocumentReference("objectRef")!!
                val userName = userRef.get().await().getString("name")!!
                var objectName: String? = objectRef.get().await().getString("name")
                val log = UserLog(
                    strId = document.id,
                    id = document.getLong("id")!!,
                    userRef = userRef,
                    userName = userName,
                    objectRef = objectRef,
                    datetime = document.getTimestamp("datetime")!!,
                    action = document.get("action", Action::class.java)!!,
                    objectType = document.get("objectType", Object::class.java)!!,
                    objectName = objectName
                )
                mutableLog.add(log)
            }
            _logResult.postValue(LogResult(success = mutableLog))
        } else {
            _logResult.postValue(LogResult(error = R.string.get_data_error))
        }
    }
}