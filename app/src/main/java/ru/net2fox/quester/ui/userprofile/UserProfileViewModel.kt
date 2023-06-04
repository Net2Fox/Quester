package ru.net2fox.quester.ui.userprofile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import ru.net2fox.quester.R
import ru.net2fox.quester.data.Result
import ru.net2fox.quester.data.database.DatabaseRepository
import ru.net2fox.quester.data.database.ModeratorRepository
import ru.net2fox.quester.data.model.Achievement
import ru.net2fox.quester.data.model.Skill
import ru.net2fox.quester.data.model.UserSkill
import ru.net2fox.quester.data.model.User

class UserProfileViewModel : ViewModel() {

    private val databaseRepository = DatabaseRepository.get()

    private val _userProfileResult = MutableLiveData<UserProfileResult>()
    val userProfileResult: LiveData<UserProfileResult> = _userProfileResult

    private val _userSkillResult = MutableLiveData<UserSkillResult>()
    val userSkillResult: LiveData<UserSkillResult> = _userSkillResult

    private val _skillResult = MutableLiveData<SkillResult>()
    val skillResult: LiveData<SkillResult> = _skillResult

    private val _addSkillResult = MutableLiveData<AddSkillResult>()
    val addSkillResult: LiveData<AddSkillResult> = _addSkillResult

    private val _blockUserResult = MutableLiveData<Boolean>()
    val blockUserResult: LiveData<Boolean> = _blockUserResult

    private val _userAchievementResult = MutableLiveData<UserAchievementResult>()
    val userAchievementResult: LiveData<UserAchievementResult> = _userAchievementResult

    suspend fun getUser(userId: String? = null) {
        val result = if(userId == null) {
            databaseRepository.getUser()
        } else {
            databaseRepository.getUserById(userId)
        }

        if (result is Result.Success) {
            val document: DocumentSnapshot = result.data;
            val achievements: MutableList<Achievement> = mutableListOf()
            val achievementsRefs = document.get("achievementsRefs") as List<DocumentReference>
            for (ref in achievementsRefs) {
                val achiev = ref.get().await()
                val skill = achiev.getDocumentReference("skillRef")!!.get().await()
                achievements.add(Achievement(
                    nameRU = achiev.getString("nameRU")!!,
                    nameEN = achiev.getString("nameEN")!!,
                    skillLevel = achiev.getLong("skillLevel")!!,
                    skill = Skill(
                        skill.id,
                        skill.getString("nameRU")!!,
                        skill.getString("nameEN")!!
                    )
                ))
            }
            val skills: MutableList<UserSkill> = mutableListOf()
            val skillsRef = document.reference.collection("skills")
                .whereEqualTo("isDeleted", false)
                .orderBy("id").get().await()
            for (skill in skillsRef) {
                skills.add(
                    UserSkill(
                        skill.id,
                        skill.get("id", Long::class.java)!!,
                        skill.getString("nameRU")!!,
                        skill.getString("nameEN")!!,
                        skill.get("experience", Int::class.java)!!,
                        skill.get("needExperience", Int::class.java)!!,
                        skill.get("level", Int::class.java)!!
                )
                )
            }
            _userProfileResult.postValue(
                UserProfileResult(success = User(
                    document.id,
                    document.getString("name")!!,
                    document.get("experience", Int::class.java)!!,
                    document.get("level", Int::class.java)!!,
                    achievements = achievements,
                    userSkills = skills
                )
                )
            )
        } else {
            _userProfileResult.postValue(UserProfileResult(error = R.string.get_data_error))
        }
    }

    suspend fun blockUser(userId: String) {
        _blockUserResult.postValue(ModeratorRepository.get().blockUser(userId))
    }

    suspend fun getUserSkills(userId: String? = null) {
        val result = if(userId == null) {
            databaseRepository.getUserSkills()
        } else {
            databaseRepository.getUserSkillsById(userId)
        }

        if (result is Result.Success) {
            val query: QuerySnapshot = result.data;
            val mutableUserSkills: MutableList<UserSkill> = mutableListOf()
            for (postDocument in query) {
                mutableUserSkills.add(
                    UserSkill(
                        postDocument.id,
                        postDocument.get("id", Long::class.java)!!,
                        postDocument.getString("nameRU")!!,
                        postDocument.getString("nameEN")!!,
                        postDocument.get("experience", Int::class.java)!!,
                        postDocument.get("needExperience", Int::class.java)!!,
                        postDocument.get("level", Int::class.java)!!
                    )
                )
            }
            _userSkillResult.postValue(UserSkillResult(success = mutableUserSkills))
        } else {
            _userSkillResult.postValue(UserSkillResult(error = R.string.get_data_error))
        }
    }

    suspend fun addSkill(skill: Skill) {
        val result = databaseRepository.addSkill(skill)

        if (result) {
            _addSkillResult.postValue(AddSkillResult(success = true))
        } else {
            _addSkillResult.postValue(AddSkillResult(error = R.string.get_data_error))
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
                        postDocument.getString("nameRU")!!,
                        postDocument.getString("nameEN")!!
                    )
                )
            }
            _skillResult.postValue(SkillResult(success = mutableSkills))
        } else {
            _skillResult.postValue(SkillResult(error = R.string.get_data_error))
        }
    }
}