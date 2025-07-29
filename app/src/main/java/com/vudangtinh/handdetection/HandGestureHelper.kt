package com.vudangtinh.handdetection

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.os.SystemClock
import android.util.Log
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import java.io.ByteArrayOutputStream
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker

class HandGestureHelper(
    context: Context,
    private val onMoveDetected: (Move?) -> Unit
) : ImageAnalysis.Analyzer {

    private val handLandmarker: HandLandmarker

    init {
        val options = HandLandmarker.HandLandmarkerOptions.builder()
            .setBaseOptions(BaseOptions.builder().setModelAssetPath("hand_landmarker.task").build())
            .setRunningMode(RunningMode.LIVE_STREAM)
            .setResultListener { result, _ ->
                val landmarks = result.landmarks().firstOrNull()
                val move = landmarks?.let { detectGesture(it) }
                onMoveDetected(move)
            }
            .setErrorListener { e ->
                Log.e("HandLandmarker", "Lỗi khi detect tay: ${e.message}")
            }
            .build()

        handLandmarker = HandLandmarker.createFromOptions(context, options)
    }

    override fun analyze(imageProxy: ImageProxy) {
        val mpImage = imageProxy.toMediaPipeInputImage() ?: run {
            imageProxy.close()
            return
        }

        val rotation = ImageProcessingUtil.getRotationDegrees(imageProxy)
        val timestamp = SystemClock.uptimeMillis()

        handLandmarker.detectAsync(mpImage, timestamp, rotation)

        imageProxy.close()
    }

    object ImageProcessingUtil {
        fun getRotationDegrees(imageProxy: ImageProxy): Int {
            return when (imageProxy.imageInfo.rotationDegrees) {
                0 -> 0
                90 -> 90
                180 -> 180
                270 -> 270
                else -> 0
            }
        }
    }


    private fun detectGesture(landmarks: MutableList<NormalizedLandmark>): Move? {
        val fingersOpen = (1..4).map { isFingerOpen(landmarks, it) }
        val isThumbOpen = isThumbOpen(landmarks)

        return when {
            fingersOpen.all { !it } && !isThumbOpen -> Move.ROCK
            fingersOpen[0] && fingersOpen[1] && !fingersOpen[2] && !fingersOpen[3] -> Move.SCISSORS
            fingersOpen.all { it } && isThumbOpen -> Move.PAPER
            else -> null
        }
    }

    private fun isFingerOpen(landmarks: MutableList<NormalizedLandmark>, fingerIndex: Int): Boolean {
        val tip = landmarks[4 + fingerIndex * 4]
        val pip = landmarks[2 + fingerIndex * 4]
        return tip.y() < pip.y()
    }

    private fun isThumbOpen(landmarks: MutableList<NormalizedLandmark>): Boolean {
        val tip = landmarks[4]
        val ip = landmarks[3]
        return tip.x() > ip.x()
    }

    private fun ImageProxy.toMediaPipeInputImage(): MPImage? {
        val bitmap = toBitmap() ?: return null
        return BitmapImageBuilder(bitmap).build()
    }

    fun ImageProxy.toBitmap(): Bitmap? {
        val yBuffer = planes[0].buffer
        val vuBuffer = planes[2].buffer
        val ySize = yBuffer.remaining()
        val vuSize = vuBuffer.remaining()
        val nv21 = ByteArray(ySize + vuSize)

        yBuffer.get(nv21, 0, ySize)
        vuBuffer.get(nv21, ySize, vuSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
        val yuv = out.toByteArray()
        return BitmapFactory.decodeByteArray(yuv, 0, yuv.size)
    }
}

private fun HandLandmarker.detectAsync(mpImage: MPImage, timestamp: Long, rotation: Int) {

}
