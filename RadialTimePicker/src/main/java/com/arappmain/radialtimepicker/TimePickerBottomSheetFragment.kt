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
import android.widget.NumberPicker
import android.widget.TextView
import androidx.annotation.IntRange
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import com.arappmain.radialtimepicker.ClockMode.*
import com.arappmain.radialtimepicker.PageData.ClockArrow.Hour
import com.arappmain.radialtimepicker.PageData.ClockArrow.Minute
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
    var digitalAnalogClockMode = ClockAnalogDigitalMode.Analog

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

            fun setTime(time: Int) {
                this.time.run {
                    if (clockArrow == Minute) {
                        if (time in 0..59) {
                            minute = time
                        } else throw TimeoutException("minute must be in 0..59")
                    } else {
                        if (time in 0..23) {
                            if (timeCountMode == TimeCountMode.Mode12) {
                                set12Hour(time)
                            } else hour = time
                        } else throw TimeoutException("hour must be in 0..23")
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
                    changeLiveData.postValue(true)
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
                rippleColor?:background,
                secondaryColor,
                secondaryColor,
                rippleColor?:background
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
    var radialTimePickerColors = MutableLiveData<RadialTimePickerColors>()

}

class RadialTimePickerColors {
    var selectorColor: Int = 0
    var selectorTextColor: Int = 0
    var textsColors: Int = 0
    var clockBackColor: Int = 0
    var clockNumberBackColor: Int = 0
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

interface SetViewsText {
    fun onChangeViewsText(viewsText: ViewsText): ViewsText
}

class TimePickerBottomSheetFragment : BottomSheetDialogFragment() {
    private lateinit var numberPicker: NumberPicker
    private lateinit var minuteTitleTextView: TextView
    private lateinit var hourTitleTextView: TextView

    //views
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
    private lateinit var digitalClockBtn: FrameLayout
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
        pagesData.uiColorData.radialTimePickerColors.postValue(radialTimePickerColors)
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

    fun setViewsText(param: (viewsText: ViewsText) -> ViewsText) {
        pagesData.viewsText = param(pagesData.viewsText)
        pagesData.notifyChange(true)
    }

    fun setViewsText(listener: SetViewsText) {
        pagesData.viewsText = listener.onChangeViewsText(pagesData.viewsText)
        pagesData.notifyChange(true)
    }


    //view updaters
    private fun getRadialTimePickerValue(): RadialTimePickerInputData {
        val radialTimePickerInputData = RadialTimePickerInputData()
        radialTimePickerInputData.clockMode = pagesData.getPage().clockData.clockMode
        radialTimePickerInputData.time = pagesData.getPage().clockData.getTime()
        return radialTimePickerInputData
    }

    private fun updateTimePickerColors(radialTimePickerColors: RadialTimePickerColors) {
        radialTimePickerColors.let {
            radialTimePickerView.clockBackColor = it.clockBackColor
            radialTimePickerView.clockNumberBackColor = it.clockNumberBackColor
            radialTimePickerView.selectorColor = it.selectorColor
            radialTimePickerView.selectorTextColor = it.selectorTextColor
            radialTimePickerColors.textsColors = it.textsColors
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

    private fun updateNonAnimationViews() {
        updateViewsColor()
        updateTexts()
        if (pagesData.getPage().clockData.timeCountMode == PageData.TimeCountMode.Mode24) {
            (radioAmPmBtn.parent as View).visibility = View.GONE
            (digitalClockBtn.getChildAt(0) as ImageView).setImageDrawable(context?.let {
                ContextCompat.getDrawable(
                    it, R.drawable.icon_hour_24
                )
            })
        } else {
            (radioAmPmBtn.parent as View).visibility = View.VISIBLE
            (digitalClockBtn.getChildAt(0) as ImageView).setImageDrawable(context?.let {
                ContextCompat.getDrawable(
                    it, R.drawable.icon_hour_12
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

    private fun setPageState(state: PageData.PageState) {
        pagesData.pageState = state
        pagesData.notifyChange(true)
    }

    private fun setAmPageData(b: Boolean) {
        pagesData.getPage().clockData.time.am = b
    }

    private fun updateViewsColor() {
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
        }
        pagesData.uiColorData.timeTextColor.let {
            hourTextView.setTextColor(it)
            minuteTextView.setTextColor(it)
            centerTimeText.setTextColor(it)
            minuteTextView.setTextColor(it)
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
        setViewsAction(view)
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
            }
        }
        pagesData.uiColorData.radialTimePickerColors.observe(viewLifecycleOwner) {
            updateTimePickerColors(it)
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    private fun setViewsAction(view: View) {
        pageStateChangeBtn.setOnClickListener {
            pagesData.let {
                if (it.pageState == PageData.PageState.START)
                    it.pageState = PageData.PageState.END
                else if (it.pageState == PageData.PageState.END)
                    it.pageState = PageData.PageState.START
                it.notifyChange(true)
            }
        }
        digitalClockBtn.setOnClickListener {
            var set24 = pagesData.getPage().clockData.timeCountMode != PageData.TimeCountMode.Mode24
            set24Hour(set24)
        }
        radialTimePickerView.setOnTimeChangeListener(object : OnTimeChangeListener {
            override fun onTimeChange(time: Int, clockMode: ClockMode, motionEvent: MotionEvent) {
                if (motionEvent.action != MotionEvent.ACTION_UP) {
                    pagesData.getPage().clockData.setTime(time)
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
        digitalClockBtn = view.findViewById<FrameLayout>(R.id.digit_clock_btn)
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