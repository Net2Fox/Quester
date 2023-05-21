package ru.net2fox.quester.data.database

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import ru.net2fox.quester.data.Result
import ru.net2fox.quester.data.model.Action
import ru.net2fox.quester.data.model.Difficulty
import ru.net2fox.quester.data.model.TaskList
import ru.net2fox.quester.data.model.Object
import ru.net2fox.quester.data.model.Skill
import ru.net2fox.quester.data.model.Task
import ru.net2fox.quester.data.model.User
import ru.net2fox.quester.data.model.UserLog
import java.util.Calendar
import java.util.Date

/**
 * Класс, который запрашивает информацию из удаленного источника данных
 * и поддерживает в памяти кэш информации
 */
class ModeratorRepository {

    //TODO Сделать кеширование, подробнее смотри в ChatGPT "Clean architecture и MVVM"
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    suspend fun getLogs(timestamp: Timestamp? = null): Result<QuerySnapshot> {
        return try {
            var currentDate = Timestamp.now()
            if (timestamp != null) {
                currentDate = timestamp
            }

            val startOfDay = Calendar.getInstance().apply {
                time = Date(currentDate.seconds * 1000)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }.time

            val endOfDay = Calendar.getInstance().apply {
                time = Date(currentDate.seconds * 1000)
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }.time

            val startOfDayTimestamp = Timestamp(startOfDay)
            val endOfDayTimestamp = Timestamp(endOfDay)
            val result = db.collection("logs")
                .orderBy("datetime")
                .whereGreaterThanOrEqualTo("datetime", startOfDayTimestamp)
                .whereLessThanOrEqualTo("datetime", endOfDayTimestamp)
                .get()
                .await()
            Result.Success(result)
        } catch (e: Exception) {
            Log.d("Firebase", e.message.toString())
            Result.Error(e)
        }
    }

    companion object {
        private var INSTANCE: ModeratorRepository? = null

        fun initialize() {
            if (INSTANCE == null) {
                INSTANCE = ModeratorRepository()
            }
        }

        fun get(): ModeratorRepository {
            return INSTANCE ?: throw IllegalStateException("ModeratorRepository must be initialized")
        }
    }
}