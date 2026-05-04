package com.example.safehome.presentation.biometric.fragments

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
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
import com.example.safehome.databinding.FragmentFaceRecognitionBinding
import com.example.safehome.presentation.biometric.utils.FaceEmbeddingUtils
import com.example.safehome.presentation.biometric.utils.ImageProxyUtils
import com.example.safehome.presentation.biometric.viewModel.BiometricState
import com.example.safehome.presentation.biometric.viewModel.BiometricViewModel
import com.google.mlkit.vision.face.FaceDetector
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.util.concurrent.ExecutorService
import javax.inject.Inject

@AndroidEntryPoint
class FaceRecognitionFragment : Fragment() {

    private var _binding: FragmentFaceRecognitionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BiometricViewModel by viewModels()

    @Inject
    lateinit var faceDetector: FaceDetector

    @Inject
    lateinit var cameraExecutor: ExecutorService

    @Inject
    lateinit var faceEmbeddingUtils: FaceEmbeddingUtils

    private var isProcessingFrame = false
    private var isRecognitionCompleted = false

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startCamera()
            } else {
                showMessage(getString(R.string.biometric_camera_access_required))
                requireActivity().finish()
            }
        }

    companion object {
        fun newInstance(): FaceRecognitionFragment {
            return FaceRecognitionFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFaceRecognitionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.statusTextView.text = getString(R.string.biometric_look_at_camera)

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
                    binding.statusTextView.text = getString(R.string.biometric_searching_face)
                }

                BiometricState.Processing -> {
                    binding.statusTextView.text = getString(R.string.biometric_verifying_face)
                }

                BiometricState.VerificationSuccess -> {
                    showMessage(getString(R.string.biometric_face_recognized))
                    requireActivity().setResult(Activity.RESULT_OK)
                    requireActivity().finish()
                }

                BiometricState.EnrollmentSuccess -> Unit
                BiometricState.BiometricDisabled -> Unit

                is BiometricState.Error -> {
                    binding.statusTextView.text = state.message
                    showMessage(state.message)
                    isProcessingFrame = false
                    isRecognitionCompleted = false
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
        if (isProcessingFrame || isRecognitionCompleted) {
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
                        binding.statusTextView.text = getString(R.string.biometric_face_not_found)
                        isProcessingFrame = false
                    }

                    faces.size > 1 -> {
                        binding.statusTextView.text = getString(R.string.biometric_face_multiple_faces)
                        isProcessingFrame = false
                    }

                    else -> {
                        binding.statusTextView.text = getString(R.string.biometric_face_found)

                        val sourceBitmap = ImageProxyUtils.imageProxyToBitmap(imageProxy)
                        if (sourceBitmap == null) {
                            binding.statusTextView.text = getString(R.string.biometric_face_frame_processing_error)
                            isProcessingFrame = false
                            return@addOnSuccessListener
                        }

                        val faceBitmap = ImageProxyUtils.cropFaceFromBitmap(
                            sourceBitmap = sourceBitmap,
                            faceBoundingBox = faces.first().boundingBox
                        )

                        if (faceBitmap == null) {
                            binding.statusTextView.text = getString(R.string.biometric_face_crop_error)
                            isProcessingFrame = false
                            return@addOnSuccessListener
                        }

                        isRecognitionCompleted = true

                        try {
                            val embedding = faceEmbeddingUtils.getFaceEmbedding(faceBitmap)
                            viewModel.verifyFace(embedding)
                        } catch (exception: Exception) {
                            Timber.e(exception, "Biometric template verification error")

                            val errorMessage = exception.localizedMessage
                                ?: getString(R.string.biometric_face_template_verification_error)

                            showMessage(errorMessage)
                            binding.statusTextView.text = errorMessage

                            isProcessingFrame = false
                            isRecognitionCompleted = false
                        }
                    }
                }
            }
            .addOnFailureListener {
                binding.statusTextView.text = getString(R.string.biometric_face_frame_analysis_error)
                isProcessingFrame = false
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
