package com.example.safehome.presentation.biometric.fragments

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.SystemClock
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.resolutionselector.ResolutionSelector
import androidx.camera.core.resolutionselector.ResolutionStrategy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.safehome.R
import com.example.safehome.databinding.FragmentFaceEnrollmentBinding
import com.example.safehome.presentation.biometric.utils.FaceEmbeddingUtils
import com.example.safehome.presentation.biometric.utils.FaceUtils
import com.example.safehome.presentation.biometric.utils.ImageProxyUtils
import com.example.safehome.presentation.biometric.viewModel.BiometricState
import com.example.safehome.presentation.biometric.viewModel.BiometricViewModel
import com.google.mlkit.vision.face.Face
import com.google.mlkit.vision.face.FaceDetector
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.concurrent.ExecutorService
import javax.inject.Inject
import kotlin.math.abs

@AndroidEntryPoint
class FaceEnrollmentFragment : Fragment() {

    private var _binding: FragmentFaceEnrollmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BiometricViewModel by viewModels()

    @Inject
    lateinit var faceDetector: FaceDetector

    @Inject
    lateinit var cameraExecutor: ExecutorService

    @Inject
    lateinit var faceEmbeddingUtils: FaceEmbeddingUtils

    @Inject
    lateinit var faceUtils: FaceUtils

    private val capturedEmbeddings = mutableListOf<FloatArray>()

    private var isProcessingFrame = false
    private var isEnrollmentCompleted = false
    private var currentStepIndex = 0
    private var currentStepCapturedCount = 0
    private var lastCaptureTimeMs = 0L

    private val enrollmentSteps = listOf(
        EnrollmentStep(
            instructionResId = R.string.biometric_enrollment_look_straight,
            validator = { face ->
                abs(face.headEulerAngleX) <= FRONT_MAX_PITCH &&
                        abs(face.headEulerAngleY) <= FRONT_MAX_YAW
            }
        ),
        EnrollmentStep(
            instructionResId = R.string.biometric_enrollment_look_up,
            validator = { face ->
                face.headEulerAngleX >= PITCH_MIN_ANGLE &&
                        abs(face.headEulerAngleY) <= SIDE_MAX_YAW
            }
        ),
        EnrollmentStep(
            instructionResId = R.string.biometric_enrollment_look_down,
            validator = { face ->
                face.headEulerAngleX <= -PITCH_MIN_ANGLE &&
                        abs(face.headEulerAngleY) <= SIDE_MAX_YAW
            }
        ),
        EnrollmentStep(
            instructionResId = R.string.biometric_enrollment_turn_left,
            validator = { face ->
                face.headEulerAngleY <= -YAW_MIN_ANGLE &&
                        abs(face.headEulerAngleX) <= SIDE_MAX_PITCH
            }
        ),
        EnrollmentStep(
            instructionResId = R.string.biometric_enrollment_turn_right,
            validator = { face ->
                face.headEulerAngleY >= YAW_MIN_ANGLE &&
                        abs(face.headEulerAngleX) <= SIDE_MAX_PITCH
            }
        )
    )

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startCamera()
            } else {
                showMessage(getString(R.string.biometric_enrollment_camera_access_required))
                requireActivity().finish()
            }
        }

    companion object {
        fun newInstance(): FaceEnrollmentFragment {
            return FaceEnrollmentFragment()
        }

        private const val CAPTURES_PER_STEP = 2
        private const val CAPTURE_DELAY_MS = 450L

        private const val FRONT_MAX_PITCH = 8f
        private const val FRONT_MAX_YAW = 8f

        private const val PITCH_MIN_ANGLE = 10f
        private const val YAW_MIN_ANGLE = 12f

        private const val SIDE_MAX_PITCH = 14f
        private const val SIDE_MAX_YAW = 14f
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFaceEnrollmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        updateInstructionText()

        binding.cancelButton.setOnClickListener {
            requireActivity().finish()
        }

        observeViewModel()
        checkCameraPermission()
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                BiometricState.Idle -> Unit

                BiometricState.SearchingFace -> {
                    updateInstructionText()
                }

                BiometricState.Processing -> {
                    binding.statusTextView.text =
                        getString(R.string.biometric_enrollment_saving_template)
                }

                BiometricState.EnrollmentSuccess -> {
                    showMessage(getString(R.string.biometric_enrollment_enabled))
                    requireActivity().setResult(Activity.RESULT_OK)
                    requireActivity().finish()
                }

                BiometricState.VerificationSuccess -> Unit
                BiometricState.BiometricDisabled -> Unit

                is BiometricState.Error -> {
                    binding.statusTextView.text = state.message
                    showMessage(state.message)
                    isProcessingFrame = false
                    isEnrollmentCompleted = false
                }
            }
        }
    }

    private fun checkCameraPermission() {
        val permissionGranted = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            startCamera()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also { previewUseCase ->
                    previewUseCase.surfaceProvider = binding.previewView.surfaceProvider
                }

            val resolutionSelector = ResolutionSelector.Builder()
                .setResolutionStrategy(
                    ResolutionStrategy(
                        Size(720, 1280),
                        ResolutionStrategy.FALLBACK_RULE_CLOSEST_HIGHER_THEN_LOWER
                    )
                )
                .build()

            val imageAnalysis = ImageAnalysis.Builder()
                .setResolutionSelector(resolutionSelector)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysisUseCase ->
                    analysisUseCase.setAnalyzer(cameraExecutor) { imageProxy ->
                        analyzeImage(imageProxy)
                    }
                }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview,
                    imageAnalysis
                )
            } catch (exception: Exception) {
                Timber.e(exception, "Failed to start camera")
                showMessage(exception.localizedMessage ?: getString(R.string.biometric_failed_start_camera))
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun analyzeImage(imageProxy: ImageProxy) {
        if (isProcessingFrame || isEnrollmentCompleted) {
            imageProxy.close()
            return
        }

        val inputImage = ImageProxyUtils.toInputImage(imageProxy)
        if (inputImage == null) {
            imageProxy.close()
            return
        }

        isProcessingFrame = true

        faceDetector.process(inputImage)
            .addOnSuccessListener { faces ->
                when {
                    faces.isEmpty() -> {
                        binding.statusTextView.text =
                            getString(R.string.biometric_face_not_found)
                        isProcessingFrame = false
                    }

                    faces.size > 1 -> {
                        binding.statusTextView.text =
                            getString(R.string.biometric_face_multiple_faces)
                        isProcessingFrame = false
                    }

                    else -> {
                        processDetectedFace(
                            imageProxy = imageProxy,
                            face = faces.first()
                        )
                    }
                }
            }
            .addOnFailureListener {
                binding.statusTextView.text =
                    getString(R.string.biometric_face_frame_analysis_error)
                isProcessingFrame = false
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun processDetectedFace(
        imageProxy: ImageProxy,
        face: Face
    ) {
        val currentStep = enrollmentSteps[currentStepIndex]

        if (!currentStep.validator(face)) {
            updateInstructionText()
            isProcessingFrame = false
            return
        }

        val now = SystemClock.elapsedRealtime()
        if (now - lastCaptureTimeMs < CAPTURE_DELAY_MS) {
            isProcessingFrame = false
            return
        }

        val sourceBitmap = ImageProxyUtils.imageProxyToBitmap(imageProxy)
        if (sourceBitmap == null) {
            binding.statusTextView.text =
                getString(R.string.biometric_face_frame_processing_error)
            isProcessingFrame = false
            return
        }

        val faceBitmap = ImageProxyUtils.cropFaceFromBitmap(
            sourceBitmap = sourceBitmap,
            faceBoundingBox = face.boundingBox
        )

        if (faceBitmap == null) {
            binding.statusTextView.text = getString(R.string.biometric_face_crop_error)
            isProcessingFrame = false
            return
        }

        try {
            val embedding = faceEmbeddingUtils.getFaceEmbedding(faceBitmap)
            capturedEmbeddings.add(embedding)
            currentStepCapturedCount++
            lastCaptureTimeMs = now

            if (currentStepCapturedCount >= CAPTURES_PER_STEP) {
                moveToNextStepOrFinish()
            } else {
                updateInstructionText()
            }
        } catch (exception: Exception) {
            Timber.e(exception, "Biometric template creation error")

            val errorMessage = exception.localizedMessage
                ?: getString(R.string.biometric_face_template_creation_error)

            showMessage(errorMessage)
            binding.statusTextView.text = errorMessage
        } finally {
            isProcessingFrame = false
        }
    }

    private fun moveToNextStepOrFinish() {
        currentStepCapturedCount = 0
        currentStepIndex++

        if (currentStepIndex >= enrollmentSteps.size) {
            isEnrollmentCompleted = true
            val averageEmbedding = faceUtils.averageEmbeddings(capturedEmbeddings)
            viewModel.enrollFace(averageEmbedding)
        } else {
            updateInstructionText()
        }
    }

    private fun updateInstructionText() {
        if (currentStepIndex >= enrollmentSteps.size) return

        val totalCaptures = enrollmentSteps.size * CAPTURES_PER_STEP
        val capturedCount = capturedEmbeddings.size
        val currentStep = enrollmentSteps[currentStepIndex]

        binding.statusTextView.text = getString(
            R.string.biometric_enrollment_progress,
            getString(currentStep.instructionResId),
            capturedCount,
            totalCaptures
        )
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private data class EnrollmentStep(
        val instructionResId: Int,
        val validator: (Face) -> Boolean
    )
}
