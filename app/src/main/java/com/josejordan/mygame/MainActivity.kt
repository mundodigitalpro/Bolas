package com.josejordan.mygame
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: MyGameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gameView = findViewById<MyGameView>(R.id.my_game_view)
        gameView.requestFocus()

        gameView.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    gameView.moveTo(event.x, event.y)
                }
                MotionEvent.ACTION_UP -> {
                    view.performClick()
                }
            }
            true
        }
    }
}

