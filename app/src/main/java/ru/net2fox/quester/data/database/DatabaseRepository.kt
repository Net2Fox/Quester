package ru.net2fox.quester.data.database

import android.content.Context
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import ru.net2fox.quester.data.Result
import ru.net2fox.quester.data.model.ListOfTasks
import ru.net2fox.quester.data.model.Skill
import ru.net2fox.quester.data.model.Task

/**
 * Класс, который запрашивает информацию из удаленного источника данных
 * и поддерживает в памяти кэш информации
 */
class DatabaseRepository private constructor(context: Context) {

    private val databaseDataSource = DatabaseDataSource.get()

    suspend fun getUserLeaderboard(): Result<QuerySnapshot> {
        return databaseDataSource.getUserLeaderboard()
    }

    suspend fun getListsOfTasks(): Result<QuerySnapshot> {
        return databaseDataSource.getListsOfTasks()
    }

    suspend fun createListOfTasks(listName: String): Result<DocumentReference> {
        return databaseDataSource.createListOfTasks(listName)
    }

    suspend fun editListOfTasksName(listId: String, listName: String): Boolean {
        return databaseDataSource.editListOfTasksName(listId, listName)
    }

    suspend fun deleteListOfTasks(listId: String): Boolean {
        return databaseDataSource.deleteListOfTasks(listId)
    }

    suspend fun getUser(): Result<DocumentSnapshot> {
        return databaseDataSource.getUser()
    }

    suspend fun getTasks(listId: String): Result<QuerySnapshot> {
        return databaseDataSource.getTasks(listId)
    }

    suspend fun getTask(listId: String, taskId: String): Result<DocumentSnapshot> {
        return databaseDataSource.getTask(listId, taskId)
    }

    suspend fun createTask(listId: String, taskName: String): Result<DocumentReference>  {
        return databaseDataSource.createTask(listId, taskName)
    }

    suspend fun editTask(task: Task): Boolean {
        return databaseDataSource.editTask(task)
    }

    suspend fun taskMarkChange(task: Task, isComplete: Boolean, haveChanges: Boolean = true): Boolean {
        return databaseDataSource.taskMarkChange(task, isComplete, haveChanges)
    }

    suspend fun deleteTask(task: Task): Boolean {
        return databaseDataSource.deleteTask(task)
    }

    suspend fun getSkills(): Result<QuerySnapshot> {
        return databaseDataSource.getSkills()
    }

    suspend fun getSkill(skillId: String): Result<DocumentSnapshot> {
        return databaseDataSource.getSkill(skillId)
    }

    suspend fun createSkill(skillName: String): Result<DocumentReference>  {
        return databaseDataSource.createSkill(skillName)
    }

    suspend fun editSkill(skill: Skill): Boolean {
        return databaseDataSource.editSkill(skill)
    }

    suspend fun deleteSkill(skill: Skill): Boolean {
        return databaseDataSource.deleteSkill(skill)
    }

    suspend fun progressReset(): Boolean{
        return databaseDataSource.progressReset()
    }

    suspend fun deleteAccount(): Boolean{
        return databaseDataSource.deleteAccount()
    }

    companion object {
        private var INSTANCE: DatabaseRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = DatabaseRepository(context)
            }
        }

        fun get(): DatabaseRepository {
            return INSTANCE ?:
            throw IllegalStateException("DatabaseRepository must be initialized")
        }
    }
}