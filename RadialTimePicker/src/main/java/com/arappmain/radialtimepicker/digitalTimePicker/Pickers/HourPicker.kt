package com.arappmain.radialtimepicker.digitalTimePicker.Pickers

import android.content.Context
import android.util.AttributeSet

class HourPicker @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CustomNumberPicker(context, attrs, defStyleAttr) {

    var data = Data()
    fun timeFormatter(time: Int): String {
        return if (time / 10 == 0) {
            "0$time"
        } else time.toString()
    }

    class Data {
        var hour: Int = 0
            set(value) {
                field = value
            }
        var mode12 = Mode12()
        var mode24 = Mode24()
        var is24Hour = true
        fun mode(): Mode {
            return if (is24Hour) mode24 else mode12
        }


        class Mode12 : Mode() {
            override val max = 11
            override val min = 0
        }

        class Mode24 : Mode() {
            override val max = 23
            override val min = 0
        }

        open class Mode {
            open val max = 0
            open val min = 0
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

    fun setHour(hour: Int) {
        data.hour = hour
        initView()
    }

    init {
        setIs24(true)
    }

    fun initView() {
        this.minValue = data.mode().min
        this.maxValue = data.mode().max
        this.setAutoTextSize(true, maxTextLength)
        this.value = data.hour
        this.formatter = Formatter {
            return@Formatter timeFormatter(
                if (it == 0) {
                    if (data.is24Hour) 24 else 12
                } else it
            )
        }

    }
}