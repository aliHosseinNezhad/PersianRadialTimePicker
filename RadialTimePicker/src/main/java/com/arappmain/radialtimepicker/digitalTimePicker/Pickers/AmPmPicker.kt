package com.arappmain.radialtimepicker.digitalTimePicker.Pickers

import android.content.Context
import android.util.AttributeSet

class AmPmPicker @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CustomNumberPicker(context, attrs, defStyleAttr) {
    private val min = 0
    private val max = 1
    init {
        initView()
        this.formatter = Formatter {
            return@Formatter if (it == 0) amText else pmText
        }
    }

    private var maxTextLength = 2
    var isAm: Boolean = true
        set(value) {
            field = value
            initView()
        }

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
        this.value = if (isAm) min else max
        this.setAutoTextSize(true,maxTextLength)
    }



}