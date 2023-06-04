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
import ru.net2fox.quester.data.model.UserSkill
import ru.net2fox.quester.data.model.Task
import ru.net2fox.quester.ui.tasks.TaskActionResult
import ru.net2fox.quester.ui.userprofile.UserProfileResult
import ru.net2fox.quester.ui.userprofile.UserSkillResult

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
            var userSkills: MutableList<UserSkill>? = null
            val skillsRef = document.get("skills") as ArrayList<DocumentReference>?
            if (skillsRef != null) {
                userSkills = mutableListOf()
                for (skill in skillsRef){
                    val skillDocument = skill.get().await()
                    userSkills.add(UserSkill(
                        strId = skillDocument.id,
                        id = skillDocument.get("id", Long::class.java)!!,
                        nameRU = skillDocument.getString("nameRU")!!,
                        nameEN = skillDocument.getString("nameEN")!!,
                        experience = skillDocument.get("experience", Int::class.java)!!,
                        level = skillDocument.get("level", Int::class.java)!!
                    ))

                }
            }
            _taskDetailedResult.postValue(
                TaskDetailedResult(success = Task(
                    strId = taskId,
                    listId = listId,
                    id = document.get("id", Long::class.java)!!,
                    name = document.getString("name")!!,
                    difficulty = document.get("difficulty", Difficulty::class.java)!!,
                    description = document.getString("description")!!,
                    isExecuted = document.getBoolean("isExecuted")!!,
                    listUserSkills = userSkills,
                    skills = skillsRef
            ))
            )
        } else {
            _taskDetailedResult.postValue(TaskDetailedResult(error = R.string.get_data_error))
        }
    }

    suspend fun saveTask(task: Task, haveChanges: Boolean) {
        if (haveChanges && task.listUserSkills != null) {
            if (task.skills == null) {
                task.skills = mutableListOf()
            }
            for (skill in task.listUserSkills!!) {
                _skillsRef.find { t ->
                    t.id == skill.strId
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
        if (haveChanges && task.listUserSkills != null) {
            if (task.skills == null) {
                task.skills = mutableListOf()
            }
            for (skill in task.listUserSkills!!) {
                _skillsRef.find { t ->
                    t.id == skill.strId
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

    private val _skillsResult = MutableLiveData<UserSkillResult>()
    val skillsResult: LiveData<UserSkillResult> = _skillsResult

    private val _skillsRef: MutableList<DocumentReference> = mutableListOf()

    suspend fun getSkills() {
        val result = databaseRepository.getUserSkills()

        if (result is Result.Success) {
            val query: QuerySnapshot = result.data;
            for (document in query.documents) {
                _skillsRef.add(document.reference)
            }
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
            _skillsResult.postValue(UserSkillResult(success = mutableUserSkills))
        } else {
            _skillsResult.postValue(UserSkillResult(error = R.string.get_data_error))
        }
    }

}