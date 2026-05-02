package com.example.safehome.presentation.auth.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.safehome.data.model.ErrorType
import com.example.safehome.data.model.Result
import com.example.safehome.databinding.FragmentResetPasswordBinding
import com.example.safehome.presentation.auth.viewModel.ResetPasswordViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import kotlin.getValue

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

@AndroidEntryPoint
class ResetPasswordFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private lateinit var binding: FragmentResetPasswordBinding
    private val resetViewModel: ResetPasswordViewModel by viewModels()

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
    ): View? {
        binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                resetViewModel.resetState.collect { result ->
                    when (result) {
                        is Result.Loading -> Timber.tag("ResetPassword").d("Loading...")
                        is Result.Success -> {
                            Timber.tag("ResetPassword").d("Reset password successfully, mail send")
                            Toast.makeText(requireContext(), "A confirmation letter has been sent to the mail", Toast.LENGTH_LONG).show()
                            findNavController().popBackStack()
                        }
                        is Result.Error -> {
                            val message = when (val error = result.errorType) {
                                is ErrorType.ServerError -> {
                                    if (error.code == 422) "User with this email not found"
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

        binding.apply {
            resetPswdButton.setOnClickListener {
                val email = emailEditText.text.toString().trim()

                resetViewModel.resetPassword(email)
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ResetPasswordFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ResetPasswordFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}