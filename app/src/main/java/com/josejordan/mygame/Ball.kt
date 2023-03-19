package com.josejordan.mygame

class Ball(var x: Float, var y: Float, var radius: Float, var xVelocity: Float, var yVelocity: Float) {

    private val startX = x
    private val startY = y

    fun update() {
        x += xVelocity
        y += yVelocity
    }

    fun resetPosition() {
        x = startX
        y = startY
        xVelocity = 0f
        yVelocity = 0f
    }
}

