package com.arappmain.radialtimepicker.digitalTimePicker.animUtils

import android.os.CountDownTimer
import android.util.Log
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class AnimateUtils(var interval: Long) {
    private var isAnimating: Boolean = false
    private var timeLine = TimeLine(1, interval)
    private var frames = ArrayList<Frame>()
    private var minTime = 0L
    private var maxTime = 0L
    private var onStartParam: (() -> Unit)? = null
    private var onEndParam: (() -> Unit)? = null
    var revert: Boolean = false
    fun start(revert: Boolean = false) {
        if (!isAnimating) {
            isAnimating = true
            this.revert = revert
            timeLine.stop()
            timeLine = TimeLine(maxTime, interval)
            frames.forEach {
                it.lastDisplayed = false
            }
            timeLine.start()
            if (revert) onEndParam?.let { it() } else
                onStartParam?.let { it() }
            timeLine.onRefresh {
                refresh(
                    if (revert) maxTime - it else it,
                    if (revert) it == 0L else it == maxTime
                )
            }
            timeLine.onEnd { end(if (revert) maxTime - it else it) }
        }
    }

    fun frame(from: Long, to: Long, param: (Float) -> Unit): Data {
        val frame = Frame(param)
        val data = Data(frame, from, to)
        frames.add(frame)
        if (frame.sTime < minTime) {
            minTime = frame.sTime
        }
        if (frame.eTime > maxTime) {
            maxTime = frame.eTime
        }
        return data
    }

    fun onStart(param: () -> Unit) {
        onStartParam = param
    }

    fun onEnd(param: () -> Unit) {
        onEndParam = param
    }


    private fun end(time: Long) {
        frames.forEach {
            if (it.lastDisplayed)
                if (revert) it.start() else it.end()
            it.lastDisplayed = false
        }
        if (revert) onStartParam?.let { it() } else
            onEndParam?.let { it() }
        isAnimating = false
    }

    private fun refresh(time: Long, last: Boolean) {
        frames.forEach {
            if (time in it.sTime..it.eTime) {
                if (!it.lastDisplayed) {
                    if (revert) it.end() else it.start()
                }
                it.lastDisplayed = true
                it.refresh(calculate(time, it.sTime, it.eTime, it.sWeight, it.eWeight, it.expand))
            } else if (if (revert) it.sTime > time else it.eTime < time) {
                if (it.lastDisplayed)
                    if (revert) it.start() else it.end()
                it.lastDisplayed = false
            }
        }

    }


    fun calculate(
        time: Long,
        sTime: Long,
        eTime: Long,
        sWeight: Float,
        eWeight: Float,
        expand: Boolean
    ): Float {
        val t = (time - sTime) / (eTime - sTime).toFloat()
        return (if (expand) {
            sin(t * PI / 2)
        } else {
            cos(t * PI / 2)
        }).toFloat()
    }

    class Data(
        private val frame: Frame,
        //startTime
        val sTime: Long,
        //endTime
        val eTime: Long
    ) {
        // startWeight
        private var sWeight: Float = 0f

        //endWeight
        private var eWeight: Float = 1f
        var expand: Boolean = true
            set(value) {
                field = value
                frame.expand = value
            }

        init {
            frame.sTime = sTime
            frame.eTime = eTime
            frame.sWeight = sWeight
            frame.eWeight = eWeight
        }

        fun out(sWeight: Float, eWeight: Float): Data {
            this.sWeight = sWeight
            this.eWeight = eWeight
            frame.sWeight = sWeight
            frame.eWeight = eWeight
            return this
        }

        fun onStart(param: () -> Unit): Data {
            frame.onStartParam = param
            return this
        }

        fun onEnd(param: () -> Unit): Data {
            frame.onEndParam = param
            return this
        }
    }

    class Frame(private var param: ((Float) -> Unit)) {
        var expand: Boolean = true
        var lastDisplayed = false
        var sTime: Long = 0
        var eTime: Long = 0
        var sWeight: Float = 0f
        var eWeight: Float = 0f
        var onStartParam: (() -> Unit)? = null
        var onEndParam: (() -> Unit)? = null
        fun start() {
            onStartParam?.let { it() }
        }

        fun end() {
            onEndParam?.let { it() }
        }

        fun refresh(it: Float) {
            param(it)
        }

    }

}
class TimeLine(val duration: Long, val interval: Long) {
    var countDownTimer = Timer(duration, interval)
    var currentTime = 0L
    var lastTime = 0L
    var finishParam: ((Long) -> Unit)? = null
    var onTickParam: ((Long) -> Unit)? = null
    fun start() {
        currentTime = 0L
        lastTime = 0L
        countDownTimer.cancel()
        countDownTimer = Timer(duration, interval)
        countDownTimer.start()

    }

    fun stop() {
        countDownTimer.cancel()
    }

    fun resume(revert: Boolean = false) {
        countDownTimer.cancel()
        lastTime += currentTime
        currentTime = 0
        countDownTimer = Timer(duration - lastTime, interval)
        countDownTimer.start()
    }

    fun onEnd(param: (Long) -> Unit) {
        finishParam = param
    }

    fun onRefresh(param: (Long) -> Unit) {
        onTickParam = param
    }

    inner class Timer(val duration: Long, val interval: Long) :
        CountDownTimer(duration, interval) {
        override fun onTick(millisUntilFinished: Long) {
            currentTime = duration - millisUntilFinished
            synchronized(this@TimeLine){
                onTickParam?.let {
                    it(currentTime + lastTime)
                }
            }
            Log.i("TAG011", "onTick: ${currentTime + lastTime}")
        }

        override fun onFinish() {
            onTickParam?.let {
                it(currentTime + lastTime)
            }
            finishParam?.let {
                it(currentTime + lastTime)
            }
        }

    }
}