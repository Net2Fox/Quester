package ru.net2fox.quester.ui.skill

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import ru.net2fox.quester.R
import ru.net2fox.quester.data.Result
import ru.net2fox.quester.data.database.DatabaseRepository
import ru.net2fox.quester.data.model.Difficulty
import ru.net2fox.quester.data.model.Skill
import ru.net2fox.quester.data.model.Task
import ru.net2fox.quester.ui.tasks.TaskActionResult
import ru.net2fox.quester.ui.tasks.task.TaskResult
import ru.net2fox.quester.ui.tasks.taskdetailed.TaskDetailedResult

class SkillViewModel : ViewModel() {

    private val databaseRepository = DatabaseRepository.get()

    private val _skillResult = MutableLiveData<SkillResult>()
    val skillResult: LiveData<SkillResult> = _skillResult

    private val _skillActionResult = MutableLiveData<SkillActionResult>()
    val skillActionResult: LiveData<SkillActionResult> = _skillActionResult

    suspend fun getSkill(skillId: String) {
        val result = databaseRepository.getSkill(skillId)

        if (result is Result.Success) {
            val document: DocumentSnapshot = result.data;
            _skillResult.postValue(
                SkillResult(success = Skill(
                    document.id,
                    document.get("id", Long::class.java)!!,
                    document.getString("name")!!,
                    document.get("experience", Int::class.java)!!,
                    document.get("level", Int::class.java)!!
                )
                )
            )
        } else {
            _skillResult.postValue(SkillResult(error = R.string.get_data_error))
        }
    }

    suspend fun saveSkill(skill: Skill) {
        val result = databaseRepository.editSkill(skill)

        if (result) {
            _skillActionResult.postValue(SkillActionResult(success = true))
        } else {
            _skillActionResult.postValue(SkillActionResult(error = R.string.get_data_error))
        }
    }

    suspend fun deleteSkill(skill: Skill) {
        val result = databaseRepository.deleteSkill(skill)

        if (result) {
            _skillActionResult.postValue(SkillActionResult(success = true))
        } else {
            _skillActionResult.postValue(SkillActionResult(error = R.string.get_data_error))
        }
    }
}