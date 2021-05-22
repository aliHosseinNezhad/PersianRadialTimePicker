package com.arappmain.radialtimepicker

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.IntRange
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.arappmain.radialtimepicker.ClockMode.*
import com.arappmain.radialtimepicker.PageData.ClockAnalogDigitalMode.Analog
import com.arappmain.radialtimepicker.PageData.ClockAnalogDigitalMode.Digital
import com.arappmain.radialtimepicker.PageData.ClockArrow.Hour
import com.arappmain.radialtimepicker.PageData.ClockArrow.Minute
import com.arappmain.radialtimepicker.digitalTimePicker.DigitalTimePicker
import com.arappmain.radialtimepicker.digitalTimePicker.animUtils.AnimUtils
import com.arappmain.radialtimepicker.digitalTimePicker.animUtils.AnimateUtils
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeoutException
import kotlin.math.min


class RadialTimePickerInputData {
    var time: Int = 0
    var clockMode = Hour12
}

interface OnTimeResultListener {
    fun onResult(startHour: Int, startMinute: Int, endHour: Int, endMinute: Int)
    fun onCancel()
}

class PageData {
    var uiColorData = UiColorData()
    var viewsText = ViewsText()
    var changeLiveData = MutableLiveData(true)
    var pageState = PageState.START
    var start = PageStateModel()
    var end = PageStateModel()
    var digitalAnalogClockMode = Analog
    var analogClockPercent = 0.85f
    var digitalClockPercent = 0.9f

    fun getPage() = if (pageState == PageState.START) {
        start
    } else end

    fun notifyChange(b: Boolean) {
        changeLiveData.postValue(b)
    }

    enum class ClockAnalogDigitalMode {
        Analog, Digital
    }

    inner class PageStateModel() {
        var clockData = ClockModel()
            private set

        inner class ClockModel() {
            var time = Time()
                private set
            var timeCountMode = TimeCountMode.Mode12
            var clockArrow = ClockArrow.Hour

            val clockMode: ClockMode
                get()
                = when {
                    clockArrow == Minute -> ClockMode.Minute
                    timeCountMode == TimeCountMode.Mode12 -> Hour12
                    else -> Hour24
                }

            fun getTime(): Int {
                time.run {
                    return if (clockArrow == ClockArrow.Hour) {
                        when (timeCountMode) {
                            TimeCountMode.Mode24 -> {
                                hour
                            }
                            TimeCountMode.Mode12 -> {
                                get12Hour()
                            }
                        }
                    } else minute
                }

            }

            fun setMinute(minute: Int) {
                if (minute in 0..59) {
                    time.minute = minute
                } else throw TimeoutException("minute must be in 0..59")
            }

            fun setHour(hour: Int) {
                if (hour in 0..23) {
                    if (timeCountMode == TimeCountMode.Mode12) {
                        set12Hour(hour)
                    } else time.hour = hour
                } else throw TimeoutException("hour must be in 0..23")
            }

            fun setTimeWithRadialTimePicker(time: Int) {
                this.time.run {
                    if (clockArrow == Minute) {
                        setMinute(time)
                    } else {
                        setHour(time)
                    }
                }
            }

            fun getHour(): Int {
                return if (timeCountMode == TimeCountMode.Mode12)
                    get12Hour().let {
                        if (it == 0) 12
                        else it
                    }
                else time.hour.let {
                    if (it == 0) 24 else it
                }
            }

            fun getMinute(): Int {
                return time.minute
            }

            fun get12Hour(): Int {
                time.run {
                    return if (am) hour
                    else hour - 12
                }
            }

            fun set12Hour(@IntRange(from = 0, to = 11) hour: Int) {
                time.run {
                    if (am) this.hour = hour
                    else this.hour = hour + 12
                }
            }


            inner class Time {
                private fun changeHour(hour: Int) {
                    this.hour = hour
                    notifyChange()
                }

                private fun changeAm(am: Boolean) {
                    this.am = am
                    notifyChange()
                }

                private fun notifyChange() {
                    changeLiveData.postValue(false)
                }


                @IntRange(from = 0, to = 23)
                var hour: Int = 0
                    set(value) {
                        field = value
                        if (am != value <= 11) {
                            changeAm(value <= 11)
                        }
                    }

                @IntRange(from = 0, to = 59)
                var minute: Int = 0

                var am: Boolean = true
                    set(value) {
                        field = value
                        if (am) {
                            if (hour !in 0..11) {
                                changeHour(hour - 12)
                            }
                        } else {
                            if (hour !in 12..23) {
                                changeHour(hour + 12)
                            }
                        }
                    }
            }


        }
    }

    enum class TimeCountMode {
        Mode12, Mode24
    }

    enum class ClockArrow {
        Hour, Minute
    }

    enum class PageState {
        START, END
    }

    class UiColorData {
        fun rippleColorCreator(): ColorStateList {
            return ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_pressed),
                    intArrayOf(android.R.attr.state_focused),
                    intArrayOf(android.R.attr.state_activated),
                    intArrayOf(android.R.attr.state_selected)
                ),
                intArrayOf(
                    rippleColor ?: background,
                    secondaryColor,
                    secondaryColor,
                    rippleColor ?: background
                )
            )
        }

        var timeCardViewColor: Int = Color.WHITE
        var rippleColor: Int? = null
        var timeTextColor: Int = Color.rgb(100, 100, 100)
        var textColors: Int = Color.WHITE
        var secondaryColor: Int = Color.argb(250, 0, 180, 170)
            set(value) {
                field = value
                val A: Int = value shr 24 and 0xff
                val R: Int = value shr 16 and 0xff
                val G: Int = value shr 8 and 0xff
                val B: Int = value and 0xff
                transparentSecondary = Color.argb(60, R, G, B)
            }
        var stateTitleColor: Int = Color.WHITE
        var transparentSecondary = Color.argb(80, 0, 200, 180)
        var background: Int = Color.rgb(0, 218, 197)
        var radialTimePickerColors = RadialTimePickerColors()
    }

    class ViewsText {
        var startTime = "زمان شروع"
        var endTime = "زمان پایان"
        var amText = "قبل ظهر"
        var pmText = "بعد ظهر"
        var acceptText = "تایید"
        var minuteText = "دقیقه"
        var hourText = "ساعت"
    }
}

class RadialTimePickerColors {
    var selectorColor: Int = Color.rgb(0, 160, 170)
    var selectorTextColor: Int = Color.WHITE
    var textsColors: Int = Color.WHITE
    var clockBackColor: Int = Color.rgb(240, 240, 245)
    var clockNumberBackColor: Int = Color.rgb(180, 190, 200)
}


interface SetViewsText {
    fun onChangeViewsText(viewsText: PageData.ViewsText): PageData.ViewsText
}

class TimePickerBottomSheetFragment : BottomSheetDialogFragment() {

    private lateinit var digitalTimePicker: DigitalTimePicker

    //views
    private lateinit var clockAnalogDigitalModeChangeBtn: FrameLayout
    private lateinit var minuteTitleTextView: TextView
    private lateinit var hourTitleTextView: TextView
    private lateinit var acceptBtn: MaterialButton
    private lateinit var radioCardView: CardView
    private lateinit var bottomSheetCardBack: CardView
    private lateinit var pageStateTextView: TextView
    private lateinit var hourTextBtn: View
    private lateinit var minuteTextBtn: View
    private lateinit var minuteTextBackShadow: View
    private lateinit var hourTextBackShadow: View
    private lateinit var centerTimeText: TextView
    private lateinit var timeTextCardView: CardView
    private lateinit var radioAmPmBtn: RoundRadioGroup
    private lateinit var hourTextView: TextView
    private lateinit var minuteTextView: TextView
    private lateinit var clockCountModeBtn: FrameLayout
    private lateinit var bottomSheetBack: ConstraintLayout
    private var bottomSheetBehavior: BottomSheetBehavior<*>? = null
    private lateinit var radialTimePickerView: RadialTimePickerView
    private lateinit var pageStateChangeBtn: MaterialButton
    //end


    //data
    private var pagesData = PageData()
    private var typeface = MutableLiveData<Typeface>()


    //end
    private var onTimeResultListener: OnTimeResultListener? = null
    private var onTimeResultParam: ((successful: Boolean, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) -> Unit)? =
        null


    fun initTime(
        @IntRange(from = 0, to = 23) startHour: Int,
        @IntRange(from = 0, to = 59) startMinute: Int,
        @IntRange(from = 0, to = 23) endHour: Int,
        @IntRange(from = 0, to = 59) endMinute: Int
    ) {
        pagesData.start.clockData.time.hour = startHour
        pagesData.start.clockData.time.minute = startMinute
        pagesData.end.clockData.time.hour = endHour
        pagesData.end.clockData.time.minute = endMinute
    }

    fun setOnTimeResultListener(result: (successful: Boolean, startHour: Int, startMinute: Int, endHour: Int, endMinute: Int) -> Unit) {
        onTimeResultParam = result
    }

    @JvmName("setOnTimeResultListener1")
    fun setOnTimeResultListener(onTimeResultListener: OnTimeResultListener) {
        this.onTimeResultListener = onTimeResultListener
    }

    fun setClockArrowMode(timeArrow: PageData.ClockArrow) {
        pagesData.getPage().clockData.clockArrow = timeArrow
        pagesData.notifyChange(true)
    }

    fun set24Hour(is24Hour: Boolean) {
        if (is24Hour) {
            pagesData.start.clockData.timeCountMode = PageData.TimeCountMode.Mode24
            pagesData.end.clockData.timeCountMode = PageData.TimeCountMode.Mode24
        } else {
            pagesData.start.clockData.timeCountMode = PageData.TimeCountMode.Mode12
            pagesData.end.clockData.timeCountMode = PageData.TimeCountMode.Mode12
        }
        pagesData.notifyChange(true)
    }

    fun setTimeTextColors(color: Int) {
        pagesData.uiColorData.timeTextColor = color
        pagesData.notifyChange(true)
    }

    fun setTextsColors(color: Int) {
        pagesData.uiColorData.textColors = color
        pagesData.notifyChange(true)
    }

    fun setBackgroundColor(color: Int) {
        pagesData.uiColorData.background = color
        pagesData.notifyChange(true)
    }

    fun setTitleColor(color: Int) {
        pagesData.uiColorData.stateTitleColor = color
        pagesData.notifyChange(true)
    }

    fun setSecondaryColor(color: Int) {
        pagesData.uiColorData.secondaryColor = color
        pagesData.notifyChange(true)
    }

    fun setRadialTimePickerColors(radialTimePickerColors: RadialTimePickerColors) {
        pagesData.uiColorData.radialTimePickerColors=(radialTimePickerColors)
        pagesData.notifyChange(true)
    }

    fun setAnalogDigitalMode(mode: PageData.ClockAnalogDigitalMode) {
        pagesData.digitalAnalogClockMode = mode
        pagesData.notifyChange(true)
    }

    fun setTextTypeFace(typeface: Typeface?) {
        typeface?.let {
            this.typeface.postValue(typeface)
        }

    }

    fun setTimeCardViewColor(color: Int) {
        pagesData.uiColorData.timeCardViewColor = color
        pagesData.notifyChange(true)
    }

    fun setViewsText(param: (viewsText: PageData.ViewsText) -> PageData.ViewsText) {
        pagesData.viewsText = param(pagesData.viewsText)
        pagesData.notifyChange(true)
    }

    fun setViewsText(listener: SetViewsText) {
        pagesData.viewsText = listener.onChangeViewsText(pagesData.viewsText)
        pagesData.notifyChange(true)
    }

    fun setButtonsRippleColor(color: Int) {
        pagesData.uiColorData.rippleColor = color
        pagesData.notifyChange(true)
    }

    private fun setPageState(state: PageData.PageState) {
        pagesData.pageState = state
        pagesData.notifyChange(true)
    }

    private fun setAmPageData(b: Boolean) {
        pagesData.getPage().clockData.time.am = b
        pagesData.notifyChange(true)
    }

    //view updaters
    private fun getRadialTimePickerValue(): RadialTimePickerInputData {
        val radialTimePickerInputData = RadialTimePickerInputData()
        radialTimePickerInputData.clockMode = pagesData.getPage().clockData.clockMode
        radialTimePickerInputData.time = pagesData.getPage().clockData.getTime()
        return radialTimePickerInputData
    }

    private fun updateRadialTimePickerColors() {
        pagesData.uiColorData.radialTimePickerColors.let {
            radialTimePickerView.clockBackColor = it.clockBackColor
            radialTimePickerView.clockNumberBackColor = it.clockNumberBackColor
            radialTimePickerView.selectorColor = it.selectorColor
            radialTimePickerView.selectorTextColor = it.selectorTextColor
            radialTimePickerView.textsColors = it.textsColors
        }
    }

    private fun updateViewsByAnimation() {
        updateNonAnimationViews()
        getRadialTimePickerValue().let {
            radialTimePickerView.setTime(it.time, it.clockMode)
        }
    }

    private fun updateViewsWithoutAnimation() {
        updateNonAnimationViews()
    }


    var radioBtnVisibility = AnimUtils(300).apply {
        onStart { weight, toExpand ->
            if (toExpand) {
                radioCardView.alpha = 0f
                radioCardView.visibility = View.VISIBLE
            } else {
                radioCardView.alpha = 1f
                radioCardView.visibility = View.VISIBLE
            }
        }
        onRefresh {
            radioCardView.alpha = it
        }
        onEnd { weight, isExpand ->
            radioCardView.visibility = if (isExpand) View.VISIBLE else View.GONE
        }
    }
    var clockSwitchAnimation = AnimateUtils(1).apply {
        frame(0, 300) {
            setVisibilityWithWeight(radialTimePickerView, it, pagesData.analogClockPercent)

        }.onStart {

            setVisibilityWithWeight(radialTimePickerView, 1f, pagesData.analogClockPercent)
        }.onEnd {

            setVisibilityWithWeight(radialTimePickerView, 0f, pagesData.analogClockPercent)
        }.expand = false
        //--------------
        frame(300, 600) {
            setVisibilityWithWeight(digitalTimePicker, it, pagesData.digitalClockPercent)
        }.onStart {
            setVisibilityWithWeight(digitalTimePicker, 0f, pagesData.digitalClockPercent)
        }.onEnd {
            setVisibilityWithWeight(digitalTimePicker, 1f, pagesData.digitalClockPercent)
        }.expand = true
    }


    fun setVisibilityWithWeight(view: View, weight: Float, analogClockPercent: Float) {
        var layoutParams = (view.layoutParams as ConstraintLayout.LayoutParams)
        layoutParams.matchConstraintPercentWidth = weight * analogClockPercent
        view.alpha = weight
        view.layoutParams = layoutParams
        if (weight == 0f) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
        }
    }

    private fun updateNonAnimationViews() {
        updateViewsColor()
        updateTexts()
        if (digitalTimePicker.isAm != pagesData.getPage().clockData.time.am)
            digitalTimePicker.isAm = pagesData.getPage().clockData.time.am
        if (digitalTimePicker.hour != pagesData.getPage().clockData.getHour())
            digitalTimePicker.hour = pagesData.getPage().clockData.getHour()
        if (digitalTimePicker.minute != pagesData.getPage().clockData.getMinute())
            digitalTimePicker.minute = pagesData.getPage().clockData.getMinute()
        if (pagesData.digitalAnalogClockMode == Analog) {
            if (radialTimePickerView.visibility != View.VISIBLE) {
                clockSwitchAnimation.start(true)
            }
        } else {
            if (digitalTimePicker.visibility != View.VISIBLE) {
                clockSwitchAnimation.start()
            }
        }
        if (pagesData.getPage().clockData.timeCountMode == PageData.TimeCountMode.Mode24) {
//            (radioAmPmBtn.parent as View).visibility = View.GONE
            if (radioCardView.visibility == View.VISIBLE)
                radioBtnVisibility.hide()
            digitalTimePicker.is24Mode = true
            (clockCountModeBtn.getChildAt(0) as ImageView).setImageDrawable(context?.let {
                ContextCompat.getDrawable(
                    it, R.drawable.clock_24_count_mode
                )
            })
        } else {
            digitalTimePicker.is24Mode = false
//            (radioAmPmBtn.parent as View).visibility = View.VISIBLE
            if (radioCardView.visibility != View.VISIBLE)
                radioBtnVisibility.show()
            (clockCountModeBtn.getChildAt(0) as ImageView).setImageDrawable(context?.let {
                ContextCompat.getDrawable(
                    it, R.drawable.clock_12_count_mode
                )
            })
        }
        if (pagesData.digitalAnalogClockMode == Analog) {
            (clockAnalogDigitalModeChangeBtn.getChildAt(0) as ImageView).setImageDrawable(context?.let {
                ContextCompat.getDrawable(
                    it, R.drawable.analog_clock
                )
            })
        } else {
            (clockAnalogDigitalModeChangeBtn.getChildAt(0) as ImageView).setImageDrawable(context?.let {
                ContextCompat.getDrawable(
                    it, R.drawable.digital_clock
                )
            })
        }
        if (pagesData.getPage().clockData.clockArrow == Minute) {
            hourTextBackShadow.visibility = View.VISIBLE
            minuteTextBackShadow.visibility = View.INVISIBLE
        } else {
            hourTextBackShadow.visibility = View.INVISIBLE
            minuteTextBackShadow.visibility = View.VISIBLE

        }
        radioAmPmBtn.setState(if (pagesData.getPage().clockData.time.am) 0 else 1)
        var numberFormat = DecimalFormat("00")
        hourTextView.text = numberFormat.format(pagesData.getPage().clockData.getHour())
        minuteTextView.text = numberFormat.format(pagesData.getPage().clockData.getMinute())
    }

    private fun updateViewsColor() {
        updateRadialTimePickerColors()
        digitalTimePicker.background = pagesData.uiColorData.radialTimePickerColors.clockBackColor
        digitalTimePicker.secondColor = pagesData.uiColorData.timeTextColor


        acceptBtn.rippleColor = pagesData.uiColorData.rippleColorCreator()
        pageStateChangeBtn.rippleColor = pagesData.uiColorData.rippleColorCreator()
        pagesData.uiColorData.timeCardViewColor.let {
            timeTextCardView.setCardBackgroundColor(it)
        }
        bottomSheetCardBack.setCardBackgroundColor(pagesData.uiColorData.background)
        pagesData.uiColorData.secondaryColor.let {
            acceptBtn.setBackgroundColor(it)
            pageStateChangeBtn.setBackgroundColor(it)
            radioCardView.setCardBackgroundColor(it)
            minuteTextBackShadow.background =
                ColorDrawable(pagesData.uiColorData.transparentSecondary)
            hourTextBackShadow.background =
                ColorDrawable(pagesData.uiColorData.transparentSecondary)
        }
        pageStateTextView.setTextColor(pagesData.uiColorData.stateTitleColor)
        pagesData.uiColorData.textColors.let {
            acceptBtn.setTextColor(it)
            pageStateChangeBtn.setTextColor(it)
            radioAmPmBtn.setTextColors(it)
            (clockCountModeBtn.getChildAt(0) as ImageView).setColorFilter(it)
            (clockAnalogDigitalModeChangeBtn.getChildAt(0) as ImageView).setColorFilter(it)
        }
        pagesData.uiColorData.timeTextColor.let {
            hourTextView.setTextColor(it)
            minuteTextView.setTextColor(it)
            centerTimeText.setTextColor(it)
            hourTitleTextView.setTextColor(it)
            minuteTitleTextView.setTextColor(it)
        }


    }

    private fun updateTexts() {
        if (pagesData.pageState == PageData.PageState.START) {
            pageStateTextView.text = pagesData.viewsText.startTime
            (pageStateChangeBtn).text = pagesData.viewsText.endTime

        } else {
            pageStateTextView.text = pagesData.viewsText.endTime
            (pageStateChangeBtn).text = pagesData.viewsText.startTime
        }
        hourTitleTextView.text = pagesData.viewsText.hourText
        minuteTitleTextView.text = pagesData.viewsText.minuteText
        acceptBtn.text = pagesData.viewsText.acceptText
        radioAmPmBtn.setAmText(pagesData.viewsText.amText)
        digitalTimePicker.amText = pagesData.viewsText.amText
        digitalTimePicker.pmText = pagesData.viewsText.pmText
        radioAmPmBtn.setPmText(pagesData.viewsText.pmText)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }


    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        val bottomSheetDialog = dialog as BottomSheetDialog
        bottomSheetDialog.setContentView(R.layout.time_picker_bottom_sheet_layout)
        try {
            val behaviorField = bottomSheetDialog.javaClass.getDeclaredField("behavior");
            behaviorField.isAccessible = true;
            val behavior = behaviorField.get(bottomSheetDialog) as BottomSheetBehavior<*>
            bottomSheetBehavior = behavior
        } catch (e: NoSuchFieldException) {
            e.printStackTrace();
        } catch (e: IllegalAccessException) {
            e.printStackTrace();
        }
        super.setupDialog(dialog, style)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.time_picker_bottom_sheet_layout, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSheetBehavior?.state = BottomSheetBehavior.STATE_EXPANDED
        setViewsById(view)
        setViewsAction()
        setLiveDataObserves()
    }

    private fun setLiveDataObserves() {
        pagesData.changeLiveData.observe(viewLifecycleOwner) { animation ->
            if (animation)
                updateViewsByAnimation()
            else updateViewsWithoutAnimation()
        }
        typeface.observe(viewLifecycleOwner) {
            it?.let {
                radioAmPmBtn.setTypeFace(it)
                radialTimePickerView.textTypeface = it
                hourTextView.typeface = it
                minuteTextView.typeface = it
                pageStateChangeBtn.typeface = it
                pageStateTextView.typeface = it
                centerTimeText.typeface = it
                minuteTitleTextView.typeface = it
                hourTitleTextView.typeface = it
                digitalTimePicker.typeface = it
            }
        }

    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setViewsAction() {
        clockAnalogDigitalModeChangeBtn.setOnClickListener {
            if (pagesData.digitalAnalogClockMode == Analog)
                pagesData.digitalAnalogClockMode = Digital
            else
                pagesData.digitalAnalogClockMode = Analog
            pagesData.notifyChange(true)
        }

        pageStateChangeBtn.setOnClickListener {
            pagesData.let {
                if (it.pageState == PageData.PageState.START)
                    it.pageState = PageData.PageState.END
                else if (it.pageState == PageData.PageState.END)
                    it.pageState = PageData.PageState.START
                it.notifyChange(true)
            }
        }
        clockCountModeBtn.setOnClickListener {
            var set24 = pagesData.getPage().clockData.timeCountMode != PageData.TimeCountMode.Mode24
            set24Hour(set24)
        }
        digitalTimePicker.setOnTimeChangeListener { amPm, hour, minute ->
            pagesData.getPage().clockData.time.am = amPm
            pagesData.getPage().clockData.setHour(hour)
            pagesData.getPage().clockData.setMinute(minute)
            pagesData.notifyChange(false)
        }
        radialTimePickerView.setOnTimeChangeListener(object : OnTimeChangeListener {
            override fun onTimeChange(time: Int, clockMode: ClockMode, motionEvent: MotionEvent) {
                if (motionEvent.action != MotionEvent.ACTION_UP) {
                    pagesData.getPage().clockData.setTimeWithRadialTimePicker(time)
                    pagesData.changeLiveData.postValue(false)
                } else {
                    gotoMinuteSet()
                }

            }
        })
        radialTimePickerView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_UP -> {
                    bottomSheetBehavior?.let {
                        it.isDraggable = true
                        gotoMinuteSet()
                    }
                }
                else -> {
                    bottomSheetBehavior?.let {
                        it.isDraggable = false
                    }
                }
            }
            return@setOnTouchListener false
        }
        minuteTextBtn.setOnClickListener {
            setClockArrowMode(Minute)
        }
        hourTextBtn.setOnClickListener {
            setClockArrowMode(Hour)
        }
        radioAmPmBtn.setOnStateChangeListener {
            setAmPageData(it == RoundRadioGroup.AM)
        }
        acceptBtn.setOnClickListener {
            val startHour = pagesData.start.clockData.time.hour
            val startMinute = pagesData.start.clockData.time.minute

            val endHour = pagesData.end.clockData.time.hour
            val endMinute = pagesData.end.clockData.time.minute
            onTimeResultParam?.let {

                it(true, startHour, startMinute, endHour, endMinute)
            }
            onTimeResultListener?.onResult(startHour, startMinute, endHour, endMinute)
            dismiss()
        }
    }

    private fun gotoMinuteSet() {
        if (pagesData.getPage().clockData.clockArrow != Minute) {
            pagesData.getPage().clockData.clockArrow = Minute
            pagesData.changeLiveData.postValue(true)
        }
    }


    private fun setViewsById(view: View) {
        radialTimePickerView = view.findViewById<RadialTimePickerView>(R.id.radial_time_picker_2)
        bottomSheetBack = view.findViewById<ConstraintLayout>(R.id.bottom_sheet_back)
        pageStateChangeBtn = view.findViewById(R.id.page_state_change_btn)
        clockCountModeBtn = view.findViewById<FrameLayout>(R.id.clock_count_mode_btn)
        minuteTextView = view.findViewById<TextView>(R.id.minute_text)
        hourTextView = view.findViewById<TextView>(R.id.hour_text)
        radioAmPmBtn = view.findViewById<RoundRadioGroup>(R.id.pm_am_ratio_btn)
        centerTimeText = view.findViewById<TextView>(R.id.center_time_text)
        timeTextCardView = view.findViewById<CardView>(R.id.text_digital_time_card_view)
        hourTextBackShadow = view.findViewById<View>(R.id.hour_text_back_shadow)
        minuteTextBackShadow = view.findViewById<View>(R.id.minute_text_back_shadow)
        minuteTextBtn = view.findViewById<View>(R.id.minute_text_btn)
        hourTextBtn = view.findViewById<View>(R.id.hour_text_btn)
        pageStateTextView = view.findViewById<TextView>(R.id.page_state_text)
        acceptBtn = view.findViewById<MaterialButton>(R.id.accept_btn)
        bottomSheetCardBack = view.findViewById<CardView>(R.id.bottom_sheet_card_back)
        radioCardView = view.findViewById<CardView>(R.id.radio_card_view)
        hourTitleTextView = view.findViewById<TextView>(R.id.hour_title_text)
        minuteTitleTextView = view.findViewById<TextView>(R.id.minute_title_text)
        clockAnalogDigitalModeChangeBtn =
            view.findViewById<FrameLayout>(R.id.clock_analog_digit_mode_btn)
        digitalTimePicker =
            view.findViewById<DigitalTimePicker>(R.id.time_picker_digital_time_picker)
//        numberPicker.maxValue = 1
//        numberPicker.minValue = 0
//        numberPicker.value = 0
//        numberPicker.setFormatter(NumberPicker.Formatter {
//            val numberFormat = DecimalFormat("00")
//            var x = numberFormat.format(it)
//            return@Formatter if (it == 0) "am" else "pm"
//        })
        hourTextView.viewTreeObserver.addOnGlobalLayoutListener {
            var textSize = min(hourTextView.height, hourTextView.width) * 0.15f
//           Toast.makeText(context, "$textSize", Toast.LENGTH_SHORT).show()
            hourTextView.textSize = textSize
            minuteTextView.textSize = textSize
            centerTimeText.textSize = textSize

        }


    }


}