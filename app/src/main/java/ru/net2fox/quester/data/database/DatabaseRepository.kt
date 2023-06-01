package ru.net2fox.quester.data.database

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import ru.net2fox.quester.data.Result
import ru.net2fox.quester.data.model.Action
import ru.net2fox.quester.data.model.Difficulty
import ru.net2fox.quester.data.model.TaskList
import ru.net2fox.quester.data.model.Object
import ru.net2fox.quester.data.model.Skill
import ru.net2fox.quester.data.model.UserSkill
import ru.net2fox.quester.data.model.Task
import ru.net2fox.quester.data.model.User
import ru.net2fox.quester.data.model.UserLog
import java.util.Locale

/**
 * Класс, который запрашивает информацию из удаленного источника данных
 * и поддерживает в памяти кэш информации
 */
class DatabaseRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    private lateinit var user: FirebaseUser
    private lateinit var userReference: DocumentReference

    private var skillLastId: Long = 0
    private var listLastId: Long = 0
    private var taskLastId: Long = 0


    init {
        initializeUser()
    }

    fun initializeUser() {
        try {
            user = firebaseAuth.currentUser!!
            userReference = db.collection("users").document(user.uid)
        } catch (e: Exception) {
            Log.d("Firebase", e.message.toString())
        }
    }

    suspend fun getLastId() {
        try {
            val userSnapshot = db.collection("users")
                .document(user.uid)
                .get().await()
            val currentUser = User(
                userSnapshot.getString("name")!!,
                userSnapshot.get("experience", Int::class.java)!!,
                userSnapshot.get("level", Int::class.java)!!,
                listsCount = userSnapshot.get("listsCount", Long::class.java)!!,
                skillsCount = userSnapshot.get("skillsCount", Long::class.java)!!,
                tasksCount = userSnapshot.get("tasksCount", Long::class.java)!!,
                isDeleted = userSnapshot.getBoolean("isDeleted")!!
            )
            listLastId = currentUser.listsCount
            taskLastId = currentUser.tasksCount
            skillLastId = currentUser.skillsCount
        } catch (_: Exception) {
        }
    }

    private suspend fun addId(obj: String) {
        val changeId: Map<String, Long> = when (obj) {
            "lists" -> {
                hashMapOf(
                    obj + "Count" to ++listLastId
                )
            }

            "tasks" -> {
                hashMapOf(
                    obj + "Count" to ++taskLastId
                )
            }

            "skills" -> {
                hashMapOf(
                    obj + "Count" to ++skillLastId
                )
            }

            else -> {
                throw Exception("No ID")
            }
        }
        userReference.set(changeId, SetOptions.merge()).await()
    }

    suspend fun getUserLeaderboard(): Result<QuerySnapshot> {
        return try {
            val result = db.collection("users")
                .whereEqualTo("isDeleted", false)
                .orderBy("level", Query.Direction.DESCENDING)
                .orderBy("experience", Query.Direction.DESCENDING)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .await()
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getUser(): Result<DocumentSnapshot> {
        return try {
            val result = db.collection("users")
                .document(user.uid)
                .get()
                .await()
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getUserById(userId: String): Result<DocumentSnapshot>  {
        return try {
            val result = db.collection("users")
                .document(userId)
                .get()
                .await()
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun initializeNewUser(): Boolean {
        return try {
            createNewUserData()
            firebaseAuth.signOut()
            true
        } catch (e: Exception) {
            Log.d("FirebaseAuth", e.message.toString())
            false
        }
    }

    private suspend fun createNewUserData(): Boolean {
        return try {
            val user = firebaseAuth.currentUser!!
            val userRef = db.collection("users").document(user.uid)
            userRef.set(User(
                name = user.displayName!!,
                experience = 0,
                level = 1)).await()
            getLastId()
            writeLog(userRef, Action.CREATE, Object.ACCOUNT)
            true
        } catch (e: Exception) {
            Log.d("FirebaseAuth", e.message.toString())
            false
        }
    }

    private suspend fun deleteUserData(): Boolean {
        return try {
            val userRef = db.collection("users").document(user.uid)
            userRef.set(hashMapOf(
                "isDeleted" to true
            ), SetOptions.merge()).await()
            writeLog(userRef, Action.DELETE, Object.ACCOUNT)
            true
        } catch (e: Exception) {
            e.message?.let { Log.d("deleteUserData", it) }
            false
        }
    }

    suspend fun progressReset(): Boolean {
        return try {
            deleteUserData() && createNewUserData()
        } catch (e: Exception) {
            e.message?.let { Log.d("progressReset", it) }
            false
        }
    }

    suspend fun deleteAccount(): Boolean {
        return try {
            val result = deleteUserData()
            if (result) {
                try {
                    firebaseAuth.signOut()
                } catch (e: Exception) {
                    return false
                }
                return true
            } else {
                return false
            }
        } catch (e: Exception) {
            e.message?.let { Log.d("deleteAccount", it) }
            false
        }
    }

    suspend fun getListsOfTasks(): Result<QuerySnapshot> {
        return try {
            val result = db.collection("users")
                .document(user.uid)
                .collection("lists")
                .whereEqualTo("isDeleted", false)
                .orderBy("id")
                .get()
                .await()
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun createListOfTasks(listName: String): Result<DocumentReference> {
        return try {
            val result = db.collection("users")
                .document(user.uid)
                .collection("lists")
                .add(
                    TaskList(
                        id = listLastId,
                        name = listName
                    )
                )
                .await()
            addId("lists")
            writeLog(result, Action.CREATE, Object.LIST)
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun editListOfTasksName(listId: String, listName: String): Boolean {
        return try {
            val newListName: Map<String, Any> = hashMapOf(
                "name" to listName
            )
            val listRef = db.collection("users")
                .document(user.uid)
                .collection("lists")
                .document(listId)
            listRef
                .update(newListName)
                .await()
            writeLog(listRef, Action.EDIT, Object.LIST)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteListOfTasks(listId: String): Boolean {
        return try {
            val listRef = db.collection("users")
                .document(user.uid)
                .collection("lists")
                .document(listId)
            listRef
                .set(hashMapOf(
                    "isDeleted" to true
                ), SetOptions.merge())
                .await()
            writeLog(listRef, Action.DELETE, Object.LIST)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getTasks(listId: String): Result<QuerySnapshot> {
        return try {
            val result = db.collection("users")
                .document(user.uid)
                .collection("lists")
                .document(listId)
                .collection("tasks")
                .whereEqualTo("isDeleted", false)
                .orderBy("id")
                .get()
                .await()
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getTask(listId: String, taskId: String): Result<DocumentSnapshot> {
        return try {
            val result = db.collection("users")
                .document(user.uid)
                .collection("lists")
                .document(listId)
                .collection("tasks")
                .document(taskId)
                .get()
                .await()
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun createTask(listId: String, taskName: String): Result<DocumentReference> {
        return try {
            val result = db.collection("users")
                .document(user.uid)
                .collection("lists")
                .document(listId)
                .collection("tasks")
                .add(
                    Task(
                        id = taskLastId,
                        name = taskName,
                        difficulty = Difficulty.EASY,
                        description = "Описание",
                        isExecuted = false
                    )
                )
                .await()
            addId("tasks")
            writeLog(result, Action.CREATE, Object.TASK)
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun editTask(task: Task): Boolean {
        return try {
            db.collection("users")
                .document(user.uid)
                .collection("lists")
                .document(task.listId!!)
                .collection("tasks")
                .document(task.strId!!)
                .set(task, SetOptions.merge())
                .await()
            writeLog(task, Action.EDIT)
            true
        } catch (e: Exception) {
            false
        }
    }

    // TODO Убрать возможность снятия отметки о выполнении с задачи
    suspend fun taskMarkChange(
        task: Task,
        isComplete: Boolean,
        haveChanges: Boolean = true
    ): Boolean {
        return try {
            if (haveChanges) {
                db.collection("users")
                    .document(user.uid)
                    .collection("lists")
                    .document(task.listId!!)
                    .collection("tasks")
                    .document(task.strId!!)
                    .set(task, SetOptions.merge())
                    .await()
            } else {
                val taskMarkChange: Map<String, Boolean> = hashMapOf(
                    "isExecuted" to isComplete
                )
                db.collection("users")
                    .document(user.uid)
                    .collection("lists")
                    .document(task.listId!!)
                    .collection("tasks")
                    .document(task.strId!!)
                    .update(taskMarkChange)
                    .await()
            }

            if ((task.skills?.size ?: 0) != 0 && task.isExecuted) {
                var skillChange: Map<String, Int>
                for (skillRef in task.skills!!) {
                    // Добавление опыта навыку
                    val skill = skillRef.get().await()
                    skillChange = if ((skill.get(
                            "experience",
                            Int::class.java
                        )!! + task.difficulty.addExp) >= skill.get("needExperience", Int::class.java)!!
                    ) {
                        hashMapOf(
                            "level" to skill.get("level", Int::class.java)!! + 1,
                            "experience" to (skill.get(
                                "experience",
                                Int::class.java
                            )!! + task.difficulty.addExp) - 100,
                            "needExperience" to skill.get("needExperience", Int::class.java)!! * 2
                        )

                    } else {
                        hashMapOf(
                            "experience" to skill.get("experience", Int::class.java)!! + task.difficulty.addExp
                        )
                    }
                    skillRef
                        .update(skillChange)
                        .await()
                }
                // Добавление опыта на аккаунт
                val userDoc = db.collection("users")
                    .document(user.uid).get().await()
                val userExpChange: Map<String, Double> =
                    if (userDoc.get("experience", Int::class.java)!! >= 1000) {
                        hashMapOf(
                            "level" to userDoc.get("level", Double::class.java)!! + 1,
                            "experience" to (userDoc.get(
                                "experience",
                                Double::class.java
                            )!! + (task.difficulty.addExp.toDouble() / 2)) - 100
                        )
                    } else {
                        hashMapOf(
                            "experience" to userDoc.get(
                                "experience",
                                Double::class.java
                            )!! + (task.difficulty.addExp.toDouble() / 2)
                        )
                    }
                db.collection("users")
                    .document(user.uid)
                    .update(userExpChange)
                    .await()
            }
            writeLog(task, Action.MARK_COMPLETE)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteTask(task: Task): Boolean {
        return try {
            db.collection("users")
                .document(user.uid)
                .collection("lists")
                .document(task.listId!!)
                .collection("tasks")
                .document(task.strId!!)
                .set(hashMapOf(
                    "isDeleted" to true
                ), SetOptions.merge())
                .await()
            writeLog(task, Action.DELETE)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getSkills(): Result<QuerySnapshot> {
        return try {
            val result = db.collection("skills")
                .orderBy(
                    if (Locale.getDefault().language.equals(Locale("ru").language)) {
                        "nameRU"
                    } else {
                        "nameEN"
                    }
                )
                .get()
                .await()
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getUserSkills(): Result<QuerySnapshot> {
        return try {
            val result = db.collection("users")
                .document(user.uid)
                .collection("skills")
                .whereEqualTo("isDeleted", false)
                .orderBy("id")
                .get()
                .await()
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getUserSkillsById(userId: String): Result<QuerySnapshot> {
        return try {
            val result = db.collection("users")
                .document(userId)
                .collection("skills")
                .whereEqualTo("isDeleted", false)
                .orderBy("id")
                .get()
                .await()
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getUserSkill(skillId: String): Result<DocumentSnapshot> {
        return try {
            val result = db.collection("users")
                .document(user.uid)
                .collection("skills")
                .document(skillId)
                .get()
                .await()
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun addSkill(skill: Skill): Boolean {
        return try {
            val result = db.collection("users")
                .document(user.uid)
                .collection("skills")
                .add(
                    UserSkill(
                        id = skillLastId,
                        nameRU = skill.nameRU,
                        nameEN = skill.nameEN
                    )
                )
                .await()
            addId("skills")
            writeLog(result, Action.CREATE, Object.SKILL)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteUserSkill(userSkill: UserSkill): Boolean {
        return try {
            db.collection("users")
                .document(user.uid)
                .collection("skills")
                .document(userSkill.strId!!)
                .set(hashMapOf(
                    "isDeleted" to true
                ), SetOptions.merge())
                .await()
            writeLog(userSkill, Action.DELETE)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun writeLog(obj: TaskList, action: Action): Boolean {
        return try {
            val objectRef = db.collection("users").document(user.uid)
                .collection("lists").document(obj.strId!!)
            db.collection("logs")
                .add(
                    UserLog(
                        userRef = userReference,
                        objectRef = objectRef,
                        action = action,
                        objectType = Object.LIST
                    )
                ).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun writeLog(obj: Task, action: Action): Boolean {
        return try {
            val objectRef = db.collection("users").document(user.uid)
                .collection("lists").document(obj.listId!!)
                .collection("tasks").document(obj.strId!!)
            db.collection("logs")
                .add(
                    UserLog(
                        userRef = userReference,
                        objectRef = objectRef,
                        action = action,
                        objectType = Object.TASK
                    )
                ).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun writeLog(obj: UserSkill, action: Action): Boolean {
        return try {
            val objectRef = db.collection("users").document(user.uid)
                .collection("skills").document(obj.strId!!)
            db.collection("logs")
                .add(
                    UserLog(
                        userRef = userReference,
                        objectRef = objectRef,
                        action = action,
                        objectType = Object.SKILL
                    )
                ).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun writeLog(
        objectRef: DocumentReference,
        action: Action,
        objectType: Object
    ): Boolean {
        return try {
            db.collection("logs")
                .add(
                    UserLog(
                        userRef = userReference,
                        objectRef = objectRef,
                        action = action,
                        objectType = objectType
                    )
                ).await()
            true
        } catch (e: Exception) {
            Log.d("QuesterFirebase", e.message.toString())
            false
        }
    }

    companion object {
        private var INSTANCE: DatabaseRepository? = null

        fun initialize() {
            if (INSTANCE == null) {
                INSTANCE = DatabaseRepository()
            }
        }

        fun get(): DatabaseRepository {
            return INSTANCE ?: throw IllegalStateException("DatabaseRepository must be initialized")
        }
    }
}