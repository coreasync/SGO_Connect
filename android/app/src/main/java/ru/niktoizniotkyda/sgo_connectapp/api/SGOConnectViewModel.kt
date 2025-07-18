package ru.niktoizniotkyda.sgo_connectapp.api

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.launch
import ru.niktoizniotkyda.sgo_connectapp.data.TokenData
import javax.inject.Inject

class SGOConnectViewModel
    @Inject
    constructor(
        private var repository: SGOConnectRepositoryImpl
) : ViewModel() {
    private val _isLoading = MutableLiveData<Boolean>()
    private val _errorMessage = MutableLiveData<String?>()
    private val _tokenId = MutableLiveData<TokenID?>()

    val isLoading: LiveData<Boolean> = _isLoading
    val errorMessage: LiveData<String?> = _errorMessage
    val tokenId: LiveData<TokenID?> = _tokenId

    fun createToken(tokenInput: TokenData) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.createToken(tokenInput)
                .onSuccess { tokenId ->
                    _tokenId.value = tokenId
                    _isLoading.value = false
                }
                .onFailure { exception ->
                    _errorMessage.value = exception.message
                    _isLoading.value = false
                }
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}