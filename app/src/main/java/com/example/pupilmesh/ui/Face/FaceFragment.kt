package com.example.pupilmesh.ui.Face

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.AspectRatio
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.pupilmesh.databinding.FragmentFaceBinding
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FaceFragment : Fragment(), FaceDetectorHelper.DetectorListener {

    private var _binding: FragmentFaceBinding? = null
    private val binding get() = _binding!!

    private lateinit var faceDetectorHelper: FaceDetectorHelper
    private lateinit var cameraExecutor: ExecutorService
    private var camera: Camera? = null
    private var imageAnalyzer: ImageAnalysis? = null
    
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startCamera()
            } else {
                Toast.makeText(context, "Camera permission is required for this feature", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFaceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        cameraExecutor = Executors.newSingleThreadExecutor()
        
        // Initialize the face detector
        faceDetectorHelper = FaceDetectorHelper(
            context = requireContext(),
            faceDetectorListener = this
        )
        
        // Request camera permission and start camera if granted
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            // Set up the preview use case
            val preview = Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
            
            // Set up the image analyzer
            imageAnalyzer = ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { imageProxy ->
                        analyzeImage(imageProxy)
                    }
                }
            
            // Select front camera as a default
            val cameraSelector = CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build()
            
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                
                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
                
            } catch (e: Exception) {
                Log.e(TAG, "Use case binding failed", e)
            }
            
        }, ContextCompat.getMainExecutor(requireContext()))
    }
    
    private fun analyzeImage(imageProxy: ImageProxy) {
        lifecycleScope.launch(Dispatchers.Default) {
            val frameTime = SystemClock.uptimeMillis()
            
            val bitmap = imageProxy.toBitmap()
            
            // Process the image
            faceDetectorHelper.detectAsync(bitmap, frameTime)
            
            // Close the image to make it available for the next frame
            imageProxy.close()
        }
    }
    
    override fun onDetectionResult(result: FaceDetectorResult) {
        activity?.runOnUiThread {
            // Get the camera's orientation
            val rotation = binding.viewFinder.display.rotation
            
            // Update the UI with the results
            binding.overlayView.setResults(
                result,
                binding.viewFinder.width,
                binding.viewFinder.height,
                binding.overlayView.width,
                binding.overlayView.height
            )
            
            // Display the number of faces detected
            val numFaces = result.detections().size
            binding.textFace.text = "Faces detected: $numFaces"
        }
    }
    
    override fun onError(error: String) {
        activity?.runOnUiThread {
            binding.textFace.text = "Error: $error"
            Log.e(TAG, "Face detection error: $error")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up
        cameraExecutor.shutdown()
        faceDetectorHelper.clearDetector()
        _binding = null
    }
    
    companion object {
        private const val TAG = "FaceFragment"
    }
}

// Extension function to convert ImageProxy to Bitmap
private fun ImageProxy.toBitmap(): Bitmap {
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer
    
    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()
    
    val nv21 = ByteArray(ySize + uSize + vSize)
    
    yBuffer.get(nv21, 0, ySize)
    vBuffer.get(nv21, ySize, vSize)
    uBuffer.get(nv21, ySize + vSize, uSize)
    
    val yuvImage = android.graphics.YuvImage(nv21, android.graphics.ImageFormat.NV21, width, height, null)
    val out = java.io.ByteArrayOutputStream()
    yuvImage.compressToJpeg(android.graphics.Rect(0, 0, width, height), 100, out)
    val imageBytes = out.toByteArray()
    return android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}