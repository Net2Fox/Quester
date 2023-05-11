package ru.net2fox.quester.data.auth

import android.content.Context
import android.util.Log
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.userProfileChangeRequest
import kotlinx.coroutines.tasks.await
import ru.net2fox.quester.data.Result
import ru.net2fox.quester.data.database.DatabaseDataSource

/**
 * Класс, который запрашивает аутентификацию и информацию о пользователе из удаленного источника данных
 * и поддерживает в памяти кэш статуса входа и информации об учетных данных пользователя.
 */
class AuthRepository private constructor(context: Context)  {

    private val authDataSource = AuthDataSource.get()
    private val databaseDataSource = DatabaseDataSource.get()

    suspend fun signIn(username: String, password: String): Result<AuthResult> {
        return authDataSource.signIn(username, password)
    }

    suspend fun signUp(username: String, email: String, password: String): Result<AuthResult> {
        val result = authDataSource.signUp(email, password)
        if (result is Result.Success) {
            val profileUpdates = userProfileChangeRequest {
                displayName = username
            }
            result.data.user!!.updateProfile(profileUpdates).await()
            databaseDataSource.initializeNewUser()
        }
        return result
    }

    fun signOut() {
        authDataSource.signOut()
    }

    // Функция для получения текущего пользователя
    fun getCurrentUser(): FirebaseUser? {
        return authDataSource.getCurrentUser()
    }

    companion object {
        private var INSTANCE: AuthRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = AuthRepository(context)
            }
        }

        fun get(): AuthRepository {
            return INSTANCE ?:
            throw IllegalStateException("AuthRepository must be initialized")
        }
    }
}