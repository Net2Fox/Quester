package ru.net2fox.quester.data.auth

import android.content.Context
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import kotlinx.coroutines.tasks.await
import ru.net2fox.quester.data.Result
import ru.net2fox.quester.data.database.DatabaseRepository

/**
 * Класс, который запрашивает аутентификацию и информацию о пользователе из удаленного источника данных
 * и поддерживает в памяти кэш статуса входа и информации об учетных данных пользователя.
 */
class AuthRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val databaseRepository = DatabaseRepository.get()

    // Реализация функции для регистрации нового пользователя по электронной почте и паролю
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
            Result.Error(e)
        }
    }

    // Реализация функции для входа существующего пользователя по электронной почте и паролю
    suspend fun signIn(email: String, password: String): Result<AuthResult> {
        return try {
            val authResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Result.Success(authResult)
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    fun signOut() {
        firebaseAuth.signOut()
    }

    // Реализация функции для получения текущего пользователя
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