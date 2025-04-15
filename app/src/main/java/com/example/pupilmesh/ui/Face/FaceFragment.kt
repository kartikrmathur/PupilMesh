package com.example.pupilmesh.ui.Face

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
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
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
        
        // Initialize camera executor
        cameraExecutor = Executors.newSingleThreadExecutor()
        
        // Initialize detector helper
        faceDetectorHelper = FaceDetectorHelper(
            context = requireContext(),
            faceDetectorListener = this
        )
        
        // Set initial text
        binding.textFace.text = "Initializing face detection..."
        
        // Wait until the view is properly laid out before starting camera
        view.post {
            checkCameraPermission()
        }
    }
    
    private fun checkCameraPermission() {
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
            try {
                val cameraProvider = cameraProviderFuture.get()
                
                // Set up the preview use case
                val preview = Preview.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .build()
                
                // Safely set surface provider
                _binding?.let { binding ->
                    preview.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                } ?: return@addListener
                
                // Set up the image analyzer
                imageAnalyzer = ImageAnalysis.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor) { imageProxy ->
                            if (isAdded && _binding != null) {
                                analyzeImage(imageProxy)
                            } else {
                                imageProxy.close()
                            }
                        }
                    }
                
                // Select front camera as a default
                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                    .build()
                
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                
                // Bind use cases to camera
                camera = cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
                
                // Set initial text
                _binding?.textFace?.text = "Face detection ready"
                
            } catch (e: Exception) {
                Log.e(TAG, "Use case binding failed", e)
                _binding?.textFace?.text = "Camera error: ${e.message}"
            }
            
        }, ContextCompat.getMainExecutor(requireContext()))
    }
    
    private fun analyzeImage(imageProxy: ImageProxy) {
        lifecycleScope.launch(Dispatchers.Default) {
            try {
                val frameTime = SystemClock.uptimeMillis()
                
                // Convert ImageProxy to Bitmap
                val bitmap = imageProxy.toBitmap()
                
                // Process the image
                faceDetectorHelper.detectAsync(bitmap, frameTime)
            } catch (e: Exception) {
                Log.e(TAG, "Error analyzing image: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    _binding?.textFace?.text = "Analysis error: ${e.message}"
                }
            } finally {
                // Always close the image proxy
                imageProxy.close()
            }
        }
    }
    
    // Extension function to convert ImageProxy to Bitmap
    private fun ImageProxy.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer // Y
        val uBuffer = planes[1].buffer // U
        val vBuffer = planes[2].buffer // V
        
        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()
        
        val nv21 = ByteArray(ySize + uSize + vSize)
        
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)
        
        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
        val imageBytes = out.toByteArray()
        
        // Create the bitmap
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        
        // Mirror for front camera
        val matrix = Matrix()
        matrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
        
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    
    override fun onDetectionResult(result: FaceDetectorResult) {
        // Skip updates if fragment is not attached
        if (!isAdded) return
        
        activity?.runOnUiThread {
            _binding?.let { binding ->
                try {
                    // Update the UI with the results
                    binding.overlayView.setResults(
                        result,
                        binding.viewFinder.width,
                        binding.viewFinder.height,
                        binding.overlayView.width,
                        binding.overlayView.height
                    )
                    
                    // Display the number of faces detected and if face is in reference rectangle
                    val numFaces = result.detections().size
                    val isFaceInRect = binding.overlayView.isFaceInReferenceRect()
                    binding.textFace.text = "Faces detected: $numFaces | " + 
                                           (if (isFaceInRect) "Face in position ✓" else "Please center your face ✗")
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating UI: ${e.message}")
                    binding.textFace.text = "Display error: ${e.message}"
                }
            }
        }
    }
    
    override fun onError(error: String) {
        // Skip updates if fragment is not attached
        if (!isAdded) return
        
        activity?.runOnUiThread {
            _binding?.let { binding ->
                binding.textFace.text = "Error: $error"
                Log.e(TAG, "Face detection error: $error")
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Make sure we restart the camera when returning to this fragment
        if (_binding != null && camera == null) {
            checkCameraPermission()
        }
    }

    override fun onPause() {
        super.onPause()
        // Stop camera analysis when paused
        imageAnalyzer?.clearAnalyzer()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clean up resources
        imageAnalyzer?.clearAnalyzer()
        camera = null
        
        try {
            cameraExecutor.shutdown()
            faceDetectorHelper.clearDetector()
        } catch (e: Exception) {
            Log.e(TAG, "Error during cleanup: ${e.message}")
        }
        
        _binding = null
    }
    
    companion object {
        private const val TAG = "FaceFragment"
    }
}