package com.example.safehome.presentation.main.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.safehome.databinding.FragmentChangePasswordBinding
import com.example.safehome.presentation.auth.utils.PasswordVisibilityUtils
import com.example.safehome.presentation.auth.utils.ValidatorUtils
import com.example.safehome.presentation.main.viewModel.ChangePasswordViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.getValue

@AndroidEntryPoint
class ChangePasswordFragment : Fragment() {
    private val changePasswordViewModel: ChangePasswordViewModel by viewModels()
    private lateinit var binding: FragmentChangePasswordBinding
    private var isCurrentPasswordVisible = false
    private var isNewPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUI()
    }

    private fun initUI() {
        with(binding){
            backButton.setOnClickListener {
                findNavController().popBackStack()
            }

            eyeCurrentButton.setOnClickListener {
                isCurrentPasswordVisible = !isCurrentPasswordVisible
                PasswordVisibilityUtils.togglePasswordVisibility(
                    passwordCurrentEditText,
                    eyeCurrentButton,
                    isCurrentPasswordVisible
                )
            }

            eyeNewButton.setOnClickListener {
                isNewPasswordVisible = !isNewPasswordVisible
                PasswordVisibilityUtils.togglePasswordVisibility(
                    passwordNewEditText,
                    eyeNewButton,
                    isNewPasswordVisible
                )
            }

            eyeConfirmButton.setOnClickListener {
                isConfirmPasswordVisible = !isConfirmPasswordVisible
                PasswordVisibilityUtils.togglePasswordVisibility(
                    passwordConfirmEditText,
                    eyeConfirmButton,
                    isConfirmPasswordVisible
                )
            }

            changePasswordButton.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    val currentPassword = passwordCurrentEditText.text.toString().trim()
                    val newPassword = passwordNewEditText.text.toString().trim()
                    val confirmPassword = passwordConfirmEditText.text.toString().trim()

                    val message: String = when {
                        !ValidatorUtils.isNotBlank(currentPassword, newPassword, confirmPassword) -> {
                            "Field is empty"
                        }
                        !ValidatorUtils.isValidPassword(currentPassword) -> {
                            "Current password must be 8 characters or more"
                        }
                        !ValidatorUtils.isValidPassword(newPassword) -> {
                            "New password must be 8 characters or more"
                        }
                        !ValidatorUtils.isValidPassword(confirmPassword) -> {
                            "Confirm password must be 8 characters or more"
                        }
                        !ValidatorUtils.isPasswordConfirmed(newPassword, confirmPassword) -> {
                            "Passwords do not match"
                        }
                        else -> {
                            val messageResponse = changePasswordViewModel.updatePassword(currentPassword, newPassword)
                            if (messageResponse != null) {
                                Toast.makeText(context, messageResponse.message, Toast.LENGTH_LONG).show()
                                if (messageResponse.message.contains("successfully")) {
                                    findNavController().popBackStack()
                                }
                            } else {
                                Toast.makeText(context, "Unexpected error occurred", Toast.LENGTH_LONG).show()
                            }
                            return@launch
                        }
                    }

                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}