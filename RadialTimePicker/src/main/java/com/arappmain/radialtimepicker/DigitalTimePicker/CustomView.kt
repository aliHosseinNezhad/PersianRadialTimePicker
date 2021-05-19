package com.arappmain.radialtimepicker.DigitalTimePicker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.CountDownTimer
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.MotionEvent.PointerCoords
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Toast
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.*


@SuppressLint("ClickableViewAccessibility")
class CustomView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var cx2: Float = 0f
    private var cy2: Float = 0f
    val TAG = "d34io"
    private var cy: Float = 0f
    private var cx: Float = 0f
    private var circleRadiusWeight: Float = 0f
    var circleRadius = 0f
    var arrayPointers = ArrayList<PointerCoords>()
    var animation = object : CountDownTimer(4000, 10) {
        override fun onTick(millisUntilFinished: Long) {
            var t = 4000f - millisUntilFinished
            t /= 1000f
            circleRadiusWeight = abs((1 * cos(t * PI / 2f)).toFloat())
            postInvalidate()
        }

        override fun onFinish() {
            circleRadiusWeight = 1f
        }
    }

    init {
        var gestureDetector = GestureDetector(context, MyGestureDetector())
        var gestureListener = OnTouchListener { v, event ->
            gestureDetector.onTouchEvent(event)
        }
        setOnTouchListener(gestureListener)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        try {
            cx *= w / oldw
            cy *= h / oldh
        } catch (e: Exception) {

        }

    }

    var rect = Rect();
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let { canvas ->
            rect = canvas.clipBounds
            canvas.drawRect(rect, Paint().also {
                it.color = Color.BLUE
                it.style = Paint.Style.STROKE
            })
            Toast.makeText(context, "${arrayPointers.size}", Toast.LENGTH_SHORT).show()
            canvas.drawCircle(
                cx,
                cy,
                100f,
                Paint().apply {
                    this.style = Paint.Style.FILL
                    this.color = Color.BLUE
                    this.isAntiAlias = true
                })
            canvas.drawCircle(
                cx2,
                cy2,
                100f,
                Paint().apply {
                    this.style = Paint.Style.FILL
                    this.color = Color.BLUE
                    this.isAntiAlias = true
                })

        }
    }


    var startTime = 0L
    var endTime = 0L
    var startY = 0
    var endY = 0
    var moveY = 0
    var moveTime = 0L
    var calender = Calendar.getInstance()
    val duration get() = (endTime - startTime).let { if (it == 0L) 1L else it }
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let { event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    startTime = Calendar.getInstance().timeInMillis
                    startY = event.y.toInt()
                }
                MotionEvent.ACTION_UP -> {
                    endY = event.y.toInt()
                    endTime = Calendar.getInstance().timeInMillis
                    Log.i(
                        TAG,
                        "***** UP *****"
                    )
                    Log.i(
                        TAG,
                        " y: ${event.y}"
                    )
                    Log.i(
                        TAG,
                        "duration 1: ${endTime - startTime} velocity: ${(endY - startY) / (duration / 1000f)}"
                    )
                    Log.i(
                        TAG,
                        "duration 2: ${endTime - moveTime} velocity: ${(endY - moveY) / ((endTime - moveTime) / 1000f)}"
                    )
                    Log.i(
                        TAG,
                        "**********"
                    )
                }
                MotionEvent.ACTION_MOVE -> {
                    moveY = event.y.toInt()
                    moveTime = Calendar.getInstance().timeInMillis
                    cx = event.x
                    cy = event.y

                    Log.i(
                        TAG,
                        "***** MOVE *****"
                    )
                    Log.i(
                        TAG,
                        " y: ${event.y}"
                    )

                    Log.i(
                        TAG,
                        "**********"
                    )
                    postInvalidate()
                }
                else -> {
                }

            }

        }
        return true
    }

    inner class MyGestureDetector : SimpleOnGestureListener() {
        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            Log.i("TAG123", "vy: $velocityY")
            val timer = object : TimerTask() {
                var y = e2.y
                var vy = velocityY
                var tm = 0;
                override fun run() {
                    tm += 1
                    val t = tm/1000f
                    cy = -2000f * t * t + vy * t + y
                    if (-4000f*t + vy  in -1f..1f){
                        cancel()
                    }
                    postInvalidate()
                }
            }
            Timer().scheduleAtFixedRate(
                timer, 0, 1
            )

//            try {
//                if (Math.abs(e1.y - e2.y) > SWIPE_MAX_OFF_PATH) return false
//                // right to left swipe
//                if (e1.x - e2.x > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                    Toast.makeText(this@SelectFilterActivity, "Left Swipe", Toast.LENGTH_SHORT)
//                        .show()
//                } else if (e2.x - e1.x > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
//                    Toast.makeText(this@SelectFilterActivity, "Right Swipe", Toast.LENGTH_SHORT)
//                        .show()
//                }
//            } catch (e: Exception) {
//                // nothing
//            }
            return false
        }

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }
    }
}