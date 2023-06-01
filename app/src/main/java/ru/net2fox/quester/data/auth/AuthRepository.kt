package ru.net2fox.quester.data.auth

import android.util.Log
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import ru.net2fox.quester.data.Result
import ru.net2fox.quester.data.database.DatabaseRepository
import ru.net2fox.quester.data.model.FirebaseBlockedAccountException
import ru.net2fox.quester.data.model.FirebaseDeletedAccountException

/**
 * Класс, который запрашивает аутентификацию и информацию о пользователе из удаленного источника данных
 * и поддерживает в памяти кэш статуса входа и информации об учетных данных пользователя.
 */
class AuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseRepository = DatabaseRepository.get()
    private val db = Firebase.firestore

    suspend fun signUp(username: String, email: String, password: String): Result<AuthResult> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val profileUpdates = userProfileChangeRequest {
                displayName = username
            }
            authResult.user!!.updateProfile(profileUpdates).await()
            databaseRepository.initializeNewUser()
            Result.Success(authResult)
        } catch (e: Exception) {
            Log.d("FirebaseAuth", e.message.toString())
            Result.Error(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<AuthResult> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            if (db.collection("users").document(authResult.user!!.uid).get().await().getBoolean("isDeleted") == true) {
                firebaseAuth.signOut()
                throw FirebaseDeletedAccountException("Your account has been deleted")

            }
            if (isBlocked()) {
                firebaseAuth.signOut()
                throw FirebaseBlockedAccountException("Your account has been blocked")
            }
            databaseRepository.initializeUser()
            Result.Success(authResult)
        } catch (e: Exception) {
            Log.d("FirebaseAuth", e.message.toString())
            Result.Error(e)
        }
    }

    suspend fun isModerator(): Boolean {
        val result = db.collection("users").document(firebaseAuth.currentUser!!.uid).get().await()
        return result.getBoolean("isModerator")!!
    }

    suspend fun isBlocked(): Boolean {
        val result = db.collection("users").document(firebaseAuth.currentUser!!.uid).get().await()
        return result.getBoolean("isBlocked")!!
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    companion object {
        private var INSTANCE: AuthRepository? = null

        fun initialize() {
            if (INSTANCE == null) {
                INSTANCE = AuthRepository()
            }
        }

        fun get(): AuthRepository {
            return INSTANCE ?:
            throw IllegalStateException("AuthRepository must be initialized")
        }
    }
}