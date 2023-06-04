package ru.net2fox.quester.ui.achievement

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.tasks.await
import ru.net2fox.quester.R
import ru.net2fox.quester.data.Result
import ru.net2fox.quester.data.database.DatabaseRepository
import ru.net2fox.quester.data.model.Achievement
import ru.net2fox.quester.data.model.Skill
import ru.net2fox.quester.data.model.User
import ru.net2fox.quester.data.model.UserSkill
import ru.net2fox.quester.ui.userprofile.UserProfileResult

class AchievementsViewModel : ViewModel() {

    private val databaseRepository = DatabaseRepository.get()

    private val _achievementsResult = MutableLiveData<AchievementsResult>()
    val achievementsResult: LiveData<AchievementsResult> = _achievementsResult

    suspend fun getAchievements() {
        val result = databaseRepository.getAchievementsList()

        if (result is Result.Success) {
            val query: QuerySnapshot = result.data;
            val achievements: MutableList<Achievement> = mutableListOf()
            for (achiev in query) {
                val skill = achiev.getDocumentReference("skillRef")!!.get().await()
                achievements.add(Achievement(
                    nameRU = achiev.getString("nameRU")!!,
                    nameEN = achiev.getString("nameEN")!!,
                    skillLevel = achiev.getLong("skillLevel")!!,
                    skill = Skill(
                        skill.id,
                        skill.getString("nameRU")!!,
                        skill.getString("nameEN")!!
                    )
                ))
            }
            _achievementsResult.postValue(AchievementsResult(success = achievements))
        } else {
            _achievementsResult.postValue(AchievementsResult(error = R.string.get_data_error))
        }
    }
}