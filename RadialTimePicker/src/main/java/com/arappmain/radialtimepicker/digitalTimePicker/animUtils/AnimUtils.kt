package com.arappmain.radialtimepicker.digitalTimePicker.animUtils

import android.content.Context
import android.os.CountDownTimer
import android.view.animation.AnimationUtils
import kotlin.coroutines.coroutineContext
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class AnimUtils(var duration: Long, var interval: Long = 15L) {
    private var onRefresh: ((weight: Float) -> Unit)? = null
    private var onEnd: ((weight: Float,isExpanded:Boolean) -> Unit)? = null
    private var onStart: ((weight: Float,toExpand:Boolean) -> Unit)? = null
    private var expand: Boolean = false
    private var tm: Long = 0

    var t: Float = 0f
    private var countDownTimer = object : CountDownTimer(duration, interval) {
        override fun onTick(millisUntilFinished: Long) {
            tm = duration - millisUntilFinished
            t = tm / duration.toFloat()
            onRefresh?.let {
                it(animFun(t))
            }
        }

        override fun onFinish() {
            t = 1f
            onRefresh?.let {
                it(animFun(t))
            }
            onEnd?.let { it(animFun(t),expand) }
        }
    }


    private fun animFun(t: Float): Float {
        return (if (expand) {
            sin(t * PI / 2)
        } else {
            cos(t * PI / 2)
        }).toFloat()
    }


    private fun start(expand: Boolean = true) {
        onStart?.let { it(if (expand) 0f else 1f,expand) }
        this.expand = expand
        countDownTimer.start()
    }
    fun show(){
        start()
    }
    fun hide(){
        start(false)
    }

    fun cancel() {
        countDownTimer.cancel()
    }

    fun onRefresh(param: (weight: Float) -> Unit) {
        this.onRefresh = param

    }

    fun onEnd(param:(weight:Float,isExpand:Boolean)->Unit){
        this.onEnd = param
    }

    fun onStart(param:(weight:Float,toExpand:Boolean)->Unit){
        this.onStart = param
    }


}