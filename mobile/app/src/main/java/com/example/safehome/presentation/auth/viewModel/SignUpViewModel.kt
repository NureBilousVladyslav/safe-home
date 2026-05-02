package com.example.safehome.presentation.auth.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safehome.domain.SignUpUseCase
import com.example.safehome.data.model.DateModel
import com.example.safehome.data.model.ErrorType
import com.example.safehome.data.model.Result
import com.example.safehome.presentation.auth.utils.ValidatorUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {
    private val _signUpState = MutableSharedFlow<Result<Boolean>>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val signUpState: SharedFlow<Result<Boolean>> get() = _signUpState.asSharedFlow()

    fun registerUser(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
        dateModel: DateModel?
    ) {
        when {
            !ValidatorUtils.isNotBlank(email, password) -> {
                _signUpState.tryEmit(
                    Result.Error(ErrorType.InternalError("Email or password is empty"))
                )
            }

            !ValidatorUtils.isNotBlank(name) -> {
                _signUpState.tryEmit(
                    Result.Error(ErrorType.InternalError("Name is empty"))
                )
            }

            !ValidatorUtils.isDateSelected(dateModel) -> {
                _signUpState.tryEmit(
                    Result.Error(ErrorType.InternalError("Date not selected"))
                )
            }

            !ValidatorUtils.isValidEmail(email) -> {
                _signUpState.tryEmit(
                    Result.Error(ErrorType.InternalError("Email is incorrect"))
                )
            }

            !ValidatorUtils.isValidPassword(password) -> {
                _signUpState.tryEmit(
                    Result.Error(ErrorType.InternalError("Password must be 8 characters or more"))
                )
            }

            !ValidatorUtils.isPasswordConfirmed(password, confirmPassword) -> {
                _signUpState.tryEmit(
                    Result.Error(ErrorType.InternalError("Passwords do not match"))
                )
            }

            !ValidatorUtils.isUserAtLeast18(dateModel!!) -> {
                _signUpState.tryEmit(
                    Result.Error(ErrorType.InternalError("You must be at least 18 years old"))
                )
            }

            else -> {
                _signUpState.tryEmit(Result.Loading)
                viewModelScope.launch {
                    val result = signUpUseCase.isRegister(name, email, password)
                    _signUpState.emit(result)
                }
            }
        }
    }
}