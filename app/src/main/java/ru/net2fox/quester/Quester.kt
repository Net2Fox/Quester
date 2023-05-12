package ru.net2fox.quester

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import com.google.android.material.color.DynamicColors
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.net2fox.quester.data.auth.AuthRepository
import ru.net2fox.quester.data.database.DatabaseRepository

class Quester : Application() {

    override fun onCreate() {
        super.onCreate()
        setContext(this)
        DynamicColors.applyToActivitiesIfAvailable(this)
        FirebaseApp.initializeApp(this)
        FirebaseAuth.getInstance().useAppLanguage()
        DatabaseRepository.initialize()
        AuthRepository.initialize()

        kotlinx.coroutines.MainScope().launch(Dispatchers.IO) {
            DatabaseRepository.get().getLastId()
        }
    }

    companion object {

        @SuppressLint("StaticFieldLeak")
        private lateinit var _context: Context

        fun setContext(context: Context) {
            _context = context
        }

        fun getContext(): Context {
            return _context
        }
    }
}