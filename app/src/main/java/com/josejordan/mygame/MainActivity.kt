package com.josejordan.mygame

import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: MyGameView
    private lateinit var exitButton: Button
    lateinit var highScoreTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gameView = findViewById(R.id.my_game_view)
        gameView.requestFocus()
        exitButton = findViewById(R.id.exitButton)
        highScoreTextView = findViewById(R.id.highScoreTextView)

        val prefs = getSharedPreferences("MyGamePrefs", Context.MODE_PRIVATE)
        if (prefs.contains(MyGameView.HIGH_SCORE_KEY)) {
            val highScore = prefs.getInt(MyGameView.HIGH_SCORE_KEY, 0)
            highScoreTextView.text = getString(R.string.high_score, highScore)
        } else {
            highScoreTextView.text = getString(R.string.high_score, 0)
        }

        gameView.setOnTouchListener { _, event ->
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

        exitButton.setOnClickListener {
            finish()
        }

        gameView.onGameOver = {
            runOnUiThread {
                updateExitButtonVisibility()
            }
        }

        updateExitButtonVisibility()
    }

    private fun updateExitButtonVisibility() {
        if (gameView.getGameState() == MyGameView.GameState.GameOver) {
            exitButton.visibility = View.VISIBLE
        } else {
            exitButton.visibility = View.GONE
        }
    }
}

