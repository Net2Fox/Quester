package ru.net2fox.quester.ui.userprofile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import ru.net2fox.quester.R
import ru.net2fox.quester.data.Result
import ru.net2fox.quester.data.database.DatabaseRepository
import ru.net2fox.quester.data.model.Skill
import ru.net2fox.quester.data.model.UserSkill
import ru.net2fox.quester.data.model.User

class UserProfileViewModel : ViewModel() {

    private val databaseRepository = DatabaseRepository.get()

    private val _userProfileResult = MutableLiveData<UserProfileResult>()
    val userProfileResult: LiveData<UserProfileResult> = _userProfileResult

    private val _userSkillResult = MutableLiveData<ProfileUserSkillResult>()
    val userSkillResult: LiveData<ProfileUserSkillResult> = _userSkillResult

    private val _skillResult = MutableLiveData<ProfileSkillResult>()
    val skillResult: LiveData<ProfileSkillResult> = _skillResult

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

    suspend fun getUserSkills() {
        val result = databaseRepository.getUserSkills()

        if (result is Result.Success) {
            val query: QuerySnapshot = result.data;
            val mutableUserSkills: MutableList<UserSkill> = mutableListOf()
            for (postDocument in query) {
                mutableUserSkills.add(
                    UserSkill(
                        postDocument.id,
                        postDocument.get("id", Long::class.java)!!,
                        postDocument.getString("name")!!,
                        postDocument.get("experience", Int::class.java)!!,
                        postDocument.get("needExperience", Int::class.java)!!,
                        postDocument.get("level", Int::class.java)!!
                    )
                )
            }
            _userSkillResult.postValue(ProfileUserSkillResult(success = mutableUserSkills))
        } else {
            _userSkillResult.postValue(ProfileUserSkillResult(error = R.string.get_data_error))
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
                        postDocument.getString("name")!!
                    )
                )
            }
            _skillResult.postValue(ProfileSkillResult(success = mutableSkills))
        } else {
            _skillResult.postValue(ProfileSkillResult(error = R.string.get_data_error))
        }
    }
}