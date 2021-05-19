package com.arappmain.radialtimepicker

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Build
import android.os.CountDownTimer
import android.text.TextPaint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.annotation.FloatRange
import androidx.core.content.res.ResourcesCompat
import com.arappmain.radialtimepicker.ClockMode.*
import kotlin.math.*

class RadialTimePickerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    var clockMode: ClockMode = Hour12
        set(value) {
            field = value
            if (value == Minute) {
                hideCenterMinuteAnimation.cancel()
                if (centerClockNumberRadiusWeight < 1f) {
                    showCenterMinuteAnimation.start()
                }
                postInvalidate()
            } else {
                showCenterMinuteAnimation.cancel()
                if (centerClockNumberRadiusWeight > 0f) {
                    hideCenterMinuteAnimation.start()
                }
                postInvalidate()
            }
        }
    private var selectorRadiusWeight: Float = 1f


    private var centerClockWeight: Float = 0.6f

    @FloatRange(from = 0.000000, to = 1.00000)
    private var selectorAngleWeight: Float = 1f
    private var previousSelectorAngleWeight: Float = -1f
    private var onTimeChangeListener: OnTimeChangeListener? = null


    // Components Color
    var selectorColor = Color.rgb(0, 160, 170)
        set(value) {
            field = value
            initPaint()
            postInvalidate()
        }
    var selectorTextColor = Color.WHITE
        set(value) {
            field = value
            initPaint()
            postInvalidate()
        }
    var clockRadius: Int = Math.min(width, height)
        set(value) {
            field = value
            initPaint()
            postInvalidate()
        }
    var textsColors = Color.WHITE
        set(value) {
            field = value
            initPaint()
            postInvalidate()
        }
    var clockBackColor = Color.rgb(240, 240, 245)
        set(value) {
            field = value
            initPaint()
            postInvalidate()
        }
    var clockNumberBackColor = Color.rgb(180, 190, 200)
        set(value) {
            field = value
            initPaint()
            postInvalidate()
        }
    var textTypeface: Typeface? = null
        set(value) {
            field = value
            initPaint()
            postInvalidate()
        }


    private var hourArray = intArrayOf(12, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
    private var hourArray2 = intArrayOf(24, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23)
    private var minuteArray = intArrayOf(0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55)

    private var selectorRadiusChangeAnimationDuration: Long = 300
    private var selectorAngleChangeAnimationDuration: Long = 500
    private var animCountDownInterval: Long = 10
    private var centerClockNumberRadiusWeight = 0f
    private var clockNumberRadiusWeight = 0.15f
    private var clockBackPaint = Paint().also {
        it.isAntiAlias = true
        it.color = clockBackColor
        it.style = Paint.Style.FILL
    }
    private var clockNumbersBackPaint = Paint().also {
        it.isAntiAlias = true
        it.color = clockNumberBackColor
        it.style = Paint.Style.FILL
    }
    private var timeSelectorTextPaint = TextPaint().also {
        it.color = selectorTextColor
        it.typeface = textTypeface
        it.isAntiAlias = true
        it.textAlign = Paint.Align.CENTER
    }
    private var timeSelectorPaint = Paint().apply {
        color = selectorColor
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    private var clockTextPaint = TextPaint().apply {
        color = textsColors
        textSize = 60f
        isAntiAlias = true
        typeface = textTypeface
    }

    private var showCenterMinuteAnimation =
        object : CountDownTimer(selectorRadiusChangeAnimationDuration, animCountDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                centerClockNumberRadiusWeight =
                    cos((millisUntilFinished) / selectorRadiusChangeAnimationDuration.toFloat() * Math.PI / 2).toFloat()
                timeSelectorTextPaint.alpha = (centerClockNumberRadiusWeight * 255).toInt()
                postInvalidate()
            }

            override fun onFinish() {
                centerClockNumberRadiusWeight = 1f
                timeSelectorTextPaint.alpha = 255
                postInvalidate()
            }
        }
    private var hideCenterMinuteAnimation =
        object : CountDownTimer(selectorRadiusChangeAnimationDuration, animCountDownInterval) {
            override fun onTick(millisUntilFinished: Long) {
                centerClockNumberRadiusWeight =
                    sin((millisUntilFinished) / selectorRadiusChangeAnimationDuration.toFloat() * Math.PI / 2).toFloat()
                timeSelectorTextPaint.alpha = (centerClockNumberRadiusWeight * 255).toInt()
                postInvalidate()
            }

            override fun onFinish() {
                centerClockNumberRadiusWeight = 0f
                timeSelectorTextPaint.alpha = 0
                postInvalidate()
            }

        }
    private var smoothMoveSelectorAnimation =
        object : AnimCountDownTimer(selectorAngleChangeAnimationDuration, animCountDownInterval) {
            var t: Float = 0f
            var weight: Float = 1f
            var tp: Float = (Math.PI / 2f).toFloat()
            override fun onTick(millisUntilFinished: Long) {
                t =
                    (selectorAngleChangeAnimationDuration - millisUntilFinished) / selectorAngleChangeAnimationDuration.toFloat()
                selectorAngleWeight =
                    circularNumber(aw1 + circularDifference(aw1, aw2) * sin(t * tp))
                Log.i("TAG022", "onTick: selectorAngleWeight:$selectorAngleWeight")
                selectorRadiusWeight = rw1 + (rw2 - rw1) * sin(t * tp)
                postInvalidate()
            }

            override fun onFinish() {
                selectorRadiusWeight = rw2
                selectorAngleWeight = aw2
                Log.i("TAG022", "onTick: end ******* selectorAngleWeight:$selectorAngleWeight")
                Log.i("TAG022", "onTick: end ******* aw1:$aw1")
                Log.i("TAG022", "onTick: end ******* aw2:$aw2")
                postInvalidate()
            }
        }

    private fun moveSelectorWithAnimation(aw1: Float, aw2: Float, rw1: Float, rw2: Float) {
//        Toast.makeText(context, "aws: $aw1 , awe:$aw2", Toast.LENGTH_SHORT).show()
        smoothMoveSelectorAnimation.let {
            it.aw1 = aw1
            it.aw2 = aw2
            it.rw1 = rw1
            it.rw2 = rw2
            it.start()
        }
    }

    fun getIndexBySelectorRadiusWeight(angleWeight: Float): Int {
        return when (clockMode) {
            Minute -> (angleWeight * 60).toInt() % 60
            Hour12 -> (angleWeight * 12).toInt() % 12
            Hour24 -> (angleWeight * 12).toInt() % 12
        }
    }

    fun getAngleByIndex(index: Int): Float {
        return when (clockMode) {
            Minute -> index / 60f
            Hour12 -> index / 12f
            Hour24 -> index / 12f
        }
    }

    fun getIndexByTime(time: Int, clockMode: ClockMode): Int {
        return when (clockMode) {
            Minute -> {
                if (time in 0..59)
                    time
                else 0
            }
            Hour12 -> {
                if (time in 0..12) {
                    time % 12
                } else 0
            }
            Hour24 -> {
                if (time in 1..11) {
                    time
                } else if (time == 12) {
                    0
                } else if (time in 13..23) {
                    time - 12
                } else 0
            }
        }
    }

    fun getClockNumbersCount(): Int {
        return if (clockMode == Minute) {
            60
        } else 12
    }

    fun setTime(time: Int, clockMode: ClockMode, animate: Boolean = true) {
        this.clockMode = clockMode
        val angle1 = selectorAngleWeight
        val radius1 = selectorRadiusWeight
        val angle2 = getAngleByIndex(getIndexByTime(time, clockMode))
        val radius2 = getRadiusByTime(time, clockMode)
        if (animate)
            moveSelectorWithAnimation(angle1, angle2, radius1, radius2)
        else {
            selectorAngleWeight = angle2
            selectorRadiusWeight = radius2
            postInvalidate()
        }
    }

    private fun getRadiusByTime(time: Int, clockMode: ClockMode): Float {
        return when (clockMode) {
            Hour24 -> {
                if (time in 1..12) {
                    1f
                } else if (time == 0 || time in 13..24) {
                    centerClockWeight
                } else centerClockWeight
            }
            Hour12 -> {
                1f
            }
            Minute -> {
                1f
            }
        }
    }

    fun getTime(): Int {
        val index = getIndexBySelectorRadiusWeight(selectorAngleWeight)
        return when (clockMode) {
            Minute -> index
            Hour12 -> index
            Hour24 -> index.let {
                if (selectorRadiusWeight == centerClockWeight) {
                    if (index == 0)
                        0
                    else index + 12
                } else if (index == 0) 12
                else index
            }
        }
    }

    fun setOnTimeChangeListener(onTimeChangeListener: OnTimeChangeListener) {
        this.onTimeChangeListener = onTimeChangeListener
    }

    init {
//        setTime(0, Hour12)
        clockMode = Hour24
        setValueWithStyledAttributes(attrs)
        initPaint()
    }

    private fun setValueWithStyledAttributes(attrs: AttributeSet?) {
        val typedArray = context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.RadialTimePickerView,
            0, 0
        )

        clockBackColor =
            typedArray.getColor(R.styleable.RadialTimePickerView_clock_background, clockBackColor)
        selectorColor =
            typedArray.getColor(R.styleable.RadialTimePickerView_time_selector_color, selectorColor)
        clockNumberBackColor = typedArray.getColor(
            R.styleable.RadialTimePickerView_clock_numbers_background,
            clockNumberBackColor
        )
        textsColors =
            typedArray.getColor(R.styleable.RadialTimePickerView_clock_numbers_text_color, textsColors)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            textTypeface = typedArray.getFont(R.styleable.RadialTimePickerView_text_typeface)
        } else {
            var x = typedArray.getResourceId(
                R.styleable.RadialTimePickerView_text_typeface, 0
            )
            if (x != 0) {
                textTypeface = ResourcesCompat.getFont(
                    context,
                    x
                )
            }
        }


        typedArray.getInt(R.styleable.RadialTimePickerView_clock_mode, 1).let {
            clockMode =
                ClockMode.values()[typedArray.getInt(R.styleable.RadialTimePickerView_clock_mode, 0)]
        }

        typedArray.recycle()
    }

    private fun initPaint() {
        clockBackPaint = Paint().also {
            it.isAntiAlias = true
            it.color = clockBackColor
            it.style = Paint.Style.FILL
        }
        clockNumbersBackPaint = Paint().also {
            it.isAntiAlias = true
            it.color = clockNumberBackColor
            it.style = Paint.Style.FILL
        }
        timeSelectorTextPaint = TextPaint().also {
            it.color = selectorTextColor
            it.typeface = textTypeface
            it.isAntiAlias = true
            it.textAlign = Paint.Align.CENTER
        }
        timeSelectorPaint = Paint().apply {
            color = selectorColor
            isAntiAlias = true
            style = Paint.Style.FILL
        }
        clockTextPaint = TextPaint().apply {
            color = textsColors
            textSize = 60f
            isAntiAlias = true
            typeface = textTypeface
        }
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.let { canvas ->
            if (clockMode == Minute) {
                val a = Math.PI * 2
                var br = Math.min(width, height) / 2f
                clockRadius = br.roundToInt()
                val rp = br * clockNumberRadiusWeight
                val r = (br - rp) * 0.95f
                drawClockBackground(canvas, width / 2, height / 2, br)
                drawClockNumbersBackground(canvas, width, height, a, r, rp, minuteArray)
                drawSelector(
                    canvas,
                    width,
                    height,
                    a,
                    selectorAngleWeight * 60,
                    r * selectorRadiusWeight,
                    rp * selectorRadiusWeight,
                    5,
                    60
                )
                drawClockNumbers(canvas, width, height, a, r, rp, minuteArray)
            } else if (clockMode == Hour12) {
                val a = Math.PI * 2
                var br = Math.min(width, height) / 2f
                clockRadius = br.roundToInt()
                val rp = br * clockNumberRadiusWeight
                val r = (br - rp) * 0.95f
                drawClockBackground(canvas, width / 2, height / 2, br)
                drawClockNumbersBackground(canvas, width, height, a, r, rp, hourArray)
                drawSelector(
                    canvas,
                    width,
                    height,
                    a,
                    selectorAngleWeight * 12,
                    r * selectorRadiusWeight,
                    rp * selectorRadiusWeight,
                    1,
                    12
                )
                drawClockNumbers(canvas, width, height, a, r, rp, hourArray)
            } else if (clockMode == Hour24) {
                val a = Math.PI * 2
                var br = Math.min(width, height) / 2f
                clockRadius = br.roundToInt()
                val rp = br * clockNumberRadiusWeight
                val r = (br - rp) * 0.95f
                drawClockBackground(canvas, width / 2, height / 2, br)
                drawClockNumbersBackground(canvas, width, height, a, r, rp, hourArray2)
                drawClockNumbersBackground(
                    canvas,
                    width,
                    height,
                    a,
                    r * centerClockWeight,
                    rp * centerClockWeight, hourArray
                )
                drawSelector(
                    canvas,
                    width,
                    height,
                    a,
                    selectorAngleWeight * 12,
                    r * selectorRadiusWeight,
                    rp * selectorRadiusWeight,
                    1,
                    12
                )
                drawClockNumbers(canvas, width, height, a, r, rp, hourArray)
                drawClockNumbers(
                    canvas,
                    width,
                    height,
                    a,
                    r * centerClockWeight,
                    rp * centerClockWeight,
                    hourArray2
                )
            }
        }

    }


    private fun drawTextCenter(canvas: Canvas, paint: Paint, text: String, cx: Float, cy: Float) {
        paint.textAlign = Paint.Align.CENTER
        val textHeight: Float = paint.descent() - paint.ascent()
        val textOffset: Float = textHeight / 2 - paint.descent()
        canvas.drawText(text, cx, cy + textOffset, paint)
    }


    private fun drawSelector(
        canvas: Canvas,
        width: Int,
        height: Int,
        a: Double,
        s: Float,
        r: Float,
        rp: Float,
        interval: Int,
        digitCount: Int
    ) {
        val sx = (width / 2 + r * cos(s * a / digitCount - a / 4)).toFloat()
        val sy = (height / 2 + r * sin(s * a / digitCount - a / 4)).toFloat()
        canvas.drawLine(width / 2f, height / 2f, sx, sy, Paint().apply {
            strokeWidth = rp / 12
            style = Paint.Style.STROKE
            isAntiAlias = true
            color = timeSelectorPaint.color
        })
        canvas.drawCircle(width / 2f, height / 2f, rp / 10, timeSelectorPaint)
        if (s.toInt() % interval != 0) {
            canvas.drawCircle(
                sx,
                sy,
                rp / interval,
                timeSelectorPaint
            )
        } else
            canvas.drawCircle(
                sx,
                sy,
                rp,
                timeSelectorPaint
            )
        showSelectorNumber(
            canvas,
            width,
            height,
            rp * 1.2f * centerClockNumberRadiusWeight,
            s.toInt()
        )
    }

    private fun showSelectorNumber(
        canvas: Canvas,
        width: Int,
        height: Int,
        rp: Float,
        s: Int,
    ) {
        val cx = width / 2f
        val cy = height / 2f
        canvas.drawCircle(cx, cy, rp, timeSelectorPaint)
        drawTextCenter(canvas, timeSelectorTextPaint.apply {
            textSize = rp
        }, s.toString(), cx, cy)
    }


    private fun drawClockBackground(canvas: Canvas, cx: Int, cy: Int, radius: Float) {
        canvas.drawCircle(cx.toFloat(), cy.toFloat(), radius, clockBackPaint)
    }


    private fun drawClockNumbersBackground(
        canvas: Canvas,
        width: Int,
        height: Int,
        a: Double,
        r: Float,
        rp: Float,
        array: IntArray
    ) {
        var i = 0
        while (i in array.indices) {
            val cx = (width / 2f + r * cos(i * a / array.size - a / 4)).toFloat()
            val cy = (height / 2f + r * sin(i * a / array.size - a / 4)).toFloat()
            canvas.drawCircle(cx, cy, rp, clockNumbersBackPaint)
            i++
        }
    }

    private fun drawClockNumbers(
        canvas: Canvas,
        width: Int,
        height: Int,
        a: Double,
        r: Float,
        rp: Float,
        array: IntArray
    ) {
        var i = 0
        while (i in array.indices) {
            var cx = (width / 2f + r * cos(i * a / array.size - a / 4)).toFloat()
            var cy = (height / 2f + r * sin(i * a / array.size - a / 4)).toFloat()
            drawTextCenter(canvas, clockTextPaint.apply {
                this.textSize = rp
                this.textAlign = Paint.Align.CENTER
            }, array[i].toString(), cx, cy)
            i++
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    setPosition(it.x, it.y, it)
                }
                MotionEvent.ACTION_MOVE -> {
                    setPosition(it.x, it.y, it)
                }
                MotionEvent.ACTION_UP -> {
                    setPosition(it.x, it.y, it)
                }
            }
        }
        return true
    }


    private fun setPosition(x: Float, y: Float, motionEvent: MotionEvent) {
        smoothMoveSelectorAnimation.cancel()
        selectorAngleWeight = calculateSelectorAngleWeightByTouch(
            x,
            y,
            getClockNumbersCount(),
            width / 2,
            height / 2
        ).toFloat()

        selectorRadiusWeight = calculateSelectorRadiusWeightByTouch(x, y, width / 2, height / 2)
        onTimeChangeListener?.onTimeChange(getTime(), clockMode, motionEvent)
        postInvalidate()
    }

    private fun calculateSelectorRadiusWeightByTouch(x: Float, y: Float, cx: Int, cy: Int): Float {
        return if (clockMode == Hour24)
            (if ((sqrt((y - cy).pow(2) + (x - cx).pow(2))) / clockRadius <= centerClockWeight * 1.1f) centerClockWeight else 1f)
        else 1f
    }

    private fun calculateSelectorAngleWeightByTouch(
        x: Float,
        y: Float,
        count: Int,
        cx: Int,
        cy: Int
    ): Float {
        if (cx.toFloat() == x && cy.toFloat() == y) {
            return 0f
        }
        val add = if (x - cx >= 0) 0.0 else Math.PI
        val step = 2 * Math.PI / count
        val bA =
            (acos((cy - y) / Math.sqrt(
                Math.pow(
                    (cx - x).toDouble(),
                    2.0
                ) + Math.pow((cy - y).toDouble(), 2.0)
            )
            ))
        val angle = if (add == 0.0) (bA) else (2 * add - bA);

        val index = ((angle % (step / 2)) / (step) + angle / step).roundToInt().run {
            if (this == count) {
                count - 1;
            } else
                this
        }
        return (index / count.toFloat()).also {
            Log.i("TAG023", "selectorAngleWeight: $it")
        }
    }


}

abstract class AnimCountDownTimer(duration: Long, interval: Long) :
    CountDownTimer(duration, interval) {
    var aw1: Float = 0f
        set(value) {
            field = circularNumber(value)
        }
    var aw2: Float = 0f
        set(value) {
            field = circularNumber(value)
        }

    var rw1: Float = 0f
    var rw2: Float = 0f

    fun circularNumber(value: Float): Float {
        return (value - value.toInt()).let {
            if (it < 0) it + 1 else it
        }
    }

    fun circularDifference(v1: Float, v2: Float) = (v2 - v1).let {
        if (kotlin.math.abs(it) > 0.5f) {
            (1 - kotlin.math.abs(it)) * -signOfNumber(it)
        } else it
    }

    fun signOfNumber(value: Float) =
        if (value > 0)
            1f
        else -1f
}


enum class ClockMode {
    Hour12, Hour24, Minute
}

interface OnTimeChangeListener {
    fun onTimeChange(time: Int, clockMode: ClockMode, motionEvent: MotionEvent)
}