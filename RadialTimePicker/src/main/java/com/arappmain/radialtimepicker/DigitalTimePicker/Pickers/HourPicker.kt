package com.arappmain.radialtimepicker.DigitalTimePicker.Pickers

import android.content.Context
import android.util.AttributeSet

class HourPicker @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CustomNumberPicker(context, attrs, defStyleAttr) {

    private val min = 0
    private val max = 1
    var data = Data()
    fun timeFormatter(time:Int): String {
        return if (time/10 == 0){
            "0$time"
        } else time.toString()
    }
    class Data {
        var mode12 = Mode12()
        var mode24 = Mode24()
        var is24Hour = true
        fun mode(): Mode {
            return if (is24Hour) mode24 else mode12
        }

        fun setHour(hour: Int) {
            if (hour in mode().max..mode().max){
                mode().value = hour
            }
        }

        class Mode12 : Mode() {
            override val max = 12
            override val min = 1
        }

        class Mode24 : Mode() {
            override val max = 24
            override val min = 1
        }

        open class Mode {
            open val max = 0
            open val min = 0
            var value:Int = 0
        }
    }

    private val maxTextLength = 2

    fun is24(): Boolean {
        return data.is24Hour
    }
    fun setIs24(is24: Boolean) {
        data.is24Hour = is24
        initView()
    }
    fun setHour(hour:Int){
        data.setHour(hour)
        initView()
    }


    fun initView() {
        this.minValue = data.mode().min
        this.maxValue = data.mode().max
        this.setAutoTextSize(true, maxTextLength)
        this.formatter = Formatter {
            return@Formatter timeFormatter(it)
        }
    }
}