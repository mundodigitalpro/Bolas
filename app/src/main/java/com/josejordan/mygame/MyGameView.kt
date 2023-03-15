package com.josejordan.mygame

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import kotlin.math.sqrt

class MyGameView(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs), SurfaceHolder.Callback {

    private var thread: GameThread? = null
    private val paint = Paint()
    private var x = 200f
    private var y = 200f
    private var xVelocity = 10f
    private var yVelocity = 10f

    init {
        holder.addCallback(this)
        paint.color = Color.WHITE
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    fun moveTo(x: Float, y: Float) {
        val dx = x - this.x
        val dy = y - this.y
        val distance = sqrt(dx * dx + dy * dy)
        val speed = distance / 10 // ajustar este valor para cambiar la velocidad de la bola
        xVelocity = dx / distance * speed
        yVelocity = dy / distance * speed
    }



    override fun surfaceCreated(holder: SurfaceHolder) {
        thread = GameThread(holder)
        thread?.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var retry = true
        while (retry) {
            try {
                thread?.join()
                retry = false
            } catch (_: InterruptedException) {
            }
        }
    }

    override fun draw(canvas: Canvas?) {
        super.draw(canvas)
        Log.d("MyGameView", "draw()")
        canvas?.drawColor(Color.BLACK)
        canvas?.drawCircle(x, y, 50f, paint)
        Log.d("MyGameView", "draw: x=$x y=$y")

    }

    private fun update() {
        Log.d("MyGameView", "update: x=$x y=$y")
        x += xVelocity
        y += yVelocity

        // Cambiar dirección si la bola llega a los bordes del lienzo
        if (x - 50f < 0 || x + 50f > width) {
            xVelocity = -xVelocity
        }
        if (y - 50f < 0 || y + 50f > height) {
            yVelocity = -yVelocity
        }
        postInvalidate()
    }

    inner class GameThread(private val holder: SurfaceHolder) : Thread() {
        private var running = true

        override fun run() {
            while (running) {
                var canvas: Canvas? = null
                try {
                    canvas = holder.lockCanvas()
                    synchronized(holder) {
                        update()
                        draw(canvas)
                    }
                } finally {
                    canvas?.let {
                        holder.unlockCanvasAndPost(it)
                    }
                }
            }

            fun stopThread() {
                running = false
            }
        }
    }
}