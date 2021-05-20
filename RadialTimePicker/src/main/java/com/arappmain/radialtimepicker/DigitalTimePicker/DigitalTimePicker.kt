package com.arappmain.radialtimepicker.DigitalTimePicker

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.MATCH_PARENT
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.constraintlayout.widget.Guideline
import com.arappmain.radialtimepicker.AnimCountDownTimer
import com.arappmain.radialtimepicker.DigitalTimePicker.Pickers.AmPmPicker
import com.arappmain.radialtimepicker.DigitalTimePicker.Pickers.CustomNumberPicker
import com.arappmain.radialtimepicker.DigitalTimePicker.Pickers.HourPicker
import com.arappmain.radialtimepicker.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class DigitalTimePicker @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {



    private lateinit var leftDivider: View
    private lateinit var rightDivider: View
    private var content: View = LayoutInflater.from(context).inflate(
        R.layout.digital_time_picker,
        this,
        false
    ).also {
        var layoutParams = (it.layoutParams as LayoutParams).apply {
            topToTop = PARENT_ID
            bottomToBottom = PARENT_ID
            startToStart = PARENT_ID
            endToEnd = PARENT_ID
            width = MATCH_PARENT
            height = MATCH_PARENT
        }
        it.layoutParams = layoutParams
    }


    private lateinit var amPmPicker: AmPmPicker
    private lateinit var hourPicker: HourPicker
    private lateinit var minutePicker: CustomNumberPicker
    private lateinit var cardView: CardView
    private lateinit var leftGuideLine: Guideline
    private val leftGuidelinePercent = 0.25f

    //animation
    private val interval: Long = 2L
    private val duration: Long = 300L
    private var weight: Float = 1f
    private val hideAnimation = HideAnimation()
    private val showAnimation = ShowAnimation()


    private var transparency = 100
    private var textColor: Int = 0
    private var dividerColor = 0
    private  var selectedTextColor = Color.rgb(100, 100, 100)
        set(value){
            field = value
            initViews()
        }
    fun setInitSecondaryColorWith(primaryColor:Int){
        val A: Int = primaryColor shr 24 and 0xff
        val R: Int = primaryColor shr 16 and 0xff
        val G: Int = primaryColor shr 8 and 0xff
        val B: Int = primaryColor and 0xff
        dividerColor = Color.argb((transparency*0.3f).toInt(), R, G, B)
        textColor = Color.argb(transparency, R, G, B)
    }
    var background = Color.rgb(253, 252, 250)
        set(value) {
            field = value
            initViews()
        }
    var typeface: Typeface? = null
        set(value) {
            field = value
            initViews()
        }
    var is24Mode: Boolean = true
        set(value) {
            field = value
            if (!value) {
                hideAnimation.cancel()
                showAnimation.start()
            } else {
                hideAnimation.start()
                showAnimation.cancel()
            }
            initViews()
        }

    var isAm = true
        set(value) {
            field = value
            initViews()
        }
    var hour = 0
        set(value) {
            field = value
            initViews()
        }

    var amText = "صبح"
        set(value) {
            field = value
            initViews()
        }
    var pmText = "عصر"
        set(value) {
            field = value
            initViews()
        }

    var edgeFadingStrength: Float = 0f
        set(value) {
            field = value
            initViews()
        }

    fun timeFormatter(time: Int): String {
        return if (time / 10 == 0) {
            "0$time"
        } else time.toString()
    }


    init {
        addView(content)
        setViewsById()
        preInit()
        initViews()
    }

    private fun preInit() {
        if (is24Mode) {
            updateAnimation(0f)
        } else updateAnimation(1f)
    }

    private fun initViews() {
        setInitSecondaryColorWith(selectedTextColor)
        leftDivider.background = ColorDrawable(dividerColor)
        rightDivider.background = ColorDrawable(dividerColor)
        cardView.setCardBackgroundColor(background)
        initMinutePicker()
        initHourPicker()
        initAmPmPicker()
    }

    private fun initAmPmPicker() {
        typeface?.let {
            amPmPicker.typeface = it
        }
        amPmPicker.fadingEdgeStrength = this.edgeFadingStrength
        amPmPicker.isAm = isAm
        amPmPicker.textColor = textColor
        amPmPicker.selectedTextColor = selectedTextColor
        amPmPicker.amText = amText
        amPmPicker.pmText = pmText
    }

    private fun initHourPicker() {
        typeface?.let {
            hourPicker.typeface = it
        }
        hourPicker.fadingEdgeStrength = this.edgeFadingStrength
        hourPicker.setIs24(is24Mode)
        hourPicker.setHour(hour)
        hourPicker.textColor = textColor
        hourPicker.selectedTextColor = selectedTextColor

    }

    private fun initMinutePicker() {
        typeface?.let {
            minutePicker.typeface = it
        }
        minutePicker.fadingEdgeStrength = this.edgeFadingStrength
        minutePicker.minValue = 0
        minutePicker.maxValue = 59
        minutePicker.value = 1
        minutePicker.textColor = textColor
        minutePicker.selectedTextColor = selectedTextColor
        minutePicker.formatter = CustomNumberPicker.Formatter {
            return@Formatter timeFormatter(it)
        }
    }

    private fun setViewsById() {
        minutePicker = content.findViewById(R.id.digital_time_picker_minute_picker)
        hourPicker = content.findViewById(R.id.digital_time_picker_hour_picker)
        amPmPicker = content.findViewById(R.id.digital_time_picker_am_pm_picker)
        cardView = content.findViewById(R.id.digital_time_picker_card_view)
        leftGuideLine = content.findViewById(R.id.digital_time_picker_left_guideline)
        leftDivider = content.findViewById(R.id.digital_time_picker_left_divider)
        rightDivider = content.findViewById(R.id.digital_time_picker_right_divider)
    }

    fun updateAnimation(weight: Float) {
        var layoutParams = (leftGuideLine.layoutParams as LayoutParams)
        layoutParams.guidePercent = leftGuidelinePercent * weight
        leftGuideLine.layoutParams = layoutParams
        leftDivider.alpha = weight
        amPmPicker.alpha = weight
    }

    inner class HideAnimation : AnimCountDownTimer(duration, interval) {
        var tm = 0L
        var t = 0f
        var weight = 1f
        override fun onTick(millisUntilFinished: Long) {
            tm = duration - millisUntilFinished
            t = tm / duration.toFloat()
            weight = cos(t * PI / 2).toFloat()
            updateAnimation(weight)
        }

        override fun onFinish() {
            weight = 0f
            updateAnimation(weight)
        }
    }

    inner class ShowAnimation : AnimCountDownTimer(duration, interval) {
        var tm = 0L
        var t = 0f
        var weight = 0f
        override fun onTick(millisUntilFinished: Long) {
            tm = duration - millisUntilFinished
            t = tm / duration.toFloat()
            weight = sin(t * PI / 2).toFloat()
            updateAnimation(weight)
        }

        override fun onFinish() {
            weight = 1f
            updateAnimation(weight)
        }
    }


}