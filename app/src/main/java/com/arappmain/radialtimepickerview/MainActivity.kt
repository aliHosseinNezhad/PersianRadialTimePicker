package com.arappmain.radialtimepickerview

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import com.arappmain.radialtimepicker.digitalTimePicker.DigitalTimePicker
import com.arappmain.radialtimepicker.RadialTimePickerColors
import com.arappmain.radialtimepicker.TimePickerBottomSheetFragment
import java.util.*

class MainActivity : AppCompatActivity() {
    private var timePickerBottomSheetFragment = TimePickerBottomSheetFragment()
    private lateinit var digitalTimePicker: DigitalTimePicker
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        digitalTimePicker = findViewById<DigitalTimePicker>(R.id.material_number_picker)
    }

    fun onClick(view: View)
    {
//        digitalTimePicker.is24Mode = !digitalTimePicker.is24Mode


        timePickerBottomSheetFragment.let {
            darkMode(it)
            it.setButtonsRippleColor(Color.rgb(190, 20, 170))
            it.setTextTypeFace(ResourcesCompat.getFont(this, R.font.dana_fanu_light))
            it.setOnTimeResultListener { successful, startHour, startMinute, endHour, endMinute ->
                Toast.makeText(
                    this,
                    "You Complete This activity and $successful \n Open Time: $startHour:$startMinute \n Close Time: $endHour:$endMinute",
                    Toast.LENGTH_LONG
                ).show()
            }
            it.show(supportFragmentManager, null)
        }
    }


    fun darkMode(it: TimePickerBottomSheetFragment) {
        var textColors = Color.rgb(220, 220, 230)
        var secondaryColor = Color.rgb(90, 90, 90)

        it.setTimeCardViewColor(Color.rgb(50, 50, 50))
        it.setBackgroundColor(Color.rgb(50, 50, 50))
        it.setSecondaryColor(Color.rgb(90, 90, 90))
        it.setRadialTimePickerColors(RadialTimePickerColors().also {
            it.textsColors = textColors
            it.clockNumberBackColor = Color.rgb(60, 60, 60)
            it.selectorColor = secondaryColor
            it.selectorTextColor = textColors
            it.clockBackColor = Color.rgb(60, 60, 60)
        })
        it.setTitleColor(textColors)
        it.setTextsColors(textColors)
        it.setTimeTextColors(textColors)
    }

    fun englishMode(it: TimePickerBottomSheetFragment) {
        it.setViewsText {
            it.amText = "am"
            it.pmText = "pm"
            it.acceptText = "ok"
            it.hourText = "hour"
            it.minuteText = "minute"
            it.startTime = "start time"
            it.endTime = "end time"
            return@setViewsText it
        }
    }


}