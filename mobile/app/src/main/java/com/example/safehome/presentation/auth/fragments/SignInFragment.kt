package com.example.safehome.presentation.auth.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.safehome.R
import com.example.safehome.data.model.ErrorType
import com.example.safehome.databinding.FragmentSignInBinding
import com.example.safehome.presentation.auth.utils.PasswordVisibilityUtils
import com.example.safehome.data.model.Result
import com.example.safehome.presentation.auth.viewModel.SignInViewModel
import com.example.safehome.presentation.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@AndroidEntryPoint
class SignInFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentSignInBinding
    private lateinit var navController: NavController
    private val authViewModel: SignInViewModel by viewModels()

    private var _isPasswordVisible = false

    private val googleSignInLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        authViewModel.handleGoogleSignInResult(result.data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = findNavController()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.authState.collect { result ->
                    when (result) {
                        is Result.Loading -> Timber.tag("Auth").d("Loading...")
                        is Result.Success -> {
                            Timber.tag("Auth").d("User is authorized")
                            startActivity(MainActivity::class.java)
                        }
                        is Result.Error -> {
                            val message = when (val error = result.errorType) {
                                is ErrorType.ServerError -> {
                                    if (error.code == 422) "Wrong login or password"
                                    else if (error.code == 403) "Server is closed"
                                    else error.message
                                }
                                is ErrorType.NetworkError -> error.message
                                is ErrorType.InternalError -> error.message
                            }

                            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }

        binding.signInButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.pswdEditText.text.toString().trim()
            authViewModel.checkUserAuthorization(email, password)
        }

        binding.googleButton.setOnClickListener {
            val signInIntent = authViewModel.getGoogleSignInIntent(requireContext())
            googleSignInLauncher.launch(signInIntent)
        }

        binding.resetPswdButton.setOnClickListener {
            navController.navigate(R.id.action_signInFragment_to_resetPasswordFragment)
        }

        binding.signUpButton.setOnClickListener {
            navController.navigate(R.id.action_signInFragment_to_signUpFragment)
        }

        binding.eyeButton.setOnClickListener {
            _isPasswordVisible = !_isPasswordVisible
            PasswordVisibilityUtils.togglePasswordVisibility(binding.pswdEditText, binding.eyeButton, _isPasswordVisible)
        }
    }

    private fun startActivity(activityClass: Class<out ComponentActivity>) {
        val intent = Intent(context, activityClass)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignInFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}