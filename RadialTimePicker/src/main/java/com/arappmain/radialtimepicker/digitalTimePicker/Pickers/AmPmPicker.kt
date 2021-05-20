package com.arappmain.radialtimepicker.digitalTimePicker.Pickers

import android.content.Context
import android.util.AttributeSet

class AmPmPicker @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CustomNumberPicker(context, attrs, defStyleAttr) {
    private val min = 0
    private val max = 1

    private var maxTextLength = 2
    var isAm: Boolean = true
        set(value) {
            field = value
//            this.value  = if (value) min else max
            initView()
        }
        get() = (value == min)

    var amText = "قبل از ظهر"
        set(value) {
            field = value
            if (value.length > maxTextLength) maxTextLength = value.length
            initView()
        }

    var pmText = "بعد از ظهر"
        set(value) {
            field = value
            if (value.length > maxTextLength) maxTextLength = value.length
            initView()
        }


    fun initView() {
        this.minValue = min
        this.maxValue = max
        this.smoothScrollToPosition(if (isAm) min else max)
        this.setAutoTextSize(true,maxTextLength)
        this.formatter = Formatter {
            return@Formatter if (it == 0) amText else pmText
        }
    }


}