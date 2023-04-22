package com.josejordan.mygame

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {

    private lateinit var gameView: MyGameView
    private lateinit var exitButton: Button
    private lateinit var pauseButton: ImageButton
    private var isPaused = false
    lateinit var highScoreTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //hide the status bar and navigation buttons
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val controller = window.insetsController
            if (controller != null) {
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                controller.hide(WindowInsets.Type.systemBars())
            }
        } else {
            // Use the deprecated method for older Android versions
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
        }


        gameView = findViewById(R.id.my_game_view)
        gameView.requestFocus()
        exitButton = findViewById(R.id.exitButton)
        pauseButton = findViewById(R.id.pauseButton)
        highScoreTextView = findViewById(R.id.highScoreTextView)

        val prefs = getSharedPreferences(MY_GAME_PREFS, Context.MODE_PRIVATE)
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
            onBackPressedDispatcher.onBackPressed()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle(getString(R.string.exit_dialog))
                    .setMessage(getString(R.string.sure))
                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                        finishAffinity()
                        exitProcess(0) // This will close the app and all its activities

                    }
                    .setNegativeButton(getString(R.string.no), null)
                    .show()
            }
        })

        pauseButton.setOnClickListener {
            if (gameView.getGameState() == MyGameView.GameState.Playing) {
                gameView.setGameState(MyGameView.GameState.Paused).also {
                    gameView.pauseMediaPlayer()
                    gameView.setOnTouchListener(null) // Remove the onTouchListener when game is paused
                    pauseButton.setImageResource(R.drawable.ic_play)
                }
            } else if (gameView.getGameState() == MyGameView.GameState.Paused) {
                gameView.setGameState(MyGameView.GameState.Playing).also {
                    gameView.resumeMediaPlayer()
                    gameView.setOnTouchListener(onTouchListener) // Set the onTouchListener back when game is resumed
                    pauseButton.setImageResource(R.drawable.ic_pause)
                }
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

    override fun onPause() {
        super.onPause()
        //saveGameState()
        if (gameView.getGameState() == MyGameView.GameState.Playing) {
            gameView.pauseMediaPlayer()
        }


    }

    override fun onResume() {
        super.onResume()
        //loadGameState() // Recupera el estado del juego de las preferencias compartidas y lo restaura
        if (gameView.getGameState() == MyGameView.GameState.Playing) {
            gameView.resumeMediaPlayer()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
       //saveGameState()
        gameView.pauseMediaPlayer() // Pausa el MediaPlayer si no lo has hecho en onPause()
        gameView.releaseMediaPlayer() // Libera los recursos del MediaPlayer
    }


}

