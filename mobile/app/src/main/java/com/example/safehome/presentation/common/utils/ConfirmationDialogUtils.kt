package com.example.safehome.presentation.common.utils

import android.view.LayoutInflater
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.example.safehome.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

fun Fragment.showConfirmationDialog(
    @StringRes titleResId: Int,
    @StringRes messageResId: Int,
    @StringRes cancelTextResId: Int = R.string.cancel,
    @StringRes confirmTextResId: Int = R.string.ok,
    onCancel: () -> Unit = {},
    onConfirm: () -> Unit
) {
    val dialogView = LayoutInflater.from(requireContext())
        .inflate(R.layout.dialog_confirmation, null)

    val titleTextView = dialogView.findViewById<TextView>(R.id.titleTextView)
    val messageTextView = dialogView.findViewById<TextView>(R.id.messageTextView)
    val cancelButton = dialogView.findViewById<TextView>(R.id.cancelButton)
    val confirmButton = dialogView.findViewById<TextView>(R.id.confirmButton)

    titleTextView.setText(titleResId)
    messageTextView.setText(messageResId)
    cancelButton.setText(cancelTextResId)
    confirmButton.setText(confirmTextResId)

    MaterialAlertDialogBuilder(requireContext(), R.style.CustomDialogStyle)
        .setView(dialogView)
        .create()
        .apply {
            show()

            cancelButton.setOnClickListener {
                dismiss()
                onCancel()
            }

            confirmButton.setOnClickListener {
                dismiss()
                onConfirm()
            }
        }
}
