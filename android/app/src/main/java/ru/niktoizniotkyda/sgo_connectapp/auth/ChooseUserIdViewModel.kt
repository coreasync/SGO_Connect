package ru.niktoizniotkyda.sgo_connectapp.auth

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ru.niktoizniotkyda.sgo_connectapp.MainActivity
import ru.niktoizniotkyda.sgo_connectapp.data.AppDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChooseUserIdViewModel @Inject constructor(
    private val dataStore: AppDataStore,
    private val utilsRepository: UtilsRepository
) : ViewModel() {

    private val _usersId = MutableLiveData<List<UserUIEntity>>()
    val usersId: LiveData<List<UserUIEntity>> = _usersId

    init {
        viewModelScope.launch {
            _usersId.value = utilsRepository.getUsers().map {
                UserUIEntity(
                    it.id,
                    null,
                    null,
                    it.nickName,
                    it.loginName
                )
            }
        }
    }


    fun login(context: Context, userUIEntity: UserUIEntity) {
        try {
            viewModelScope.launch {
                TODO()
                //settingsDataStore.setValue(SettingsDataStore.CURRENT_USER_ID, userUIEntity.userId!!)
            }
            val intent = Intent(context, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            ContextCompat.startActivity(context, intent, null)
        } catch (e: Exception) {
            Toast.makeText(context, e.localizedMessage, Toast.LENGTH_LONG).show()
        }
    }

}
