# PersianRadialTimePicker
[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](https://opensource.org/licenses/MIT)
[![platform](https://img.shields.io/badge/platform-android-brightgreen.svg)](https://developer.android.com/index.html)
[![API](https://img.shields.io/badge/API-19%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=19)
[![](https://jitpack.io/v/osameh15/PersianRadialTimePicker.svg)](https://jitpack.io/#osameh15/PersianRadialTimePicker)

This library offers a Radial time picker designed on [Google's Material Design Principals For Pickers](http://www.google.com/design/spec/components/pickers.html) for Android 4.4 (API 19) +.
Persian Radial Time Picker via Bottom sheet design. Developed by [@aliHosseinNezhad](https://github.com/aliHosseinNezhad).

<img src="resources/ezgif.com-gif-maker.gif" height="500" width="300">

12 Hour | 24 Hour
---- | ----
![12 Hour](https://github.com/osameh15/PersianRadialTimePicker/blob/main/resources/12hour.png) | ![24 Hour](https://github.com/osameh15/PersianRadialTimePicker/blob/main/resources/24hour.png)

You can report any issue on issues page. Note: If you speak Persian, you can submit issues with Persian (Farsi) language and I will check them. :)

## Contents

- [Installation](#installation)
- [How to use](#how-to-use)
- [Bugs and feedback](#bugs-and-feedback)
- [Credits](#credits)
- [License](#license)

## Installation

#### Setup
- Make sure you are using material components in your default AppTheme i.e your AppTheme inherits from a material theme
- In project-level build.gradle:
```groovy
  allprojects {
  	repositories {
          // ...
          maven { url 'https://jitpack.io' }
      }
  }
```
- In your app-level build.gradle file: 
```
implementation 'com.github.aliHosseinNezhad:radialTimePicker:+'
```
-- Or
```
    implementation 'com.github.osameh15:PersianRadialTimePicker:1.0.2'
```
## How to use

1. You can use Time Picker in activity or fragment
### Add to layout

    <com.arappmain.radialtimepicker.RadialTimePickerView
                    android:id="@+id/radial_time_picker_2"
                    android:layout_width="0dp"
                    android:layout_height="0dp"
                    app:clock_mode="hour24"
                    app:layout_constraintBottom_toBottomOf="parent"
                    app:layout_constraintDimensionRatio="1:1"
                    app:layout_constraintEnd_toEndOf="parent"
                    app:layout_constraintHorizontal_bias="0.507"
                    app:layout_constraintStart_toStartOf="parent"
                    app:layout_constraintTop_toTopOf="parent"
                    app:layout_constraintWidth_percent="0.85"
                    app:text_typeface="@font/dana_fanu_light" />
 
2. Bottom sheet dialog: Time Picer 
```
private var timePickerBottomSheetFragment = TimePickerBottomSheetFragment()
        timePickerBottomSheetFragment.let {
            it.setOnTimeResultListener { successful, startHour, startMinute, endHour, endMinute ->
                Toast.makeText(this,
                    "$startHour:$startMinute, $endHour:$endMinute",Toast.LENGTH_LONG).show()
            }
            it.show(supportFragmentManager,null)
        }
 ```
 
## Bugs and Feedback

For bugs, feature requests, and discussion please use [GitHub Issues](https://github.com/osameh15/PersianRadialTimePicker/issues)

## Credits

This library was inspired by this repos:

- https://github.com/wdullaer/MaterialDateTimePicker
- https://github.com/ashiqursuperfly/Android-Material-Ranged-Time-Picker-Dialog

## License
```
MIT License

Copyright (c) 2021 osameh irandoust

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
