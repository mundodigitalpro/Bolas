package com.josejordan.mygame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.provider.SyncStateContract.Helpers.update
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.sqrt

class MyGameView(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs),
    SurfaceHolder.Callback {

    private var thread: GameThread? = null
    private val paint = Paint()
    private val scorePaint = Paint().apply {
        color = Color.WHITE
        textSize = 60f
    }
    private lateinit var ball: Ball
    private var obstacles: MutableList<Obstacle> = mutableListOf()
    private val obstaclesLock = Any()
    private val scoreLock = Any()
    private var score = 0 // variable de puntuación
    private var currentLevel = 0 // nivel actual
    private val levels = listOf(
        Level(
            10f,
            10,
            10
        ), // nivel 1 con velocidad de bola 10, 10 obstáculos y umbral de puntuación de 10
        Level(
            15f,
            15,
            20
        ), // nivel 2 con velocidad de bola 15, 15 obstáculos y umbral de puntuación de 20
        Level(
            20f,
            20,
            30
        )  // nivel 3 con velocidad de bola 20, 20 obstáculos y umbral de puntuación de 30
    )
    private var currentLevelIndex = 0 // nivel actual
    private var canAdvanceLevel = false // puede avanzar al siguiente nivel?
    private var gameOver = false

    init {
        holder.addCallback(this)
        paint.color = Color.WHITE
    }

    companion object {
        private const val OBSTACLE_LIMIT =
           5 // variable para limitar el número de obstáculos en pantalla
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

    private fun createObstaclesForLevel(level: Level): List<Obstacle> {
        val ballSpeed = level.ballSpeed
        val obstacleCount = level.obstacleCount
        val speedMultiplier = currentLevelIndex + 1
        val speed = ballSpeed * speedMultiplier
        return Obstacle.createRandomObstacles(obstacleCount, speed, 50f, width, height, ballSpeed, ballSpeed)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        if (width > 0 && height > 0) {
            ball = Ball(width.toFloat() / 2, height.toFloat() / 2, 50f, 10f, 10f)
            obstacles = createObstaclesForLevel(levels[currentLevel]) as MutableList<Obstacle>
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

        // copia de la lista de obstaculos actual
        val currentObstacles: List<Obstacle>
        synchronized(obstaclesLock) {
            currentObstacles = ArrayList(obstacles)
        }

        // dibujar cada obstaculo en la lista
        for (obstacle in currentObstacles) {
            obstacle.draw(canvas!!)
        }

        canvas?.drawText("Score: $score", 50f, 100f, scorePaint)
        canvas?.drawText("Level: ${currentLevelIndex + 1}", 50f, 200f, scorePaint)

        if (gameOver) {
            val gameOverText = "Game Over"
            val gameOverWidth = scorePaint.measureText(gameOverText)
            canvas?.drawText(
                gameOverText,
                (width - gameOverWidth) / 2,
                height / 2f,
                scorePaint
            )
        }
    }

    private fun update() {
        ball.update()

        if (ball.x - ball.radius < 0 || ball.x + ball.radius > width) {
            ball.xVelocity = -ball.xVelocity
        }
        if (ball.y - ball.radius < 0 || ball.y + ball.radius > height) {
            ball.yVelocity = -ball.yVelocity
        }

        val obstaclesToRemove = mutableListOf<Obstacle>()
        var hasCollided = false // nueva variable para controlar si la bola ha colisionado con algún obstáculo

        for (obstacle in obstacles) {
            if (obstacle.collidesWith(ball)) {
                ball.xVelocity = -ball.xVelocity
                ball.yVelocity = -ball.yVelocity
                obstaclesToRemove.add(obstacle)
                score += 1
                hasCollided = true
            }
        }

        synchronized(obstaclesLock) {
            obstacles.removeAll(obstaclesToRemove)
        }

        // Generar nuevos obstáculos si ya no hay ninguno en la pantalla
        if (obstacles.isEmpty() && !gameOver && currentLevelIndex < levels.lastIndex) {
            currentLevelIndex++
            val currentLevel = levels[currentLevelIndex]
            obstacles.addAll(createObstaclesForLevel(currentLevel))
            synchronized(scoreLock) {
                score = 0
            }
        } else if (obstacles.isEmpty() && currentLevelIndex == levels.lastIndex) {
            gameOver = true
        }

        // Mover los obstáculos
        for (obstacle in obstacles) {
            obstacle.x += obstacle.xVelocity
            obstacle.y += obstacle.yVelocity

            if (obstacle.x - obstacle.radius < 0 || obstacle.x + obstacle.radius > width) {
                obstacle.xVelocity = -obstacle.xVelocity
            }
            if (obstacle.y - obstacle.radius < 0 || obstacle.y + obstacle.radius > height) {
                obstacle.yVelocity = -obstacle.yVelocity
            }
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
