package com.josejordan.mygame

class Ball(var x: Float, var y: Float, var radius: Float, var xVelocity: Float, var yVelocity: Float) {

    fun update() {
        x += xVelocity
        y += yVelocity
    }
}
