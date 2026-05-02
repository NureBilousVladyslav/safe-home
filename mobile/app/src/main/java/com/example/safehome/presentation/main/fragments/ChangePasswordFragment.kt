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
            binding.backButton.setOnClickListener {
                findNavController().popBackStack()
            }

            binding.eyeCurrentButton.setOnClickListener {
                isCurrentPasswordVisible = !isCurrentPasswordVisible
                PasswordVisibilityUtils.togglePasswordVisibility(
                    binding.pswdCurrentEditText,
                    binding.eyeCurrentButton,
                    isCurrentPasswordVisible
                )
            }

            binding.eyeNewButton.setOnClickListener {
                isNewPasswordVisible = !isNewPasswordVisible
                PasswordVisibilityUtils.togglePasswordVisibility(
                    binding.pswdNewEditText,
                    binding.eyeNewButton,
                    isNewPasswordVisible
                )
            }

            binding.eyeConfirmButton.setOnClickListener {
                isConfirmPasswordVisible = !isConfirmPasswordVisible
                PasswordVisibilityUtils.togglePasswordVisibility(
                    binding.pswdConfirmEditText,
                    binding.eyeConfirmButton,
                    isConfirmPasswordVisible
                )
            }

            changePswdButton.setOnClickListener {
                viewLifecycleOwner.lifecycleScope.launch {
                    val currentPswd = pswdCurrentEditText.text.toString().trim()
                    val newPswd = pswdNewEditText.text.toString().trim()
                    val confirmPswd = pswdConfirmEditText.text.toString().trim()

                    val message: String = when {
                        !ValidatorUtils.isNotBlank(currentPswd, newPswd, confirmPswd) -> {
                            "Field is empty"
                        }
                        !ValidatorUtils.isValidPassword(currentPswd) -> {
                            "Current password must be 8 characters or more"
                        }
                        !ValidatorUtils.isValidPassword(newPswd) -> {
                            "New password must be 8 characters or more"
                        }
                        !ValidatorUtils.isValidPassword(confirmPswd) -> {
                            "Confirm password must be 8 characters or more"
                        }
                        !ValidatorUtils.isPasswordConfirmed(newPswd, confirmPswd) -> {
                            "Passwords do not match"
                        }
                        else -> {
                            val messageResponse = changePasswordViewModel.updatePassword(currentPswd, newPswd)
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