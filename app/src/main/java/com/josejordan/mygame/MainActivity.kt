package com.josejordan.mygame

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {

    private lateinit var gameView: MyGameView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myGameView = findViewById<MyGameView>(R.id.my_game_view)
        myGameView.requestFocus()
    }

}

