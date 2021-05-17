package com.arappmain.radialtimepickerview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.arappmain.radialtimepicker.TimePickerBottomSheetFragment

class MainActivity : AppCompatActivity()
{
    private var timePickerBottomSheetFragment = TimePickerBottomSheetFragment()
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onClick(view: View)
    {
        timePickerBottomSheetFragment.let {
            it.setOnTimeResultListener { successful, startHour, startMinute, endHour, endMinute ->
                Toast.makeText(this,
                    "You Complete This activity and $successful \n Open Time: $startHour:$startMinute \n Close Time: $endHour:$endMinute",
                    Toast.LENGTH_LONG).show()
            }
            it.show(supportFragmentManager,null)
        }
    }
}