package com.example.safehome.presentation.main.components

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.appcompat.widget.SwitchCompat
import timber.log.Timber

class CustomSwitchCompat @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.switchStyle
) : SwitchCompat(context, attrs, defStyleAttr) {

    private var onSwitchEnabledListener: ((Boolean) -> Unit)? = null

    fun setOnSwitchEnabledListener(listener: (Boolean) -> Unit) {
        this.onSwitchEnabledListener = listener
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP && !isEnabled) {
            Timber.tag("CustomSwitchCompat").d("Touch detected on disabled switch")
            isEnabled = true
            isChecked = true
            onSwitchEnabledListener?.invoke(true)
            return true
        }
        return super.onTouchEvent(event)
    }
}