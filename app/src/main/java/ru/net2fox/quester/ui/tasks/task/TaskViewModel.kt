package ru.net2fox.quester.ui.tasks.task

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import ru.net2fox.quester.R
import ru.net2fox.quester.data.Result
import ru.net2fox.quester.data.database.DatabaseRepository
import ru.net2fox.quester.data.model.Difficulty
import ru.net2fox.quester.data.model.Skill
import ru.net2fox.quester.data.model.Task
import ru.net2fox.quester.ui.tasks.TaskActionResult

class TaskViewModel : ViewModel() {

    private val databaseRepository = DatabaseRepository.get()

    private val _taskResult = MutableLiveData<TaskResult>()
    val taskResult: LiveData<TaskResult> = _taskResult

    private val _taskActionResult = MutableLiveData<TaskActionResult>()
    val taskActionResult: LiveData<TaskActionResult> = _taskActionResult

    suspend fun getTasks(listId: String) {
        val result = databaseRepository.getTasks(listId)

        if (result is Result.Success) {
            val query: QuerySnapshot = result.data;
            val mutableTasks: MutableList<Task> = mutableListOf()
            for (postDocument in query) {
                var skills: MutableList<Skill>? = null
                val skillsRef = postDocument.get("skills") as ArrayList<DocumentReference>?
                if (skillsRef != null) {
                    skills = mutableListOf()
                    for (skill in skillsRef){
                        val skillDocument = skill.get().await()
                        skills.add(
                            Skill(
                                strId = skillDocument.id,
                                id = skillDocument.get("id", Long::class.java)!!,
                                name = skillDocument.getString("name")!!,
                                experience = skillDocument.get("experience", Int::class.java)!!,
                                level = skillDocument.get("level", Int::class.java)!!
                            )
                        )

                    }
                }
                mutableTasks.add(Task(
                    postDocument.id,
                    postDocument.get("id", Long::class.java)!!,
                    listId,
                    postDocument.getString("name")!!,
                    postDocument.get("difficulty", Difficulty::class.java)!!,
                    postDocument.getString("description")!!,
                    postDocument.getBoolean("isExecuted")!!,
                    skills,
                    skillsRef
                ))
            }
            _taskResult.postValue(TaskResult(success = mutableTasks))
        } else {
            _taskResult.postValue(TaskResult(error = R.string.get_data_error))
        }
    }

    suspend fun saveTask(task: Task, isComplete: Boolean) {
        val result = databaseRepository.taskMarkChange(task, isComplete, false)

        if (result) {
            _taskActionResult.postValue(TaskActionResult(success = true))
        } else {
            _taskActionResult.postValue(TaskActionResult(error = R.string.get_data_error))
        }
    }
}