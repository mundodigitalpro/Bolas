package com.josejordan.mygame

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: MyGameView
    private lateinit var exitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gameView = findViewById(R.id.my_game_view)
        gameView.requestFocus()
        exitButton = findViewById(R.id.exitButton)

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

