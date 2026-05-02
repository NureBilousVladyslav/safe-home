package com.example.safehome.presentation.launch.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safehome.data.model.ErrorType
import com.example.safehome.data.model.Result
import com.example.safehome.domain.AuthUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LaunchViewModel @Inject constructor(
    private val authUseCase: AuthUseCase
) : ViewModel() {

    private val _tokenState = MutableStateFlow<Result<Boolean>>(Result.Loading)
    val tokenState: StateFlow<Result<Boolean>> get() = _tokenState

    init {
        checkToken()
    }

    private fun checkToken() {
        _tokenState.value = Result.Loading
        viewModelScope.launch {
            val result = authUseCase.isTokenExpired()
            _tokenState.value = when (result) {
                is Result.Success -> result
                is Result.Error -> {
                    when (val error = result.errorType) {
                        is ErrorType.ServerError -> {
                            if (error.code == 401) "Your session has expired. Please log in"
                            else error.message
                        }
                        is ErrorType.NetworkError -> error.message
                        is ErrorType.InternalError -> error.message
                    }
                    result
                }
                is Result.Loading -> result
            }
        }
    }
}