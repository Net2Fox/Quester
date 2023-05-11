package ru.net2fox.quester.ui.list

import android.util.Patterns
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.QuerySnapshot
import ru.net2fox.quester.R
import ru.net2fox.quester.data.Result
import ru.net2fox.quester.data.database.DatabaseRepository
import ru.net2fox.quester.data.model.ListOfTasks
import ru.net2fox.quester.ui.auth.signup.SignUpFormState
import ru.net2fox.quester.ui.auth.signup.SignUpResult

class ListViewModel : ViewModel() {

    private val databaseRepository = DatabaseRepository.get()

    private val _listResult = MutableLiveData<ListResult>()
    val listResult: LiveData<ListResult> = _listResult

    private val _listActionResult = MutableLiveData<ListActionResult>()
    val listActionResult: LiveData<ListActionResult> = _listActionResult

    private val _taskActionResult = MutableLiveData<ListActionResult>()
    val taskActionResult: LiveData<ListActionResult> = _taskActionResult

    val listSize: Int? get() = _listResult.value?.success?.size

    suspend fun getListsOfTasks() {
        val result = databaseRepository.getListsOfTasks()

        if (result is Result.Success) {
            val query: QuerySnapshot = result.data;
            val mutableList: MutableList<ListOfTasks> = mutableListOf()
            for (postDocument in query) {
                mutableList.add(ListOfTasks(
                    postDocument.id,
                    postDocument.get("id", Long::class.java)!!,
                    postDocument.getString("name")!!
                ))
            }
            _listResult.postValue(ListResult(success = mutableList))
        } else {
            _listResult.postValue(ListResult(error = R.string.get_data_error))
        }
    }

    suspend fun createListOfTasks(listName: String) {
        val result = databaseRepository.createListOfTasks(listName)

        if (result is Result.Success) {
            _listActionResult.postValue(ListActionResult(success = true))
        } else {
            _listActionResult.postValue(ListActionResult(error = R.string.get_data_error))
        }
    }

    suspend fun createTask(listId: String, taskName: String) {
        val result = databaseRepository.createTask(listId, taskName)

        if (result is Result.Success) {
            _taskActionResult.postValue(ListActionResult(success = true))
        } else {
            _taskActionResult.postValue(ListActionResult(error = R.string.get_data_error))
        }
    }

    suspend fun editListOfTasks(listId: String, listName: String) {
        val result = databaseRepository.editListOfTasksName(listId, listName)

        if (result) {
            _listActionResult.postValue(ListActionResult(success = true))
        } else {
            _listActionResult.postValue(ListActionResult(error = R.string.get_data_error))
        }
    }

    suspend fun deleteListOfTasks(listId: String) {
        val result = databaseRepository.deleteListOfTasks(listId)

        if (result) {
            _listActionResult.postValue(ListActionResult(success = true))
        } else {
            _listActionResult.postValue(ListActionResult(error = R.string.get_data_error))
        }
    }

    fun getListById(id: Int): ListOfTasks? = _listResult.value?.success?.get(id)
    //fun getListByListId(listId: Int): ListOfTasks? = _listResult.value?.success?.find { it -> it.name == listId }
    fun getListById(listId: String): ListOfTasks? = _listResult.value?.success?.find { it -> it.id == listId }
    fun getListId(position: Int): String? = _listResult.value?.success?.get(position)?.id
    fun contains(itemId: Int): Boolean = _listResult.value?.success?.contains(getListById(itemId)) ?: false
}