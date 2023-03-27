package com.josejordan.mygame

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class Obstacle(
    var x: Float,
    var y: Float,
    val radius: Float,
    var xVelocity: Float,
    var yVelocity: Float,
    val paint: Paint,
    var scoreByLevel: Int = 0

) {
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
        private val random = Random(System.currentTimeMillis())
        private val colors = listOf(
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.YELLOW,
            Color.CYAN,
            Color.MAGENTA,
            Color.DKGRAY,
            Color.LTGRAY,
            Color.GRAY
        )

        fun createRandomObstacles(
            count: Int,
            minRadius: Float,
            maxRadius: Float,
            screenWidth: Int,
            screenHeight: Int,
            ballSpeed: Float,
            obstacleSpeed: Float
        ): List<Obstacle> {
            val obstacles = mutableListOf<Obstacle>()
            val quadrantWidth = screenWidth / 2
            val quadrantHeight = screenHeight / 2
            val centerX = screenWidth / 2
            val centerY = screenHeight / 2

            repeat(count) {
                // Generate random x and y within a quadrant
                val quadrant = random.nextInt(4)
                val x = random.nextInt(quadrantWidth) + when (quadrant) {
                    0, 3 -> centerX - quadrantWidth
                    else -> centerX
                }
                val y = random.nextInt(quadrantHeight) + when (quadrant) {
                    0, 1 -> centerY - quadrantHeight
                    else -> centerY
                }

                // Generate random radius within minRadius and maxRadius
                val radius = random.nextFloat() * (maxRadius - minRadius) + minRadius

                // Generate random velocity for the obstacle
                val speed =
                    obstacleSpeed * (1 + ballSpeed / 20) // increase obstacle speed as ball speed increases
                val angle = random.nextDouble() * 2 * Math.PI
                val xVelocity = (speed * cos(angle)).toFloat()
                val yVelocity = (speed * sin(angle)).toFloat()

                // Choose a random color for the obstacle
                val color = colors[random.nextInt(colors.size)]
                val paint = Paint().apply {
                    this.color = color
                    style = Paint.Style.FILL
                }
                obstacles.add(
                    Obstacle(
                        x.toFloat(),
                        y.toFloat(),
                        radius,
                        xVelocity,
                        yVelocity,
                        paint
                    )
                )
            }

            return obstacles
        }
    }



}


