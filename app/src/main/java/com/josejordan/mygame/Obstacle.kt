package com.josejordan.mygame

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import kotlin.math.sqrt
import kotlin.random.Random


class Obstacle(
    private var x: Float,
    private var y: Float,
    private val radius: Float,
    val paint: Paint
) {

    val rectF: RectF
        get() = RectF(x - radius, y - radius, x + radius, y + radius)

    fun draw(canvas: Canvas) {
        canvas.drawCircle(x, y, radius, paint)
    }

    fun collidesWith(ball: Ball): Boolean {
        val dx = ball.x - x
        val dy = ball.y - y
        val distance = sqrt((dx * dx + dy * dy).toDouble())
        return distance <= ball.radius + radius
    }

    companion object {
        fun createRandomObstacles(count: Int, radius: Float, width: Int, height: Int): List<Obstacle> {
            val obstacles = mutableListOf<Obstacle>()
            val paint = Paint().apply {
                color = Color.RED
                style = Paint.Style.FILL
            }
            repeat(count) {
                val x = Random.nextInt(radius.toInt(), (width - radius).toInt())
                val y = Random.nextInt(radius.toInt(), (height - radius).toInt())
                obstacles.add(Obstacle(x.toFloat(), y.toFloat(), radius, paint))


            }
            return obstacles
        }
    }
}

