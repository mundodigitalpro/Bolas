package com.josejordan.mygame
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent

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
                        if (gameView.isGameOverTouched()) {
                            gameView.resetGame()
                        } else {
                            gameView.setGameOverTouched(true)
                        }
                    }
                }

                MotionEvent.ACTION_UP -> {
                    view.performClick()
                }
            }
            true
        }


    }
}

