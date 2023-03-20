package com.josejordan.mygame

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.abs

class Enemy(
    var x: Float,
    var y: Float,
    val width: Float,
    val height: Float,
    var xVelocity: Float,
    var yVelocity: Float
) {
    private val paint = Paint()

    init {
        paint.color = Color.RED
    }


    fun draw(canvas: Canvas) {
        canvas.drawRect(x, y, x + width, y + height, paint)
    }

    fun update() {
        x += xVelocity
        y += yVelocity
    }

    fun collidesWith(ballX: Float, ballY: Float, ballRadius: Float): Boolean {
        val distanceX = abs(ballX - x - width / 2)
        val distanceY = abs(ballY - y - height / 2)

        if (distanceX > (width / 2 + ballRadius)) {
            return false
        }
        if (distanceY > (height / 2 + ballRadius)) {
            return false
        }
        if (distanceX <= (width / 2)) {
            return true
        }
        if (distanceY <= (height / 2)) {
            return true
        }

        val cornerDistanceSq = (distanceX - width / 2) * (distanceX - width / 2) + (distanceY - height / 2) * (distanceY - height / 2)

        return cornerDistanceSq <= (ballRadius * ballRadius)
    }
}
