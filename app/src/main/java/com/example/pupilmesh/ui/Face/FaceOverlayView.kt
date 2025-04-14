package com.example.pupilmesh.ui.Face

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.google.mediapipe.tasks.vision.facedetector.FaceDetectorResult
import kotlin.math.min

class FaceOverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var results: FaceDetectorResult? = null
    private var scaleFactor: Float = 1f
    private var bounds = Rect()
    private var inputWidth: Int = 0
    private var inputHeight: Int = 0
    
    // Reference rectangle - will be positioned in the center of the view
    private var referenceRect = Rect()
    private var isFaceInReferenceRect = false

    // Paint for the reference rectangle
    private val referencePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    // Paint for face bounding box
    private val facePaint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    
    // Paint for text
    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 40f
        textAlign = Paint.Align.CENTER
    }

    fun setResults(
        detectionResults: FaceDetectorResult,
        imageWidth: Int,
        imageHeight: Int,
        viewWidth: Int,
        viewHeight: Int
    ) {
        results = detectionResults
        inputWidth = imageWidth
        inputHeight = imageHeight

        // Calculate scale factor between the actual image and the view
        val scaleX = viewWidth.toFloat() / imageWidth
        val scaleY = viewHeight.toFloat() / imageHeight
        scaleFactor = min(scaleX, scaleY)

        // Calculate the offset to center the image if needed
        val offsetX = (viewWidth - imageWidth * scaleFactor) / 2f
        val offsetY = (viewHeight - imageHeight * scaleFactor) / 2f

        bounds = Rect(
            offsetX.toInt(),
            offsetY.toInt(),
            (offsetX + imageWidth * scaleFactor).toInt(),
            (offsetY + imageHeight * scaleFactor).toInt()
        )
        
        // Set up the reference rectangle (centered, about 40% of view size)
        val refWidth = viewWidth * 0.4f
        val refHeight = viewHeight * 0.3f
        val refLeft = (viewWidth - refWidth) / 2
        val refTop = (viewHeight - refHeight) / 2
        
        referenceRect.set(
            refLeft.toInt(),
            refTop.toInt(),
            (refLeft + refWidth).toInt(),
            (refTop + refHeight).toInt()
        )

        // Log scaling information to help debug
        Log.d("FaceOverlayView", "Image: ${imageWidth}x${imageHeight}, View: ${viewWidth}x${viewHeight}")
        Log.d("FaceOverlayView", "Scale: $scaleFactor, Offset: $offsetX,$offsetY")

        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Draw the "Reference Rectangle" text above the rectangle
        canvas.drawText(
            "Reference Rectangle", 
            width / 2f, 
            referenceRect.top.toFloat() - 20f, 
            textPaint
        )
        
        // Draw a line from the text to the rectangle
        canvas.drawLine(
            width / 2f,
            referenceRect.top.toFloat() - 10f,
            width / 2f,
            referenceRect.top.toFloat(),
            Paint().apply { color = Color.BLACK; strokeWidth = 2f }
        )
        
        // Always draw the reference rectangle
        updateReferenceRectColor()
        canvas.drawRect(referenceRect, referencePaint)

        results?.let { result ->
            val detections = result.detections()
            
            // Reset face detection status
            isFaceInReferenceRect = false
            
            for (i in 0 until detections.size) {
                val detection = detections[i]
                val boundingBox = detection.boundingBox()
                
                // Convert normalized coordinates to actual pixel coordinates
                val left = bounds.left + (boundingBox.left * inputWidth.toFloat()) * scaleFactor
                val top = bounds.top + (boundingBox.top * inputHeight.toFloat()) * scaleFactor
                val right = bounds.left + (boundingBox.right * inputWidth.toFloat()) * scaleFactor
                val bottom = bounds.top + (boundingBox.bottom * inputHeight.toFloat()) * scaleFactor
                
                // Create a rect for the face bounding box
                val faceRect = Rect(
                    left.toInt(),
                    top.toInt(),
                    right.toInt(),
                    bottom.toInt()
                )
                
                // Draw the face bounding box
                canvas.drawRect(
                    left,
                    top,
                    right,
                    bottom,
                    facePaint
                )
                
                // Check if face is inside reference rectangle
                if (referenceRect.contains(faceRect)) {
                    isFaceInReferenceRect = true
                    Log.d("FaceOverlayView", "Face is inside reference rectangle")
                } else {
                    Log.d("FaceOverlayView", "Face is outside reference rectangle")
                }
                
                // Update the reference rectangle color
                updateReferenceRectColor()
                
                // Redraw the reference rectangle with updated color
                canvas.drawRect(referenceRect, referencePaint)
            }
        }
    }
    
    // Helper method to check if one rect is fully inside another
    private fun Rect.contains(other: Rect): Boolean {
        return contains(other.left, other.top) && 
               contains(other.right, other.bottom)
    }
    
    // Update the reference rectangle color based on face position
    private fun updateReferenceRectColor() {
        referencePaint.color = if (isFaceInReferenceRect) {
            Color.GREEN
        } else {
            Color.RED
        }
    }

    fun clear() {
        results = null
        invalidate()
    }
} 