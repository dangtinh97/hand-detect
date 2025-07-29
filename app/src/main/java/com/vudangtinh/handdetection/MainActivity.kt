package com.vudangtinh.handdetection

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {
    private companion object {
        private const val TAG = "MainActivity"
    }

    private var handLandmarker: HandLandmarker? = null
    private lateinit var previewView: PreviewView
    private lateinit var resultText: TextView
    private lateinit var cameraExecutor: ExecutorService

    // Đăng ký xử lý quyền
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            initializeMediaPipe()
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        previewView = findViewById(R.id.preview_view)
        resultText = findViewById(R.id.result_text)
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (checkPermissions()) {
            initializeMediaPipe()
            startCamera()
//             detectStaticImage() // Uncomment để kiểm tra với ảnh tĩnh
        } else {
            requestPermissions()
        }
    }

    // Kiểm tra quyền CAMERA
    private fun checkPermissions(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        Log.d(TAG, "Camera permission granted: $result")
        return result
    }

    // Yêu cầu quyền CAMERA
    private fun requestPermissions() {
        permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    // Khởi tạo MediaPipe HandLandmarker
    private fun initializeMediaPipe() {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("hand_landmarker.task")
                .build()
            val options = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setNumHands(2)
                .setMinHandDetectionConfidence(0.1f)
                .setMinHandPresenceConfidence(0.1f)
                .setMinTrackingConfidence(0.1f)
                .setResultListener(this::processResult)
                .setErrorListener { error ->
                    Log.e(TAG, "HandLandmarker error: ${error.message}")
                    runOnUiThread {
                        Toast.makeText(this, "HandLandmarker error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                }
                .build()

            handLandmarker = HandLandmarker.createFromOptions(this, options)
            Log.d(TAG, "HandLandmarker initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing MediaPipe: ${e.message}")
            runOnUiThread {
                Toast.makeText(this, "Failed to initialize MediaPipe: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Xử lý kết quả HandLandmarker
    private fun processResult(result: HandLandmarkerResult, input: com.google.mediapipe.framework.image.MPImage) {
        Log.d(TAG, "Result received, landmarks size: ${result.landmarks().size}")
        val overlay = findViewById<HandOverlayView>(R.id.hand_overlay)
        overlay.updateLandmarks(result.landmarks())
        runOnUiThread {

            val resultText = buildString {
                if (result.landmarks().isEmpty()) {
                    append("No hands detected")
                } else {
                    append("Hands detected:\n")
                    result.landmarks().forEachIndexed { index, landmarks ->
                        append("Hand $index:\n")
                        landmarks.forEach { landmark ->
                            append("  Point: (${landmark.x()}, ${landmark.y()})\n")
                        }
                    }
                }
            }
            this.resultText.text = resultText
            Log.d(TAG, resultText)
        }
    }

    // Kiểm tra ảnh tĩnh
    private fun detectStaticImage() {
        try {
            val baseOptions = BaseOptions.builder()
                .setModelAssetPath("hand_landmarker.task")
                .build()
            val options = HandLandmarker.HandLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.IMAGE)
                .setNumHands(2)
                .setMinHandDetectionConfidence(0.1f)
                .setMinHandPresenceConfidence(0.1f)
                .setMinTrackingConfidence(0.1f)
                .build()

            val staticHandLandmarker = HandLandmarker.createFromOptions(this, options)
            Log.d(TAG, "Static HandLandmarker initialized")

            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.f2)
                ?: throw IllegalStateException("Failed to load bitmap")
            Log.d(TAG, "Static bitmap loaded: ${bitmap.width}x${bitmap.height}")
            val mpImage = BitmapImageBuilder(bitmap).build()

            val result = staticHandLandmarker.detect(mpImage)
            val resultText = buildString {
                if (result.landmarks().isEmpty()) {
                    append("No hands detected in static image")
                } else {
                    append("Hands detected in static image:\n")
                    result.landmarks().forEachIndexed { index, landmarks ->
                        append("Hand $index:\n")
                        landmarks.forEach { landmark ->
                            append("  Point: (${landmark.x()}, ${landmark.y()})\n")
                        }
                    }
                }
            }
            runOnUiThread {
                this.resultText.text = resultText
                Log.d(TAG, resultText)
            }

            staticHandLandmarker.close()
        } catch (e: Exception) {
            Log.e(TAG, "Error detecting hands in static image: ${e.message}")
            runOnUiThread {
                Toast.makeText(this, "Error detecting hands: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Khởi động CameraX
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // ImageAnalysis
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(android.util.Size( 640,860))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { analysis ->
                    analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
                        Log.d(TAG, "Processing frame with rotation: $rotationDegrees, size: ${imageProxy.width}x${imageProxy.height}")
                        try {
                            val bitmap = imageProxy.toBitmap()
                            Log.d(TAG, "Bitmap created: ${bitmap.width}x${bitmap.height}")
                            val rotatedBitmap = bitmap.rotate(rotationDegrees.toFloat())
                            Log.d(TAG, "Rotated bitmap: ${rotatedBitmap.width}x${rotatedBitmap.height}")
                            val mpImage = BitmapImageBuilder(rotatedBitmap).build()
                            Log.d(TAG, "MPImage created, timestamp: ${System.currentTimeMillis()}")
                            handLandmarker?.detectAsync(mpImage, System.currentTimeMillis())
                                ?: Log.e(TAG, "HandLandmarker is null")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing frame: ${e.message}")
                        } finally {
                            imageProxy.close()
                        }
                    }
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
                Log.d(TAG, "Camera bound successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Camera binding failed: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this, "Failed to start camera: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }, ContextCompat.getMainExecutor(this))
    }

    // Giải phóng tài nguyên
    override fun onDestroy() {
        super.onDestroy()
        handLandmarker?.close()
        cameraExecutor.shutdown()
    }

    // Extension để chuyển ImageProxy thành Bitmap
    fun androidx.camera.core.ImageProxy.toBitmap(): Bitmap {
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
        yuvImage.compressToJpeg(android.graphics.Rect(0, 0, width, height), 95, out)
        val imageBytes = out.toByteArray()
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        if (bitmap == null) {
            throw IllegalStateException("Failed to decode bitmap")
        }
        return bitmap
    }

    // Extension để xoay Bitmap
    private fun Bitmap.rotate(degrees: Float): Bitmap {
        if (degrees == 0f) return this
        val matrix = Matrix().apply { postRotate(degrees) }
        val rotatedBitmap = Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
        Log.d(TAG, "Rotated bitmap created: ${rotatedBitmap.width}x${rotatedBitmap.height}")
        return rotatedBitmap
    }
}