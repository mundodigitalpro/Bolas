package com.josejordan.mygame

import android.content.Context
import android.graphics.*
import android.media.MediaPlayer
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.sqrt

class MyGameView(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs),
    SurfaceHolder.Callback {

    private var thread: GameThread? = null
    private val paint = Paint()
    private lateinit var ball: Ball
    private lateinit var enemy: Enemy
    private var obstacles: MutableList<Obstacle> = mutableListOf()
    private val obstaclesLock = Any()
    private val scoreLock = Any()
    private var score = 0 // variable de puntuación
    private var currentLevel = 0 // nivel actual
    private var gameOverTouched = false
    private val levels = listOf(
        Level(
            10f,
            10,
            10
        ),  // nivel 1 con velocidad de bola 10, 10 obstáculos y umbral de puntuación de 10
        Level(
            15f,
            15,
            20
        ),  // nivel 2 con velocidad de bola 15, 15 obstáculos y umbral de puntuación de 20
        Level(
            20f,
            20,
            30
        ),  // nivel 3 con velocidad de bola 20, 20 obstáculos y umbral de puntuación de 30
/*        Level(25f, 25, 40),  // nivel 4 con velocidad de bola 25, 25 obstáculos y umbral de puntuación de 40
        Level(30f, 30, 50),  // nivel 5 con velocidad de bola 30, 30 obstáculos y umbral de puntuación de 50
        Level(35f, 35, 60),  // nivel 6 con velocidad de bola 35, 35 obstáculos y umbral de puntuación de 60
        Level(40f, 40, 70),  // nivel 7 con velocidad de bola 40, 40 obstáculos y umbral de puntuación de 70
        Level(45f, 45, 80),  // nivel 8 con velocidad de bola 45, 45 obstáculos y umbral de puntuación de 80
        Level(50f, 50, 90),  // nivel 9 con velocidad de bola 50, 50 obstáculos y umbral de puntuación de 90
        Level(55f, 55, 100)  // nivel 10 con velocidad de bola 55, 55 obstáculos y umbral de puntuación de 100*/
    )
    private var currentLevelIndex = 0 // nivel actual
    private var gameOver = false
    private var gameState: GameState = GameState.Waiting

    enum class GameState {
        Waiting,
        Playing,
        GameOver
    }

    private val scorePaint = Paint().apply {
        color = Color.WHITE
        textSize = 55f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    private val playAndGameOverPaint = Paint().apply {
        color = Color.WHITE
        textSize = 150f
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }
    private var lives = 3
    var onGameOver: (() -> Unit)? = null
    var onGameRestart: (() -> Unit)? = null
    private val pop = MediaPlayer.create(context, R.raw.pop)
    private val error = MediaPlayer.create(context, R.raw.error)


    init {
        holder.addCallback(this)
        paint.color = Color.WHITE
    }

    companion object {
        private const val OBSTACLE_LIMIT =
            10 // variable para limitar el número de obstáculos en pantalla
        const val HIGH_SCORE_KEY = "HIGH_SCORE"
    }


    private fun updateHighScore() {
        val prefs = context.getSharedPreferences("MyGamePrefs", Context.MODE_PRIVATE)
        val highScore = prefs.getInt(HIGH_SCORE_KEY, 0)
        Log.d("MyGameView", "HIGH SCORE gameview: $highScore")
        Log.d("MyGameView", "SCORE gameview: $score")

        if (score > highScore) {
            with(prefs.edit()) {
                putInt(HIGH_SCORE_KEY, score)
                apply()
            }
            // Actualiza el TextView para mostrar la puntuación más alta en MainActivity
            val mainActivity = context as MainActivity
            mainActivity.highScoreTextView.text = context.getString(R.string.high_score, score)

        }
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

        // Nueva variable para mantener la puntuación de cada nivel
        val scoreByLevel = score
        return Obstacle.createRandomObstacles(
            obstacleCount,
            speed,
            50f,
            width,
            height,
            ballSpeed,
            ballSpeed
        )
            .onEach { it.scoreByLevel = scoreByLevel }
    }


    override fun surfaceCreated(holder: SurfaceHolder) {
        if (width > 0 && height > 0) {
            ball = Ball(width.toFloat() / 2, height.toFloat() / 2, 50f, 10f, 10f)
            enemy = Enemy(
                (width / 4).toFloat(),
                (height / 4).toFloat(),
                100f,
                100f,
                5f,
                5f
            )
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

        // Copia de la lista de obstáculos actual
        val currentObstacles: List<Obstacle>
        synchronized(obstaclesLock) {
            currentObstacles = ArrayList(obstacles)
        }

        // Dibujar cada obstáculo en la lista
        for (obstacle in currentObstacles) {
            obstacle.draw(canvas!!)
        }

        // Dibujar el enemigo
        enemy.draw(canvas!!)

        canvas.drawText("Score: $score", 50f, 100f, scorePaint)
        canvas.drawText("Level: ${currentLevelIndex + 1}", 50f, 200f, scorePaint)
        canvas.drawText("Lives: $lives", 50f, 300f, scorePaint) // Muestra la cantidad de vidas

        if (gameState == GameState.GameOver) {
            val gameOverText = "GAME OVER"
            val playAgainWidth = playAndGameOverPaint.measureText(gameOverText)
            canvas.drawText(
                gameOverText,
                (width - playAgainWidth) / 2,
                height / 2f,
                playAndGameOverPaint
            )
        } else if (gameState == GameState.Waiting) {
            val playText = "PLAY"
            val playWidth = playAndGameOverPaint.measureText(playText)
            canvas.drawText(
                playText,
                (width - playWidth) / 2,
                height / 2f,
                playAndGameOverPaint
            )
        }
    }


    private fun update() {
        if (gameState == GameState.Playing) {
            ball.update()

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
                    obstaclesToRemove.add(obstacle)
                    score += 1
                    pop.start()
                       }
            }

            synchronized(obstaclesLock) {
                obstacles.removeAll(obstaclesToRemove)
            }

            // Verificar si la bola colisiona con el enemigo
            if (enemy.collidesWith(ball.x, ball.y, ball.radius)) {
                error.start()
                ball.resetPosition()
                lives -= 1
                // Check if the game is over
                if (lives <= 0) {
                    gameState = GameState.GameOver
                    onGameOver?.invoke()
                }
            }

            // Pasar al siguiente nivel si no hay obstáculos
            if (obstacles.isEmpty()) {
                if (currentLevelIndex < levels.lastIndex) {
                    currentLevelIndex++
                    val currentLevel = levels[currentLevelIndex]
                    obstacles.addAll(createObstaclesForLevel(currentLevel))
                } else {
                    gameState = GameState.GameOver
                    onGameRestart?.invoke()
                }
            }

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

            // Actualizar la posición del enemigo
            enemy.update()
            if (enemy.x < 0 || enemy.x + enemy.width > width) {
                enemy.xVelocity = -enemy.xVelocity
            }
            if (enemy.y < 0 || enemy.y + enemy.height > height) {
                enemy.yVelocity = -enemy.yVelocity
            }
        } else if (gameState == GameState.GameOver) {
            // game over
        }
        postInvalidate()
    }


    fun getGameState(): GameState {
        return gameState
    }

    fun setGameState(state: GameState) {
        gameState = state
        if (state == GameState.Playing || state == GameState.Waiting) {
            gameOverTouched = false
        }
    }

    fun resetGame() {
        ball.resetPosition()
        Log.d("MyGameView", "Game reset - score: $score")
        updateHighScore()
        currentLevelIndex = 0
        score = 0
        lives = 3 // restablecer las vidas a 3 al reiniciar el juego
        gameState = GameState.Waiting
        obstacles.clear()
        obstacles.addAll(createObstaclesForLevel(levels[currentLevelIndex]))

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
