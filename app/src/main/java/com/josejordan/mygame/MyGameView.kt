package com.josejordan.mygame

import android.app.GameState
import android.content.Context
import android.graphics.*
import android.media.MediaPlayer
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.sqrt

class MyGameView(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs),
    SurfaceHolder.Callback {

    private var thread: GameThread? = null
    private val paint = Paint()
    private lateinit var ball: Ball
    private lateinit var enemy: Enemy
    private lateinit var obstacle: Obstacle
    private var obstacles: MutableList<Obstacle> = mutableListOf()
    private var enemies: MutableList<Enemy> = mutableListOf()
    private val obstaclesLock = Any()
    private val enemiesLock = Any()
    private val scoreLock = Any()
    private var score = 0 // variable de puntuación
    private var currentLevel = Level(0F, 0, 0,0) // nivel actual
    private var gameOverTouched = false
    private val levels = listOf(
        Level(3f, 3, R.raw.space,0),
        Level(4f, 5, R.raw.space,0),
        Level(5f, 8, R.raw.space,0),
        Level(6f, 12, R.raw.space,0),
        Level(7f, 15, R.raw.space,0),
        Level(8f, 20, R.raw.space,0),
        Level(9f, 25, R.raw.space,0),
        Level(10f,30, R.raw.space,0),
        Level(10f,35, R.raw.space,0),
        Level(5f, 5, R.raw.ware,1),
        Level(6f, 6, R.raw.robot,2),
        Level(7f, 7, R.raw.retro,3),
        Level(8f, 8, R.raw.land,4)
    )
    private var currentLevelIndex = -1 // nivel actual
    private var gameState: GameState = GameState.Waiting

    enum class GameState {
        Waiting,
        Playing,
        GameOver,
        Paused
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
    private val space = MediaPlayer.create(context, R.raw.space)
    private val ware = MediaPlayer.create(context, R.raw.ware)
    private val robot = MediaPlayer.create(context, R.raw.robot)
    private val error = MediaPlayer.create(context, R.raw.error)
    private val retro = MediaPlayer.create(context, R.raw.retro)
    private val land = MediaPlayer.create(context, R.raw.land)
    private var mediaPlayer: MediaPlayer? = null
    private var pause = false
    private var mediaPlayerCurrentPosition: Int = 0

    private val mediaPlayerList = mutableListOf<MediaPlayer>().apply {
        add(MediaPlayer.create(context, R.raw.pop))
        add(MediaPlayer.create(context, R.raw.space))
        add(MediaPlayer.create(context, R.raw.ware))
        add(MediaPlayer.create(context, R.raw.robot))
        add(MediaPlayer.create(context, R.raw.error))
        add(MediaPlayer.create(context, R.raw.retro))
        add(MediaPlayer.create(context, R.raw.land))
    }


    init {
        holder.addCallback(this)
        paint.color = Color.WHITE
    }
    fun pause() {
        pause = !pause
    }

    fun pauseMediaPlayer() {
        if (mediaPlayer!!.isPlaying) {
            mediaPlayerCurrentPosition = mediaPlayer!!.currentPosition
            mediaPlayer!!.pause()
        }
    }

    fun resumeMediaPlayer() {
        mediaPlayer!!.seekTo(mediaPlayerCurrentPosition)
        mediaPlayer!!.start()
    }

    fun releaseMediaPlayer() {
        mediaPlayer!!.release()
    }

    private fun updateHighScore() {
        val prefs = context.getSharedPreferences(MY_GAME_PREFS, Context.MODE_PRIVATE)
        val highScore = prefs.getInt(HIGH_SCORE_KEY, 0)
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
        val speed = distance / 40 // ajustar este valor para cambiar la velocidad de la bola
        ball.xVelocity = dx / distance * speed
        ball.yVelocity = dy / distance * speed
    }


    private fun createObstaclesForLevel(level: Level): List<Obstacle> {
        val ballSpeed = level.ballSpeed
        val obstacleCount = level.obstacleCount
        val speedMultiplier = currentLevelIndex + 1
        val speed = ballSpeed * speedMultiplier

        // Liberar los recursos de la melodía anterior
        mediaPlayer?.release()

        // Cargar y reproducir la nueva melodía
        mediaPlayer = MediaPlayer.create(context, level.melodyId)
        mediaPlayer?.start()
        mediaPlayer?.setVolume(0.5f, 0.5f)
        mediaPlayer?.isLooping = true

/*        // Liberar los recursos de la melodía anterior
        mediaPlayerList[currentLevelIndex].release()
        // Cargar y reproducir la nueva melodía
        mediaPlayerList[currentLevelIndex] = MediaPlayer.create(context, level.melodyId)
        mediaPlayerList[currentLevelIndex].start()
        mediaPlayerList[currentLevelIndex].setVolume(0.5f, 0.5f)
        mediaPlayerList[currentLevelIndex].isLooping = true*/


        // Nueva variable para mantener la puntuación de cada nivel
        val scoreByLevel = score
        return Obstacle.createRandomObstacles(obstacleCount,speed,50f,width,height,ballSpeed,ballSpeed)
            .onEach { it.scoreByLevel = scoreByLevel }
    }

    fun createObstaclesForLevel1(level: Level): List<Obstacle> {

        //Obtener un color aleatorio de Colors.kt
        val color = Colors.colors[Colors.random.nextInt(Colors.colors.size)]
        val paint = Paint().apply {
            this.color = color
            style = Paint.Style.FILL
        }

        val obstacleList = mutableListOf<Obstacle>()
        for (i in 0 until level.obstacleCount) {
            obstacle = Obstacle(level.obstacleCount.toFloat(),level.ballSpeed, width.toFloat() / 4,height.toFloat() /4,level.ballSpeed,paint)
            obstacleList.add(obstacle)
        }
        return obstacleList
    }
    fun createEnemiesForLevel(level: Level): List<Enemy> {
        val enemyList = mutableListOf<Enemy>()
        for (i in 0 until level.enemyCount) {
            enemy = Enemy(width.toFloat() / 4,height.toFloat() /4, 100f,100f,5f,5f)
            enemyList.add(enemy)
        }
        return enemyList
    }




    override fun surfaceCreated(holder: SurfaceHolder) {
        if (width > 0 && height > 0) {
            ball = Ball(width.toFloat() / 2, height.toFloat() / 2, 50f, 10f, 10f)
            enemy = Enemy(width.toFloat() / 4,height.toFloat() /4, 100f,100f,5f,5f)
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

        // Copia de la lista de enemigos actual
        val currentEnemies: List<Enemy>
        synchronized(enemiesLock) {
            currentEnemies = ArrayList(enemies)
        }

        // Dibujar cada obstáculo en la lista
        for (obstacle in currentObstacles) {
            obstacle.draw(canvas!!)
        }

        // Dibujar cada enemigo en la lista
        for (enemy in currentEnemies) {
            enemy.draw(canvas!!)
        }

        canvas?.drawText("Score: $score", 50f, 100f, scorePaint)
        canvas?.drawText("Level: ${currentLevelIndex}", 50f, 200f, scorePaint)
        canvas?.drawText("Lives: $lives", 50f, 300f, scorePaint) // Muestra la cantidad de vidas

        if (gameState == GameState.GameOver) {
            val gameOverText = "GAME OVER"
            val playAgainWidth = playAndGameOverPaint.measureText(gameOverText)
            canvas?.drawText(
                gameOverText,
                (width - playAgainWidth) / 2,
                height / 2f,
                playAndGameOverPaint
            )
        } else if (gameState == GameState.Waiting) {
            val playText = "PLAY"
            val playWidth = playAndGameOverPaint.measureText(playText)
            canvas?.drawText(
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

            for (enemy in enemies) {
                if (enemy.collidesWith(ball.x, ball.y, ball.radius)) {
                    error.start()
                    error.setVolume(5f,5f)
                    ball.resetPosition()
                    lives -= 1
                    // Check if the game is over
                    if (lives <= 0) {
                        gameState = GameState.GameOver
                        onGameOver?.invoke()
                        mediaPlayer?.stop()
                    }
                }
            }

            // Pasar al siguiente nivel si no hay obstáculos
            if (obstacles.isEmpty()) {
                if (currentLevelIndex < levels.lastIndex) {
                    currentLevelIndex++
                    currentLevel = levels[currentLevelIndex]
                    obstacles.addAll(createObstaclesForLevel(currentLevel))
                    enemies.addAll(createEnemiesForLevel(currentLevel))
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

            for (enemy in enemies) {
                enemy.update()
                if (enemy.x < 0 || enemy.x + enemy.width > width) {
                    enemy.xVelocity = -enemy.xVelocity
                }
                if (enemy.y < 0 || enemy.y + enemy.height > height) {
                    enemy.yVelocity = -enemy.yVelocity
                }
            }


        } else if (gameState == GameState.GameOver) {

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
        updateHighScore()
        currentLevelIndex = -1
        score = 0
        lives = 3 // restablecer las vidas a 3 al reiniciar el juego
        gameState = GameState.Waiting
        obstacles.clear()
        enemies.clear()
        if (currentLevelIndex >= 0 && currentLevelIndex < levels.size) {
            obstacles.addAll(createObstaclesForLevel(levels[currentLevelIndex]))
            enemies.addAll(createEnemiesForLevel(levels[currentLevelIndex]))
        }
    }


    inner class GameThread(private val holder: SurfaceHolder) : Thread() {
        private var running = true

        override fun run() {
            while (running) {
                var canvas: Canvas? = null
                try {
                    if (!pause) {
                        canvas = holder.lockCanvas()
                        synchronized(holder) {
                            update()
                            draw(canvas)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
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
