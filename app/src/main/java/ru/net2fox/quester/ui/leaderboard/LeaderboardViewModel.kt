package ru.net2fox.quester.ui.leaderboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.QuerySnapshot
import ru.net2fox.quester.R
import ru.net2fox.quester.data.Result
import ru.net2fox.quester.data.database.DatabaseRepository
import ru.net2fox.quester.data.model.User

class LeaderboardViewModel : ViewModel() {

    private val databaseRepository = DatabaseRepository.get()

    private val _usersResult = MutableLiveData<LeaderboardResult>()
    val usersResult: LiveData<LeaderboardResult> = _usersResult


    suspend fun getUserLeaderboard() {
        val result = databaseRepository.getUserLeaderboard()

        if (result is Result.Success) {
            val query: QuerySnapshot = result.data;
            val mutableUsers: MutableList<User> = mutableListOf()
            for (postDocument in query) {
                mutableUsers.add(
                    User(
                        postDocument.getString("name")!!,
                        postDocument.get("experience", Int::class.java)!!,
                        postDocument.get("level", Int::class.java)!!
                    )
                )
            }
            _usersResult.postValue(LeaderboardResult(success = mutableUsers))
        } else {
            _usersResult.postValue(LeaderboardResult(error = R.string.get_data_error))
        }
    }
}