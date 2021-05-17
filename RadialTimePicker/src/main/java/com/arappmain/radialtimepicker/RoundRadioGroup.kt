package com.arappmain.radialtimepicker

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat

class RoundRadioGroup : ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet?, defStyleAttr: Int)
            : super(context, attributeSet, defStyleAttr)

    private lateinit var pmTextView: TextView
    private lateinit var amTextView: TextView
    private lateinit var pmBtn: View
    private lateinit var amBtn: View
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    lateinit var baseView: View
    lateinit var pmView: ConstraintLayout
    lateinit var amView: ConstraintLayout

    companion object {
        const val PM = 1
        const val AM = 0
    }

    enum class ThemColorState {
        Dark, Light
    }

    fun setTypeFace(typeface: Typeface) {
        pmTextView.typeface = typeface
        amTextView.typeface = typeface
    }

    private var stateChangeListener: StateChangeListener? = null
    private var stateChangeParam: ((state: Int) -> Unit)? = null

    interface StateChangeListener {
        fun onChangeState(state: Int)
    }

    fun setOnStateChangeListener(stateChangeListener: StateChangeListener) {
        this.stateChangeListener = stateChangeListener
    }

    fun setOnStateChangeListener(listener: (state: Int) -> Unit) {
        stateChangeParam = listener
    }

    fun setThemeColorState(state: ThemColorState) {
        if (state == ThemColorState.Light) {
            selectedDrawable =
                (ContextCompat.getDrawable(context, R.drawable.light_selected_radio_btn))
            amBtn.background =
                (ContextCompat.getDrawable(context, R.drawable.light_radio_back_ripple))
            pmBtn.background =
                (ContextCompat.getDrawable(context, R.drawable.light_radio_back_ripple))
        } else if (state == ThemColorState.Dark) {
            selectedDrawable =
                (ContextCompat.getDrawable(context, R.drawable.dark_selected_radio_btn))
            amBtn.background =
                (ContextCompat.getDrawable(context, R.drawable.dark_radio_back_ripple))
            pmBtn.background =
                (ContextCompat.getDrawable(context, R.drawable.dark_radio_back_ripple))
        }
        refreshState()
    }

    fun setTextColors(color: Int) {
        amTextView.setTextColor(color)
        pmTextView.setTextColor(color)
    }

    var transparentDrawable = ColorDrawable(Color.TRANSPARENT)
    var selectedDrawable: Drawable? = null
    var backRippleDrawable: Drawable? = null


    private var state = 1
    fun setState(state: Int) {
        if (state == 0) {
            this.state = state
            amView.background = selectedDrawable
            pmView.background = transparentDrawable

        } else if (state == 1) {
            this.state = state
            amView.background = transparentDrawable
            pmView.background = selectedDrawable
        }
    }

    private fun refreshState() {
        setState(state)
    }

    init {
        initViews()
        addListeners()
        onViewCreated()
    }

    private fun onViewCreated() {
        setThemeColorState(ThemColorState.Dark)
        setState(AM)
    }

    private fun initViews() {
        baseView = inflater.inflate(R.layout.radio_view_layout, null)
        pmView = baseView.findViewById(R.id.pm_radio_btn_custom)
        amView = baseView.findViewById(R.id.am_radio_btn_custom)
        amBtn = baseView.findViewById(R.id.btn_view_am)
        pmBtn = baseView.findViewById(R.id.btn_view_pm)
        amTextView = baseView.findViewById<TextView>(R.id.am_text)
        pmTextView = baseView.findViewById<TextView>(R.id.pm_text)
        addView(baseView.also {
            it.layoutParams = LayoutParams(0, 0).also {
                it.dimensionRatio = "1:2"
                it.rightToRight = ConstraintSet.PARENT_ID
                it.leftToLeft = ConstraintSet.PARENT_ID
                it.topToTop = ConstraintSet.PARENT_ID
                it.bottomToBottom = ConstraintSet.PARENT_ID
            }
        })
        setButtonsBack()


    }

    private fun setButtonsBack() {


    }

    private fun addListeners() {
        amBtn.setOnClickListener {
            setState(0)
            callListeners(0)
        }

        pmBtn.setOnClickListener {
            setState(1)
            callListeners(1)
        }
    }

    private fun callListeners(i: Int) {
        stateChangeParam?.let {
            it(i)
        }
        stateChangeListener?.onChangeState(i)
    }


}