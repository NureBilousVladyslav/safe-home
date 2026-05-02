package com.example.safehome.presentation.auth.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safehome.data.model.ErrorType
import com.example.safehome.data.model.Result
import com.example.safehome.domain.ResetPasswordUseCase
import com.example.safehome.presentation.auth.utils.ValidatorUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val resetPasswordUseCase: ResetPasswordUseCase
) : ViewModel() {
    private val _resetState = MutableSharedFlow<Result<Boolean>>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val resetState: SharedFlow<Result<Boolean>> get() = _resetState.asSharedFlow()

    fun resetPassword(email: String) {
        when {
            !ValidatorUtils.isNotBlank(email) -> {
                _resetState.tryEmit(Result.Error(ErrorType.InternalError("Email is empty")))
            }
            !ValidatorUtils.isValidEmail(email) -> {
                _resetState.tryEmit(Result.Error(ErrorType.InternalError("Email is incorrect")))
            }
            else -> {
                _resetState.tryEmit(Result.Loading)
                viewModelScope.launch {
                    val result = resetPasswordUseCase.sendResetPassword(email)
                    _resetState.emit(result)
                }
            }
        }
    }
}