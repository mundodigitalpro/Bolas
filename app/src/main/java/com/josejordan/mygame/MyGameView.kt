package com.josejordan.mygame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.sqrt

class MyGameView(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    private var thread: GameThread? = null
    private val paint = Paint()
    private val scorePaint = Paint().apply {
        color = Color.WHITE
        textSize = 60f
    }
    private lateinit var ball: Ball
    private var obstacles: MutableList<Obstacle> = mutableListOf()
    private val obstaclesLock = Any()
    private var score = 0 // variable de puntuación

    init {
        holder.addCallback(this)
        paint.color = Color.WHITE
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun moveTo(x: Float, y: Float) {
        val dx = x - ball.x
        val dy = y - ball.y
        val distance = sqrt(dx * dx + dy * dy)
        val speed = distance / 10 // ajustar este valor para cambiar la velocidad de la bola
        ball.xVelocity = dx / distance * speed
        ball.yVelocity = dy / distance * speed
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (width > 0 && height > 0) {
            ball = Ball(width.toFloat() / 2, height.toFloat() / 2, 50f, 10f, 10f)
            obstacles = Obstacle.createRandomObstacles(10, 20f, 50f, width, height) as MutableList<Obstacle>
            thread = GameThread(holder)
            thread?.start()
        }
    }


    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var retry = true
        while (retry) {
            try {
                thread?.join()
                retry = false
            } catch (_: InterruptedException) {
            }
        }
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        canvas?.drawColor(Color.BLACK)
        canvas?.drawCircle(ball.x, ball.y, ball.radius, paint)
        obstacles.forEach {
            it.draw(canvas!!)
        }
        canvas?.drawText("Score: $score", 50f, 100f, scorePaint) // muestra la puntuación en la pantalla
    }

    private fun update() {
        ball.update()

        // Cambiar dirección si la bola llega a los bordes del lienzo
        if (ball.x - ball.radius < 0 || ball.x + ball.radius > width) {
            ball.xVelocity = -ball.xVelocity
        }
        if (ball.y - ball.radius < 0 || ball.y + ball.radius > height) {
            ball.yVelocity = -ball.yVelocity
        }

        val obstaclesToRemove = mutableListOf<Obstacle>()
        for (obstacle in obstacles) {
            if (obstacle.collidesWith(ball)) {
                ball.xVelocity = -ball.xVelocity
                ball.yVelocity = -ball.yVelocity
                synchronized(obstaclesLock) {
                    obstaclesToRemove.add(obstacle)
                }
                score += 1 // aumentar la puntuación en 1 cuando la bola colisiona con un obstáculo

            }
        }

        synchronized(obstaclesLock) {
            obstacles.removeAll(obstaclesToRemove)
        }

        postInvalidate()
    }



    inner class GameThread(private val holder: SurfaceHolder) : Thread() {
        private var running = true

        override fun run() {
            while (running) {
                var canvas: Canvas? = null
                try {
                    canvas = holder.lockCanvas()
                    synchronized(holder) {
                        update()
                        draw(canvas)
                    }
                } finally {
                    canvas?.let {
                        holder.unlockCanvasAndPost(it)
                    }
                }
            }
        }

        fun stopThread() {
            running = false
        }
    }
}
