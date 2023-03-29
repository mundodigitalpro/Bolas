package com.josejordan.mygame

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: MyGameView
    private lateinit var exitButton: Button
    private lateinit var pauseButton: ImageButton
    private lateinit var pauseButtonIcon: Drawable
    private var isPaused = false

    lateinit var highScoreTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //hide the status bar and navigation buttons
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = false
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = false
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        setContentView(R.layout.activity_main)

        gameView = findViewById(R.id.my_game_view)
        gameView.requestFocus()

        exitButton = findViewById(R.id.exitButton)
        pauseButton = findViewById(R.id.pauseButton)
        pauseButtonIcon = pauseButton.background
        highScoreTextView = findViewById(R.id.highScoreTextView)

        val prefs = getSharedPreferences("MyGamePrefs", Context.MODE_PRIVATE)
        if (prefs.contains(HIGH_SCORE_KEY)) {
            val highScore = prefs.getInt(HIGH_SCORE_KEY, 0)
            highScoreTextView.text = getString(R.string.high_score, highScore)
        } else {
            highScoreTextView.text = getString(R.string.high_score, 0)
        }



        gameView.setOnTouchListener { _, event ->
            if (!isPaused) {
                if (event.action == MotionEvent.ACTION_DOWN) {
                    if (gameView.getGameState() == MyGameView.GameState.Waiting) {
                        gameView.setGameState(MyGameView.GameState.Playing)
                    } else if (gameView.getGameState() == MyGameView.GameState.GameOver) {
                        gameView.resetGame()
                        updateExitButtonVisibility()
                    } else {
                        gameView.performClick()
                        gameView.moveTo(event.x, event.y)
                    }
                }
            }
            true
        }

        exitButton.setOnClickListener {
            finish()
        }

        pauseButton.setOnClickListener {
            if (gameView.getGameState() == MyGameView.GameState.Playing) {
                gameView.setGameState(MyGameView.GameState.Paused)
                gameView.pauseMediaPlayer()
                gameView.setOnTouchListener(null) // Remove the onTouchListener when game is paused
                pauseButton.setImageResource(R.drawable.ic_play)
            } else if (gameView.getGameState() == MyGameView.GameState.Paused) {
                gameView.setGameState(MyGameView.GameState.Playing)
                gameView.resumeMediaPlayer()
                gameView.setOnTouchListener(onTouchListener) // Set the onTouchListener back when game is resumed
                pauseButton.setImageResource(R.drawable.ic_pause)
            }
            updateExitButtonVisibility()
        }




        gameView.onGameOver = {
            runOnUiThread {
                updateExitButtonVisibility()
            }
        }

        updateExitButtonVisibility()

    }
    // Define the onTouchListener outside of onCreate method so it can be reused
    private val onTouchListener = View.OnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_DOWN) {
            if (gameView.getGameState() == MyGameView.GameState.Waiting) {
                gameView.setGameState(MyGameView.GameState.Playing)
            } else if (gameView.getGameState() == MyGameView.GameState.GameOver) {
                gameView.resetGame()
                updateExitButtonVisibility()
            } else {
                gameView.performClick()
                gameView.moveTo(event.x, event.y)
            }
        }
        true
    }

    private fun updateExitButtonVisibility() {
        if (gameView.getGameState() == MyGameView.GameState.GameOver || gameView.getGameState() == MyGameView.GameState.Paused) {
            exitButton.visibility = View.VISIBLE
        } else {
            exitButton.visibility = View.GONE
        }
    }

}

