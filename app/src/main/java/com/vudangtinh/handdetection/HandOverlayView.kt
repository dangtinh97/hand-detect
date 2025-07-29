package com.vudangtinh.handdetection

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark

class HandOverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var landmarksList: List<List<NormalizedLandmark>> = emptyList()

    private val pointPaint = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.FILL
        strokeWidth = 8f
    }

    private val linePaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }

    fun updateLandmarks(newLandmarks: List<List<NormalizedLandmark>>) {
        landmarksList = newLandmarks
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        for (hand in landmarksList) {
            // Vẽ điểm
            for (point in hand) {
                val x = point.x() * width
                val y = point.y() * height
                canvas.drawCircle(x, y, 8f, pointPaint)
            }

            // Vẽ các đường nối xương tay
            val connections = listOf(
                Pair(0, 1), Pair(1, 2), Pair(2, 3), Pair(3, 4),     // Ngón cái
                Pair(0, 5), Pair(5, 6), Pair(6, 7), Pair(7, 8),     // Ngón trỏ
                Pair(5, 9), Pair(9, 10), Pair(10, 11), Pair(11, 12),// Ngón giữa
                Pair(9, 13), Pair(13, 14), Pair(14, 15), Pair(15, 16), // Ngón áp út
                Pair(13, 17), Pair(17, 18), Pair(18, 19), Pair(19, 20), // Ngón út
                Pair(0, 17) // viền lòng bàn tay
            )

            for ((startIdx, endIdx) in connections) {
                val start = hand[startIdx]
                val end = hand[endIdx]
                canvas.drawLine(
                    start.x() * width, start.y() * height,
                    end.x() * width, end.y() * height,
                    linePaint
                )
            }
        }
    }
}