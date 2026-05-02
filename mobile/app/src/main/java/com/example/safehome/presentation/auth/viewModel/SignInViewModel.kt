package com.example.safehome.presentation.auth.viewModel

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.safehome.R
import com.example.safehome.data.model.ErrorType
import com.example.safehome.data.model.Result
import com.example.safehome.domain.AuthUseCase
import com.example.safehome.presentation.auth.utils.ValidatorUtils
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val authUseCase: AuthUseCase,
    private val firebaseAuth: FirebaseAuth,
) : ViewModel() {
    private val _authState = MutableSharedFlow<Result<Boolean>>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val authState: SharedFlow<Result<Boolean>> get() = _authState.asSharedFlow()

    fun checkUserAuthorization(email: String, password: String) {
        when {
            !ValidatorUtils.isNotBlank(email, password) -> {
                _authState.tryEmit(
                    Result.Error(ErrorType.InternalError("Email or password is empty"))
                )
            }
            !ValidatorUtils.isValidEmail(email) -> {
                _authState.tryEmit(
                    Result.Error(ErrorType.InternalError("Email is incorrect"))
                )
            }
            !ValidatorUtils.isValidPassword(password) -> {
                _authState.tryEmit(
                    Result.Error(ErrorType.InternalError("Password must be 8 characters or more"))
                )
            }
            else -> {
                _authState.tryEmit(Result.Loading)
                viewModelScope.launch {
                    val result = authUseCase.isUserAuthorized(email, password)
                    _authState.emit(result)
                }
            }
        }
    }

    fun getGoogleSignInIntent(context: Context): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        val client = GoogleSignIn.getClient(context, gso)
        return client.signInIntent
    }

    fun handleGoogleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _authState.emit(Result.Loading)
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                _authState.emit(Result.Error(ErrorType.InternalError("Google Sign-In failed: ${e.message}")))
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = firebaseAuth.currentUser
                    user?.getIdToken(false)?.addOnSuccessListener { result ->
                        val firebaseIdToken = result.token
                        if (firebaseIdToken != null) {
                            sendTokenToServer(firebaseIdToken)
                        } else {
                            _authState.tryEmit(Result.Error(ErrorType.InternalError("Failed to get Firebase ID token")))
                        }
                    }?.addOnFailureListener { e ->
                        _authState.tryEmit(Result.Error(ErrorType.InternalError("Failed to get Firebase ID token: ${e.message}")))
                    }
                } else {
                    _authState.tryEmit(Result.Error(ErrorType.InternalError("Firebase authentication failed: ${task.exception?.message}")))
                }
            }
    }

    private fun sendTokenToServer(firebaseIdToken: String) {
        viewModelScope.launch {
            _authState.tryEmit(Result.Loading)
            viewModelScope.launch {
                val result = authUseCase.firebaseGoogleAuth(firebaseIdToken)
                _authState.emit(result)
            }
        }
    }
}