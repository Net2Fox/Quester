package ru.net2fox.quester.ui.userprofile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import ru.net2fox.quester.R
import ru.net2fox.quester.data.Result
import ru.net2fox.quester.data.database.DatabaseRepository
import ru.net2fox.quester.data.model.Skill
import ru.net2fox.quester.data.model.User
import ru.net2fox.quester.ui.character.CharacterResult
import ru.net2fox.quester.ui.character.CharacterSkillResult

class UserProfileViewModel : ViewModel() {

    private val databaseRepository = DatabaseRepository.get()

    private val _userProfileResult = MutableLiveData<UserProfileResult>()
    val userProfileResult: LiveData<UserProfileResult> = _userProfileResult

    private val _skillResult = MutableLiveData<UserProfileSkillResult>()
    val skillResult: LiveData<UserProfileSkillResult> = _skillResult

    //suspend fun getUser() {
    //    //if (timestamp != null) {
    //    //    filterTimestamp = timestamp
    //    //}
    //    //val result = moderatorRepository.getLogs(filterTimestamp)
//
    //    //if (result is Result.Success) {
    //    //    val query: QuerySnapshot = result.data
    //    //    val mutableLog: MutableList<UserLog> = mutableListOf()
    //    //    for (document in query) {
    //    //        val userRef = document.getDocumentReference("userRef")!!
    //    //        val objectRef = document.getDocumentReference("objectRef")!!
    //    //        val userName = userRef.get().await().getString("name")!!
    //    //        var objectName: String? = objectRef.get().await().getString("name")
    //    //        val log = UserLog(
    //    //            strId = document.id,
    //    //            //id = document.getLong("id")!!,
    //    //            userRef = userRef,
    //    //            userName = userName,
    //    //            objectRef = objectRef,
    //    //            datetime = document.getTimestamp("datetime")!!,
    //    //            action = document.get("action", Action::class.java)!!,
    //    //            objectType = document.get("objectType", Object::class.java)!!,
    //    //            objectName = objectName
    //    //        )
    //    //        mutableLog.add(log)
    //    //    }
    //    //    logs = mutableLog
    //    //    filterUsername = null
    //    //    filterDate = null
    //    //    _logResult.postValue(LogResult(success = mutableLog))
    //    //} else {
    //    //    _logResult.postValue(LogResult(error = R.string.get_data_error))
    //    //}
    //}

    suspend fun getUser() {
        val result = databaseRepository.getUser()

        if (result is Result.Success) {
            val document: DocumentSnapshot = result.data;
            _userProfileResult.postValue(
                UserProfileResult(success = User(
                document.getString("name")!!,
                document.get("experience", Int::class.java)!!,
                document.get("level", Int::class.java)!!
            )
            )
            )
        } else {
            _userProfileResult.postValue(UserProfileResult(error = R.string.get_data_error))
        }
    }

    suspend fun getSkills() {
        val result = databaseRepository.getSkills()

        if (result is Result.Success) {
            val query: QuerySnapshot = result.data;
            val mutableSkills: MutableList<Skill> = mutableListOf()
            for (postDocument in query) {
                mutableSkills.add(
                    Skill(
                        postDocument.id,
                        postDocument.get("id", Long::class.java)!!,
                        postDocument.getString("name")!!,
                        postDocument.get("experience", Int::class.java)!!,
                        postDocument.get("needExperience", Int::class.java)!!,
                        postDocument.get("level", Int::class.java)!!
                    )
                )
            }
            _skillResult.postValue(UserProfileSkillResult(success = mutableSkills))
        } else {
            _skillResult.postValue(UserProfileSkillResult(error = R.string.get_data_error))
        }
    }
}