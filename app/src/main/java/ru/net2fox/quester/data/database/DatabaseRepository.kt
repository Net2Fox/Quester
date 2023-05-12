package ru.net2fox.quester.data.database

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import ru.net2fox.quester.data.Result
import ru.net2fox.quester.data.model.Difficulty
import ru.net2fox.quester.data.model.ListOfTasks
import ru.net2fox.quester.data.model.Skill
import ru.net2fox.quester.data.model.Task
import ru.net2fox.quester.data.model.User

/**
 * Класс, который запрашивает информацию из удаленного источника данных
 * и поддерживает в памяти кэш информации
 */
class DatabaseRepository {

    //TODO Переписать всю часть получения данных, сделать кеширование, подробнее смотри в ChatGPT "Clean architecture и MVVM"
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val db = Firebase.firestore

    private var skillLastId: Long = -1
    private var listLastId: Long = -1
    private var taskLastId: Long = -1

    suspend fun getLastId() {
        try {
            val user = firebaseAuth.currentUser!!
            val userData = db.collection("users")
                .document(user.uid)

            val skillList = userData.collection("skills")
                .orderBy("id")
                .get()
                .await()
                .documents

            if (skillList.size != 0) {
                skillLastId = skillList
                    .last()
                    .get("id", Long::class.java)!!
            }

            val listsList = userData.collection("lists")
                .orderBy("id")
                .get()
                .await()
                .documents

            if (listsList.size != 0) {
                listLastId = listsList.last()
                    .get("id", Long::class.java)!!
            }

            val allTasks: MutableList<DocumentSnapshot> = mutableListOf()

            for (list in listsList) {
                val tasks = list.reference
                    .collection("tasks")
                    .get()
                    .await()
                    .documents
                if (tasks.size != 0) {
                    allTasks.addAll(tasks)
                }
            }
            allTasks.sortBy {
                it.get("id", Long::class.java)
            }
            taskLastId = allTasks.last().get("id", Long::class.java)!!
        } catch (e: Exception) {
            Log.d("SkillLastId", e.message.toString())
        }
    }

    suspend fun getUserLeaderboard(): Result<QuerySnapshot> {
        return try {
            val result = db.collection("users")
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
            val user = firebaseAuth.currentUser!!
            val result = db.collection("users")
                .document(user.uid)
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
            false
        }
    }

    private suspend fun createNewUserData(): Boolean {
        return try {
            val user = firebaseAuth.currentUser!!
            // Создание документа пользователя
            db.collection("users").document(user.uid)
                .set(User(user.displayName!!, 0, 1)).await()
            //// Создание вложенной коллекции навыков
            //db.collection("users").document(user.uid)
            //    .collection("skills")
            //    .add(Skill(incId = ++skillLastId, name = "Навык #1")).await()
            //// Создание вложенной коллекции списков
            //db.collection("users").document(user.uid)
            //    .collection("lists")
            //    .add(ListOfTasks(incId = ++listLastId, name = "Список #1"))
            //    .addOnSuccessListener {
            //        // Создание вложенной коллекции задач и добавление тестовой задачи
            //        it.collection("tasks")
            //            .add(Task(
            //                incId = ++taskLastId,
            //                name = "Задача #1",
            //                difficulty = Difficulty.EASY,
            //                description = "Описание",
            //                isExecuted = false
            //            ))
            //    }.await()
            getLastId()
            true
        } catch (e: Exception) {
            e.message?.let { Log.d("createNewUserData", it) }
            false
        }
    }

    private suspend fun deleteUserData(): Boolean {
        return try {
            val user = firebaseAuth.currentUser!!
            val querySnapshotTask = db.collection("users").document(user.uid).collection("lists").get().await()
            for (postDocument in querySnapshotTask) {
                if (!deleteCollection(postDocument.reference.collection("tasks"), 5)) {
                    return false
                }
            }
            if (deleteCollection(db.collection("users").document(user.uid).collection("lists"), 5) &&
                deleteCollection(db.collection("users").document(user.uid).collection("skills"), 5)) {
                db.collection("users").document(user.uid)
                    .delete()
                    .await()
                return true
            } else {
                return false
            }
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

    // TODO Повторная аутентификация для удаления пользователя? https://firebase.google.com/docs/auth/android/manage-users?hl=ru#delete_a_user
    suspend fun deleteAccount(): Boolean {
        return try {
            val result = deleteUserData()
            if (result) {
                try {
                    //firebaseAuth.currentUser!!.reauthenticate(
                    //    EmailAuthProvider
                    //    .getCredential("test@test.com", "Net2Fox2003!"))
                    //    .await()
                    firebaseAuth.currentUser!!.delete()
                        .await()
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

    private fun deleteCollection(collection: CollectionReference, batchSize: Int): Boolean {
        return try {
            // Retrieve a small batch of documents to avoid out-of-memory errors/
            var deleted = 0
            collection
                .limit(batchSize.toLong())
                .get()
                .addOnCompleteListener {
                    for (document in it.result.documents) {
                        document.reference.delete()
                        ++deleted
                    }
                    if (deleted >= batchSize) {
                        // retrieve and delete another batch
                        deleteCollection(collection, batchSize)
                    }
                }
            true
        } catch (e: Exception) {
            e.message?.let { Log.d("deleteCollection", it) }
            false
        }
    }


    suspend fun getListsOfTasks(): Result<QuerySnapshot> {
        return try {
            val user = firebaseAuth.currentUser!!
            val result = db.collection("users")
                .document(user.uid)
                .collection("lists")
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
            val user = firebaseAuth.currentUser!!
            val result = db.collection("users")
                .document(user.uid)
                .collection("lists")
                .add(ListOfTasks(
                    incId = ++listLastId,
                    name = listName
                ))
                .await()
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun editListOfTasksName(listId: String, listName: String): Boolean {
        return try {
            val user = firebaseAuth.currentUser!!
            val newListName: Map<String, Any> = hashMapOf(
                "name" to listName
            )
            db.collection("users")
                .document(user.uid)
                .collection("lists")
                .document(listId)
                .update(newListName)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteListOfTasks(listId: String): Boolean {
        return try {
            val user = firebaseAuth.currentUser!!
            if (deleteCollection(db.collection("users").document(user.uid)
                    .collection("lists")
                    .document(listId)
                    .collection("tasks"), 5)) {
                db.collection("users")
                    .document(user.uid)
                    .collection("lists")
                    .document(listId)
                    .delete()
                    .await()
                return true
            } else {
                return false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getTasks(listId: String): Result<QuerySnapshot> {
        return try {
            val user = firebaseAuth.currentUser!!
            val result = db.collection("users")
                .document(user.uid)
                .collection("lists")
                .document(listId)
                .collection("tasks")
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
            val user = firebaseAuth.currentUser!!
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
            val user = firebaseAuth.currentUser!!
            val result = db.collection("users")
                .document(user.uid)
                .collection("lists")
                .document(listId)
                .collection("tasks")
                .add(Task(
                    incId = ++taskLastId,//taskListLastId[listLastId]!! + 1,
                    name = taskName,
                    difficulty = Difficulty.EASY,
                    description = "Описание",
                    isExecuted = false
                ))
                .await()
            //taskListLastId[listLastId] = taskListLastId[listLastId]!! + 1
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun editTask(task: Task): Boolean {
        return try {
            val user = firebaseAuth.currentUser!!
            db.collection("users")
                .document(user.uid)
                .collection("lists")
                .document(task.listId!!)
                .collection("tasks")
                .document(task.id!!)
                .set(task, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun taskMarkChange(task: Task, isComplete: Boolean, haveChanges: Boolean = true): Boolean {
        return try {
            val user = firebaseAuth.currentUser!!
            if (haveChanges) {
                db.collection("users")
                    .document(user.uid)
                    .collection("lists")
                    .document(task.listId!!)
                    .collection("tasks")
                    .document(task.id!!)
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
                    .document(task.id!!)
                    .update(taskMarkChange)
                    .await()
            }

            if (task.skills != null && task.isExecuted) {
                var skillChange: Map<String, Int>
                val addExp: Int = when (task.difficulty) {
                    Difficulty.EASY -> {
                        5
                    }

                    Difficulty.MEDIUM -> {
                        15
                    }

                    Difficulty.HARD -> {
                        30
                    }
                }
                for (skillRef in task.skills!!) {
                    // Добавление опыта навыку
                    val skill = skillRef.get().await()
                    skillChange = if ((skill.get("experience", Int::class.java)!! + addExp) >= skill.get("needExperience", Int::class.java)!!) {
                        hashMapOf(
                            "level" to skill.get("level", Int::class.java)!! + 1,
                            "experience" to (skill.get("experience", Int::class.java)!! + addExp) - 100,
                            "needExperience" to skill.get("needExperience", Int::class.java)!! * 2
                        )

                    } else {
                        hashMapOf(
                            "experience" to skill.get("experience", Int::class.java)!! + addExp
                        )
                    }
                    skillRef
                        .update(skillChange)
                        .await()
                }
                // Добавление опыта на аккаунт
                val userDoc = db.collection("users")
                    .document(user.uid).get().await()
                val userExpChange: Map<String, Double> =  if (userDoc.get("experience", Int::class.java)!! >= 1000) {
                    hashMapOf(
                        "level" to userDoc.get("level", Double::class.java)!! + 1,
                        "experience" to (userDoc.get("experience", Double::class.java)!! + (addExp.toDouble() / 2)) - 100
                    )
                } else {
                    hashMapOf(
                        "experience" to userDoc.get("experience", Double::class.java)!! + (addExp.toDouble() / 2)
                    )
                }
                db.collection("users")
                    .document(user.uid)
                    .update(userExpChange)
                    .await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteTask(task: Task): Boolean {
        return try {
            val user = firebaseAuth.currentUser!!
            db.collection("users")
                .document(user.uid)
                .collection("lists")
                .document(task.listId!!)
                .collection("tasks")
                .document(task.id!!)
                .delete()
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getSkills(): Result<QuerySnapshot> {
        return try {
            val user = firebaseAuth.currentUser!!
            val result = db.collection("users")
                .document(user.uid)
                .collection("skills")
                .orderBy("id")
                .get()
                .await()
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun getSkill(skillId: String): Result<DocumentSnapshot> {
        return try {
            val user = firebaseAuth.currentUser!!
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

    suspend fun createSkill(skillName: String): Result<DocumentReference> {
        return try {
            val user = firebaseAuth.currentUser!!
            val result = db.collection("users")
                .document(user.uid)
                .collection("skills")
                .add(Skill(
                    incId = ++skillLastId,
                    name = skillName
                ))
                .await()
            Result.Success(result)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    suspend fun editSkill(skill: Skill): Boolean {
        return try {
            val user = firebaseAuth.currentUser!!
            db.collection("users")
                .document(user.uid)
                .collection("skills")
                .document(skill.id!!)
                .set(skill, SetOptions.merge())
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteSkill(skill: Skill): Boolean {
        return try {
            val user = firebaseAuth.currentUser!!
            db.collection("users")
                .document(user.uid)
                .collection("skills")
                .document(skill.id!!)
                .delete()
                .await()
            true
        } catch (e: Exception) {
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
            return INSTANCE ?:
            throw IllegalStateException("DatabaseRepository must be initialized")
        }
    }
}