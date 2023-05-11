package ru.net2fox.quester.ui.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.QuerySnapshot
import ru.net2fox.quester.R
import ru.net2fox.quester.data.Result
import ru.net2fox.quester.data.auth.AuthRepository
import ru.net2fox.quester.data.database.DatabaseRepository
import ru.net2fox.quester.data.model.ListOfTasks
import ru.net2fox.quester.ui.list.ListActionResult
import ru.net2fox.quester.ui.list.ListResult

class SettingsViewModel : ViewModel() {

    private val authRepository = AuthRepository.get()
    private val databaseRepository = DatabaseRepository.get()

    private val _settingsProgressResetResult = MutableLiveData<SettingsResult>()
    val settingsProgressResetResult: LiveData<SettingsResult> = _settingsProgressResetResult

    private val _settingsDeleteAccountResult = MutableLiveData<SettingsResult>()
    val settingsDeleteAccountResult: LiveData<SettingsResult> = _settingsDeleteAccountResult

    suspend fun deleteAccount() {
        val result = databaseRepository.deleteAccount()

        if (result) {
            _settingsDeleteAccountResult.postValue(SettingsResult(success = R.string.delete_account_successful))
        } else {
            _settingsDeleteAccountResult.postValue(SettingsResult(error = R.string.get_data_error))
        }
    }

    suspend fun progressReset() {
        val result = databaseRepository.progressReset()

        if (result) {
            _settingsProgressResetResult.postValue(SettingsResult(success = R.string.progress_reset_successful))
        } else {
            _settingsProgressResetResult.postValue(SettingsResult(error = R.string.get_data_error))
        }
    }

    suspend fun signOut() {
        authRepository.signOut()
    }
}