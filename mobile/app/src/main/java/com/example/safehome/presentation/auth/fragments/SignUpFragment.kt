package com.example.safehome.presentation.auth.fragments

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.safehome.R
import com.example.safehome.data.model.DateModel
import com.example.safehome.data.model.ErrorType
import com.example.safehome.data.model.Result
import com.example.safehome.databinding.FragmentSignUpBinding
import com.example.safehome.presentation.auth.utils.PasswordVisibilityUtils
import com.example.safehome.presentation.auth.viewModel.SignUpViewModel
import com.example.safehome.presentation.common.viewModel.DatePickerViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SignUpFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class SignUpFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private var _isPasswordVisible = false
    private var _isConfirmPasswordVisible = false

    private val datePickerViewModel: DatePickerViewModel by viewModels()
    private val signUpViewModel: SignUpViewModel by viewModels()

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
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                signUpViewModel.signUpState.collect { result ->
                    when (result) {
                        is Result.Loading -> Timber.tag("SignUp").d("Loading...")
                        is Result.Success -> {
                            Timber.tag("SignUp").d("Register successfully, mail send")
                            Toast.makeText(requireContext(), "A confirmation letter has been sent to the mail", Toast.LENGTH_LONG).show()
                            findNavController().popBackStack()
                        }
                        is Result.Error -> {
                            val message = when (val error = result.errorType) {
                                is ErrorType.ServerError -> {
                                    if (error.code == 400) "An account with this email already exists"
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

        binding.registerButton.setOnClickListener {
            binding.apply {
                val name = giveTextFromEditText(nameEditText)
                val email = giveTextFromEditText(emailEditText)
                val password = giveTextFromEditText(pswdEditText)
                val confirmPassword = giveTextFromEditText(pswdConfirmEditText)
                val date = datePickerViewModel.selectedDate.value

                signUpViewModel.registerUser(
                    name,
                    email,
                    password,
                    confirmPassword,
                    date, )
            }
        }

        binding.dateEditText.setOnClickListener {
            showDatePickerDialog(binding.dateEditText)
        }

        binding.eyeButton.setOnClickListener {
            _isPasswordVisible = !_isPasswordVisible
            PasswordVisibilityUtils.togglePasswordVisibility(binding.pswdEditText, binding.eyeButton, _isPasswordVisible)
        }

        binding.eyeConfirmButton.setOnClickListener {
            _isConfirmPasswordVisible = !_isConfirmPasswordVisible
            PasswordVisibilityUtils.togglePasswordVisibility(binding.pswdConfirmEditText, binding.eyeConfirmButton, _isConfirmPasswordVisible)
        }
    }

    private fun giveTextFromEditText(editText: EditText) : String {
        return editText.text.toString().trim()
    }

    private fun showDatePickerDialog(editText: EditText) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_date_picker, null)
        val monthPicker = dialogView.findViewById<NumberPicker>(R.id.monthPicker)
        val dayPicker = dialogView.findViewById<NumberPicker>(R.id.dayPicker)
        val yearPicker = dialogView.findViewById<NumberPicker>(R.id.yearPicker)
        val cancelButton = dialogView.findViewById<TextView>(R.id.cancelButton)
        val saveButton = dialogView.findViewById<TextView>(R.id.saveButton)

        monthPicker.minValue = 0
        monthPicker.maxValue = DateModel.months.size - 1
        monthPicker.displayedValues = DateModel.months

        yearPicker.minValue = 1900
        yearPicker.maxValue = 2025

        fun isLeapYear(year: Int): Boolean = (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)

        fun updateDaysInMonth(year: Int, month: Int) {
            val daysInMonth = when (month) {
                1 -> if (isLeapYear(year)) 29 else 28
                3, 5, 8, 10 -> 30
                else -> 31
            }
            dayPicker.minValue = 1
            dayPicker.maxValue = daysInMonth
            if (dayPicker.value > daysInMonth) dayPicker.value = daysInMonth
        }

        val currentDate = datePickerViewModel.selectedDate.value ?: DateModel.DEFAULT

        updateDaysInMonth(currentDate.year, currentDate.month)
        monthPicker.value = currentDate.month
        dayPicker.value = currentDate.day
        yearPicker.value = currentDate.year

        monthPicker.setOnValueChangedListener { _, _, newMonth ->
            updateDaysInMonth(yearPicker.value, newMonth)
        }

        yearPicker.setOnValueChangedListener { _, _, newYear ->
            updateDaysInMonth(newYear, monthPicker.value)
        }

        MaterialAlertDialogBuilder(requireContext(), R.style.CustomDialogStyle)
            .setView(dialogView)
            .create()
            .apply {
                show()
                getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(
                    ContextCompat.getColor(context, android.R.color.holo_blue_dark)
                )
                getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(
                    ContextCompat.getColor(context, android.R.color.holo_red_light)
                )
                cancelButton.setOnClickListener {
                    dismiss()
                }
                saveButton.setOnClickListener {
                    datePickerViewModel.setDate(monthPicker.value, dayPicker.value, yearPicker.value)
                    editText.setText(datePickerViewModel.selectedDate.value?.toFormattedString() ?: DateModel.DEFAULT.toFormattedString())
                    dismiss()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SignUpFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SignUpFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}