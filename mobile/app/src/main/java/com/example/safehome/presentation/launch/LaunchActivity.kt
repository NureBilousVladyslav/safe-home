package com.example.safehome.presentation.launch

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.repeatOnLifecycle
import com.example.safehome.data.model.ErrorType
import com.example.safehome.presentation.auth.AuthActivity
import com.example.safehome.presentation.launch.viewModel.LaunchViewModel
import com.example.safehome.presentation.main.MainActivity
import kotlinx.coroutines.launch
import com.example.safehome.data.model.Result
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class LaunchActivity : ComponentActivity() {
    private val launchViewModel: LaunchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            launchViewModel.tokenState.value is Result.Loading
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launchViewModel.tokenState.collect { result ->
                    when (result) {
                        is Result.Loading -> { }
                        is Result.Success -> {
                            if (result.data) {
                                startActivity(MainActivity::class.java)
                            } else {
                                startActivity(AuthActivity::class.java)
                            }
                            finish()
                        }
                        is Result.Error -> {
                            when (val error = result.errorType) {
                                is ErrorType.ServerError -> {
                                    if (error.code != 401)
                                    Toast.makeText(this@LaunchActivity, error.message, Toast.LENGTH_LONG).show()
                                }
                                is ErrorType.NetworkError -> {
                                    Toast.makeText(this@LaunchActivity, error.message, Toast.LENGTH_LONG).show()
                                }
                                is ErrorType.InternalError -> error.message
                            }

                            startActivity(AuthActivity::class.java)
                            finish()
                        }
                    }
                }
            }
        }
    }

    private fun startActivity(activityClass: Class<out ComponentActivity>) {
        val intent = Intent(this, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
    }
}