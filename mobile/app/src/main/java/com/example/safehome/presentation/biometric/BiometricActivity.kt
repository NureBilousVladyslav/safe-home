package com.example.safehome.presentation.biometric

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.safehome.R
import com.example.safehome.presentation.biometric.fragments.FaceRecognitionFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BiometricActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_MODE = "extra_mode"
        const val MODE_RECOGNITION = "recognition"
        const val MODE_ENROLLMENT = "enrollment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biometric)

        if (savedInstanceState == null) {
            openStartFragment()
        }
    }

    private fun openStartFragment() {
        val mode = intent.getStringExtra(EXTRA_MODE) ?: MODE_RECOGNITION

        val fragment = when (mode) {
            MODE_RECOGNITION -> FaceRecognitionFragment.newInstance()
            MODE_ENROLLMENT -> FaceRecognitionFragment.newInstance()
            else -> FaceRecognitionFragment.newInstance()
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.biometricFragmentContainer, fragment)
            .commit()
    }
}
