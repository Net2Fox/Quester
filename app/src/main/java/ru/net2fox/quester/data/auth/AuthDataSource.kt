package ru.net2fox.quester.data.auth

import android.app.Application
import android.content.Context
import com.google.firebase.auth.*
import kotlinx.coroutines.tasks.await
import ru.net2fox.quester.data.Result

/**
 * Класс, обрабатывающий аутентификацию с использованием учетных данных для
 * входа в систему и получающий информацию о пользователе.
 */
class AuthDataSource private constructor(context: Context) {

    private val firebaseAuth = FirebaseAuth.getInstance()

    // Реализация функции для регистрации нового пользователя по электронной почте и паролю
    suspend fun signUp(email: String, password: String): Result<AuthResult> {
        return try {
            val authResult = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
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
        private var INSTANCE: AuthDataSource? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = AuthDataSource(context)
            }
        }

        fun get(): AuthDataSource {
            return INSTANCE ?:
            throw IllegalStateException("AuthDataSource must be initialized")
        }
    }
}