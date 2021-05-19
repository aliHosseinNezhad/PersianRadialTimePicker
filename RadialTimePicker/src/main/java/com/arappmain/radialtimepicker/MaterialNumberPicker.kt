package com.arappmain.radialtimepicker

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.InputType
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.EditText
import android.widget.NumberPicker
import android.widget.Scroller
import androidx.core.content.res.ResourcesCompat
import kotlin.math.min

class MaterialNumberPicker : NumberPicker {
    companion object {
        private const val DEFAULT_VALUE = 1
        private const val MAX_VALUE = 4
        private const val MIN_VALUE = 0
        private const val DEFAULT_SEPARATOR_COLOR = Color.TRANSPARENT
        private const val DEFAULT_TEXT_COLOR = Color.BLACK
        private const val DEFAULT_TEXT_SIZE = 40
        private const val DEFAULT_TEXT_STYLE = Typeface.NORMAL
        private const val DEFAULT_EDITABLE = false
        private const val DEFAULT_WRAPPED = false
    }
    init {

    }

    var x = arrayListOf(
        "بعد ظهر",
        "قبل از ظهر",
        "sdlk",
        "fslkdjfl",
        "sldkfjslkd"
    )

    var separatorColor: Int = Color.TRANSPARENT
        set(value) {
            field = value
            divider?.colorFilter = PorterDuffColorFilter(separatorColor, PorterDuff.Mode.SRC_IN)
        }
    var textColors: Int = DEFAULT_TEXT_COLOR
        set(value) {
            field = value
            updateTextAttributes()
        }


    var textStyle: Int = DEFAULT_TEXT_STYLE
        set(value) {
            field = value
            updateTextAttributes()
        }

    var typeface: Typeface? = null
        set(value) {
            field = value
            updateTextAttributes()
        }

    var textSize: Int = DEFAULT_TEXT_SIZE
        set(value) {
            field = value
            updateTextAttributes()
        }
    var editable: Boolean = DEFAULT_EDITABLE
        set(value) {
            field = value
            descendantFocusability =
                if (value) ViewGroup.FOCUS_AFTER_DESCENDANTS else ViewGroup.FOCUS_BLOCK_DESCENDANTS
        }
    private val inputEditText: EditText? by lazy {
        try {
            val f = NumberPicker::class.java.getDeclaredField("mInputText")
            f.isAccessible = true
            f.get(this) as EditText
        } catch (e: Exception) {
            null
        }
    }
    private val wheelPaint: Paint? by lazy {
        try {
            val selectorWheelPaintField =
                NumberPicker::class.java.getDeclaredField("mSelectorWheelPaint")
            selectorWheelPaintField.isAccessible = true
            selectorWheelPaintField.get(this) as Paint
        } catch (e: Exception) {
            null
        }
    }
    private val divider: Drawable? by lazy {
        val dividerField =
            NumberPicker::class.java.declaredFields.firstOrNull { it.name == "mSelectionDivider" }
        dividerField?.let {
            try {
                it.isAccessible = true
                it.get(this) as Drawable
            } catch (e: Exception) {
                null
            }
        }
    }

    @JvmOverloads
    constructor(
        context: Context,
        minValue: Int = DEFAULT_VALUE,
        maxValue: Int = MAX_VALUE,
        value: Int = DEFAULT_VALUE,
        separatorColor: Int = DEFAULT_SEPARATOR_COLOR,
        textColor: Int = DEFAULT_TEXT_COLOR,
        textSize: Int = DEFAULT_TEXT_SIZE,
        textStyle: Int = DEFAULT_TEXT_STYLE,
        editable: Boolean = DEFAULT_EDITABLE,
        wrapped: Boolean = DEFAULT_WRAPPED,
        typeface: Typeface? = null,
        formatter: Formatter? = null
    ) : super(context) {
        this.minValue = minValue
        this.maxValue = maxValue
        this.value = value
        this.separatorColor = separatorColor
        this.textColors = textColor
        this.textSize = textSize
        this.textStyle = textStyle
        this.typeface = typeface
        this.editable = editable
        this.wrapSelectorWheel = wrapped
        setFormatter(formatter)

        disableFocusability()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        val a = context.theme.obtainStyledAttributes(attrs, R.styleable.MaterialNumberPicker, 0, 0)

        minValue = a.getInteger(R.styleable.MaterialNumberPicker_mnpMinValue, DEFAULT_VALUE)
        maxValue = a.getInteger(R.styleable.MaterialNumberPicker_mnpMaxValue, MAX_VALUE)
        value = a.getInteger(R.styleable.MaterialNumberPicker_mnpValue, DEFAULT_VALUE)
        separatorColor =
            a.getColor(R.styleable.MaterialNumberPicker_mnpSeparatorColor, DEFAULT_SEPARATOR_COLOR)
        textColors = a.getColor(R.styleable.MaterialNumberPicker_mnpTextColor, DEFAULT_TEXT_COLOR)
        textSize =
            a.getDimensionPixelSize(R.styleable.MaterialNumberPicker_mnpTextSize, DEFAULT_TEXT_SIZE)
        textStyle = a.getInt(R.styleable.MaterialNumberPicker_mnpTextColor, DEFAULT_TEXT_STYLE)
        editable = a.getBoolean(R.styleable.MaterialNumberPicker_mnpEditable, DEFAULT_EDITABLE)
        wrapSelectorWheel =
            a.getBoolean(R.styleable.MaterialNumberPicker_mnpWrapped, DEFAULT_WRAPPED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            typeface = a.getFont(R.styleable.MaterialNumberPicker_mnp_typeface)
        } else {
            val x = a.getResourceId(
                R.styleable.RadialTimePickerView_text_typeface, 0
            )
            if (x != 0) {
                typeface = ResourcesCompat.getFont(
                    context,
                    x
                )
            }
        }

        a.recycle()

        disableFocusability()
    }

    /**
     * Disable focusability of edit text embedded inside the number picker
     * We also override the edit text filter private attribute by using reflection as the formatter is still buggy while attempting to display the default value
     * This is still an open Google @see <a href="https://code.google.com/p/android/issues/detail?id=35482#c9">issue</a> from 2012
     */
    private fun disableFocusability() {
        inputEditText?.filters = arrayOfNulls(0)
    }

    /**
     * Uses reflection to access text size private attribute for both wheel and edit text inside the number picker.
     */
    private fun updateTextAttributes() {
        wheelPaint?.let { paint ->
            paint.color = textColors
            paint.textSize = textSize.toFloat()
            paint.typeface = typeface?.also { Typeface.NORMAL }
            (0 until childCount)
                .map { getChildAt(it) as? EditText }
                .firstOrNull()
                ?.let {
                    it.setTextColor(textColors)
                    it.setTextSize(
                        TypedValue.COMPLEX_UNIT_SP,
                        pixelsToSp(context, textSize.toFloat())
                    )
                    it.inputType =
                        InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
                    it.typeface = typeface
                    invalidate()
                }
        }
    }

    private fun pixelsToSp(context: Context, px: Float): Float =
        px / context.resources.displayMetrics.scaledDensity
}


