package ru.net2fox.quester.ui.moderator.log

import android.text.format.DateUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import ru.net2fox.quester.R
import ru.net2fox.quester.data.Result
import ru.net2fox.quester.data.database.ModeratorRepository
import ru.net2fox.quester.data.model.Action
import ru.net2fox.quester.data.model.Object
import ru.net2fox.quester.data.model.UserLog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogViewModel : ViewModel() {

    private val moderatorRepository = ModeratorRepository.get()

    private val _logResult = MutableLiveData<LogResult>()
    val logResult: LiveData<LogResult> = _logResult

    private var logs: List<UserLog>? = null

    private var filterDate: Date? = null
    private var filterUsername: String? = null
    private var filterTimestamp: Timestamp? = null

    suspend fun getLogs(timestamp: Timestamp? = null) {
        if (timestamp != null) {
            filterTimestamp = timestamp
        }
        val result = moderatorRepository.getLogs(filterTimestamp)

        if (result is Result.Success) {
            val query: QuerySnapshot = result.data
            val mutableLog: MutableList<UserLog> = mutableListOf()
            for (document in query) {
                val userRef = document.getDocumentReference("userRef")!!
                val objectRef = document.getDocumentReference("objectRef")!!
                val user: DocumentSnapshot = userRef.get().await()
                val userName = userRef.get().await().getString("name")!!
                val userId = userRef.get().await().id
                val objectName: String? = if (document.get("objectType", Object::class.java)!! == Object.SKILL) {
                    if (Locale.getDefault().language.equals(Locale("ru").language)){
                        objectRef.get().await().getString("nameRU")
                    } else {
                        objectRef.get().await().getString("nameEN")
                    }
                } else {
                    objectRef.get().await().getString("name")
                }
                val log = UserLog(
                    strId = document.id,
                    userRef = userRef,
                    userName = userName,
                    userId = userId,
                    userIsBlocked = user.getBoolean("isBlocked")!!,
                    objectRef = objectRef,
                    datetime = document.getTimestamp("datetime")!!,
                    action = document.get("action", Action::class.java)!!,
                    objectType = document.get("objectType", Object::class.java)!!,
                    objectName = objectName
                )
                mutableLog.add(log)
            }
            logs = mutableLog.filter { u -> !u.userIsBlocked }
            filterUsername = null
            filterDate = null
            _logResult.postValue(LogResult(success = logs))
        } else {
            _logResult.postValue(LogResult(error = R.string.get_data_error))
        }
    }

    fun clearLogs() {
        logs = null
        _logResult.postValue(LogResult(success = logs))
    }

    fun filterLogsByUser(username: String?) {
        if (username == null) {
            _logResult.postValue(LogResult(success = logs))
        } else {
            val logsList = logs?.filter {
                it.userName?.lowercase()?.contains(username.lowercase())!!
            }
            _logResult.postValue(LogResult(success = logsList))
        }
    }
}