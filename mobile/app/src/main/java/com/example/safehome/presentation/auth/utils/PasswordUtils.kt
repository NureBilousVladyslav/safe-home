package com.example.safehome.presentation.auth.utils

import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageButton
import com.example.safehome.R

object PasswordVisibilityUtils {
    fun togglePasswordVisibility(
        editText: EditText,
        eyeButton: ImageButton,
        isVisible: Boolean
    ) {
        val cursorPosition = editText.selectionStart
        if (isVisible) {
            editText.transformationMethod = null
            eyeButton.setImageResource(R.drawable.ic_eye_open)
        } else {
            editText.transformationMethod = PasswordTransformationMethod.getInstance()
            eyeButton.setImageResource(R.drawable.ic_eye_close)
        }
        editText.setSelection(cursorPosition)
    }
}