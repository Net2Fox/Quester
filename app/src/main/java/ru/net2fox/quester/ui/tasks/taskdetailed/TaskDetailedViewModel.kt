package ru.net2fox.quester.ui.tasks.taskdetailed

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
import ru.net2fox.quester.data.model.Difficulty
import ru.net2fox.quester.data.model.Skill
import ru.net2fox.quester.data.model.Task
import ru.net2fox.quester.ui.character.CharacterSkillResult
import ru.net2fox.quester.ui.tasks.TaskActionResult

class TaskDetailedViewModel : ViewModel() {

    private val databaseRepository = DatabaseRepository.get()

    private val _taskDetailedResult = MutableLiveData<TaskDetailedResult>()
    val taskDetailedResult: LiveData<TaskDetailedResult> = _taskDetailedResult

    private val _taskActionResult = MutableLiveData<TaskActionResult>()
    val taskActionResult: LiveData<TaskActionResult> = _taskActionResult

    suspend fun getTask(listId: String, taskId: String) {
        val result = databaseRepository.getTask(listId, taskId)

        if (result is Result.Success) {
            val document: DocumentSnapshot = result.data;
            var skills: MutableList<Skill>? = null
            val skillsRef = document.get("skills") as ArrayList<DocumentReference>?
            if (skillsRef != null) {
                skills = mutableListOf()
                for (skill in skillsRef){
                    val skillDocument = skill.get().await()
                    skills.add(Skill(
                        id = skillDocument.id,
                        incId = skillDocument.get("id", Long::class.java)!!,
                        name = skillDocument.getString("name")!!,
                        experience = skillDocument.get("experience", Int::class.java)!!,
                        level = skillDocument.get("level", Int::class.java)!!
                    ))

                }
            }
            _taskDetailedResult.postValue(
                TaskDetailedResult(success = Task(
                    id = taskId,
                    listId = listId,
                    incId = document.get("id", Long::class.java)!!,
                    name = document.getString("name")!!,
                    difficulty = document.get("difficulty", Difficulty::class.java)!!,
                    description = document.getString("description")!!,
                    isExecuted = document.getBoolean("isExecuted")!!,
                    listSkills = skills,
                    skills = skillsRef
            ))
            )
        } else {
            _taskDetailedResult.postValue(TaskDetailedResult(error = R.string.get_data_error))
        }
    }

    suspend fun saveTask(task: Task) {
        if (task.listSkills != null) {
            if (task.skills == null) {
                task.skills = mutableListOf()
            }
            for (skill in task.listSkills!!) {
                _skillsRef.find { t ->
                    t.id == skill.id
                }?.let {
                    task.skills!!.add(it)
                }
            }
        }
        val result = databaseRepository.editTask(task)

        if (result) {
            _taskActionResult.postValue(TaskActionResult(success = true))
        } else {
            _taskActionResult.postValue(TaskActionResult(error = R.string.get_data_error))
        }
    }

    suspend fun taskMarkChange(task: Task, isComplete: Boolean, haveChanges: Boolean = false) {
        if (haveChanges && task.listSkills != null) {
            if (task.skills == null) {
                task.skills = mutableListOf()
            }
            for (skill in task.listSkills!!) {
                _skillsRef.find { t ->
                    t.id == skill.id
                }?.let {
                    task.skills!!.add(it)
                }
            }
        }
        val result = databaseRepository.taskMarkChange(task, isComplete, haveChanges)

        if (result) {
            _taskActionResult.postValue(TaskActionResult(success = true))
        } else {
            _taskActionResult.postValue(TaskActionResult(error = R.string.get_data_error))
        }
    }

    suspend fun deleteTask(task: Task) {
        val result = databaseRepository.deleteTask(task)

        if (result) {
            _taskActionResult.postValue(TaskActionResult(success = true))
        } else {
            _taskActionResult.postValue(TaskActionResult(error = R.string.get_data_error))
        }
    }

    private val _skillsResult = MutableLiveData<CharacterSkillResult>()
    val skillsResult: LiveData<CharacterSkillResult> = _skillsResult

    private val _skillsRef: MutableList<DocumentReference> = mutableListOf()

    suspend fun getSkills() {
        val result = databaseRepository.getSkills()

        if (result is Result.Success) {
            val query: QuerySnapshot = result.data;
            for (document in query.documents) {
                _skillsRef.add(document.reference)
            }
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
            _skillsResult.postValue(CharacterSkillResult(success = mutableSkills))
        } else {
            _skillsResult.postValue(CharacterSkillResult(error = R.string.get_data_error))
        }
    }

}