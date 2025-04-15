package com.example.pupilmesh.ui.Face

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facedetector.FaceDetector
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult
import com.google.mediapipe.framework.image.BitmapImageBuilder
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService

class FaceDetectorHelper(
    private val context: Context,
    private val faceDetectorListener: DetectorListener
) {
    private var faceDetector: FaceDetector? = null
    private var lastProcessingTimeMs: Long = 0
    private var lastFrameTime: Long = 0
    private val executorService: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()
    private var isSetup = false

    interface DetectorListener {
        fun onDetectionResult(result: FaceDetectorResult)
        fun onError(error: String)
    }

    private fun setupFaceDetector() {
        if (isSetup) return
        
        try {
            val modelName = "face_detection_short_range.tflite"

            val baseOptionsBuilder = BaseOptions.builder()
                .setModelAssetPath(modelName)

            val optionsBuilder = FaceDetector.FaceDetectorOptions.builder()
                .setBaseOptions(baseOptionsBuilder.build())
                .setMinDetectionConfidence(0.5f)
                .setMinSuppressionThreshold(0.3f)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener { faceDetectorResult, _ ->
                    val finishTimeMs = SystemClock.uptimeMillis()
                    lastProcessingTimeMs = finishTimeMs - lastFrameTime
                    faceDetectorListener.onDetectionResult(faceDetectorResult)
                }
                .setErrorListener { error ->
                    faceDetectorListener.onError(error.message ?: "Unknown error")
                }

            faceDetector = FaceDetector.createFromOptions(context, optionsBuilder.build())
            isSetup = true
        } catch (e: Exception) {
            faceDetectorListener.onError(e.message ?: "Unknown error")
            Log.e(TAG, "Setup failed: ${e.message}")
        }
    }

    fun detectAsync(bitmap: Bitmap, frameTime: Long) {
        if (faceDetector == null) {
            setupFaceDetector()
        }
        
        if (faceDetector == null) {
            Log.e(TAG, "Face detector not initialized")
            return
        }
        
        this.lastFrameTime = frameTime
        
        executorService.execute {
            try {
                val mpImage = BitmapImageBuilder(bitmap).build()
                faceDetector?.detectAsync(mpImage, frameTime)
            } catch (e: Exception) {
                Log.e(TAG, "Detection failed: ${e.message}")
                faceDetectorListener.onError("Detection failed: ${e.message}")
            }
        }
    }

    fun clearDetector() {
        try {
            faceDetector?.close()
            faceDetector = null
            isSetup = false
            executorService.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Error clearing detector: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "FaceDetectorHelper"
    }
} 