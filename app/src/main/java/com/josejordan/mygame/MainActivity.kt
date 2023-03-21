package com.josejordan.mygame

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import androidx.appcompat.app.AlertDialog

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: MyGameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gameView = findViewById(R.id.my_game_view)
        gameView.requestFocus()

        gameView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (gameView.getGameState() == MyGameView.GameState.Waiting) {
                        gameView.setGameState(MyGameView.GameState.Playing)
                    } else if (gameView.getGameState() == MyGameView.GameState.Playing) {
                        gameView.moveTo(event.x, event.y)
                    } else if (gameView.getGameState() == MyGameView.GameState.GameOver) {
                        gameView.resetGame()
                    }

/*                    else if (gameView.getGameState() == MyGameView.GameState.Exit) {
                        finish()
                    }*/
                }
                MotionEvent.ACTION_UP -> {
                    view.performClick()
                }
            }
            true
        }




    }
    fun showExitDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Salir del juego")
        builder.setMessage("¿Está seguro que desea salir del juego?")
        builder.setPositiveButton("Sí") { _, _ ->
            // Finaliza la actividad para salir del juego
            (this as Activity).finish()
        }
        builder.setNegativeButton("No") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

}

