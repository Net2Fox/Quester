package ru.net2fox.quester.ui.character

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import ru.net2fox.quester.R
import ru.net2fox.quester.data.Result
import ru.net2fox.quester.data.database.DatabaseRepository
import ru.net2fox.quester.data.model.Skill
import ru.net2fox.quester.data.model.Task
import ru.net2fox.quester.data.model.User
import ru.net2fox.quester.ui.list.ListActionResult
import ru.net2fox.quester.ui.tasks.TaskActionResult

class CharacterViewModel : ViewModel() {

    private val databaseRepository = DatabaseRepository.get()

    private val _skillResult = MutableLiveData<CharacterSkillResult>()
    val skillResult: LiveData<CharacterSkillResult> = _skillResult

    private val _skillActionResult = MutableLiveData<CharacterSkillActionResult>()
    val skillActionResult: LiveData<CharacterSkillActionResult> = _skillActionResult

    private val _characterResult = MutableLiveData<CharacterResult>()
    val characterResult: LiveData<CharacterResult> = _characterResult

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
            _skillResult.postValue(CharacterSkillResult(success = mutableSkills))
        } else {
            _skillResult.postValue(CharacterSkillResult(error = R.string.get_data_error))
        }
    }

    suspend fun getUser() {
        val result = databaseRepository.getUser()

        if (result is Result.Success) {
            val document: DocumentSnapshot = result.data;
            _characterResult.postValue(CharacterResult(success = User(
                document.getString("name")!!,
                document.get("experience", Int::class.java)!!,
                document.get("level", Int::class.java)!!
            )
            ))
        } else {
            _characterResult.postValue(CharacterResult(error = R.string.get_data_error))
        }
    }

    suspend fun createSkill(skillName: String) {
        val result = databaseRepository.createSkill(skillName)

        if (result is Result.Success) {
            _skillActionResult.postValue(CharacterSkillActionResult(success = true))
        } else {
            _skillActionResult.postValue(CharacterSkillActionResult(error = R.string.get_data_error))
        }
    }
}