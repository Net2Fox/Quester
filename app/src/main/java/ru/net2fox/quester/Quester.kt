package ru.net2fox.quester

import android.app.Application
import android.content.Context
import androidx.lifecycle.lifecycleScope
import com.google.android.material.color.DynamicColors
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import ru.net2fox.quester.data.auth.AuthDataSource
import ru.net2fox.quester.data.auth.AuthRepository
import ru.net2fox.quester.data.database.DatabaseDataSource
import ru.net2fox.quester.data.database.DatabaseRepository

class Quester : Application() {

    override fun onCreate() {
        super.onCreate()
        setContext(this)
        DynamicColors.applyToActivitiesIfAvailable(this)
        FirebaseApp.initializeApp(this)
        FirebaseAuth.getInstance().useAppLanguage()
        DatabaseDataSource.initialize(this)
        DatabaseRepository.initialize(this)
        AuthDataSource.initialize(this)
        AuthRepository.initialize(this)

        kotlinx.coroutines.MainScope().launch(Dispatchers.IO) {
                DatabaseDataSource.get().getLastId()
        }
    }

    companion object {

        private lateinit var _context: Context

        fun setContext(context: Context) {
            _context = context
        }

        fun getContext(): Context {
            return _context
        }
    }
}