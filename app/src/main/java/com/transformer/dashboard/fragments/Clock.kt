package com.transformer.dashboard.fragments

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.transformer.dashboard.R
import java.text.SimpleDateFormat
import java.util.*

class Clock : Fragment(R.layout.fragment_clock) {
    private lateinit var clockHeaderTextView: TextView
    private lateinit var clockTimeTextView: TextView
    private lateinit var clockDateTextView: TextView

    private val headerColors = arrayOf(
        R.color.blue,
        R.color.green,
        R.color.orange,
        R.color.pink,
        R.color.purple,
        R.color.yellow
    )

    private var hour: Int = 0
    private var hour24: Int = 0
    private var minute: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        clockHeaderTextView = view.findViewById(R.id.clock_header_text)
        clockTimeTextView = view.findViewById(R.id.clock_time_text)
        clockDateTextView = view.findViewById(R.id.clock_date_text)
        setDateTime()
    }

    private fun setDateTime() {
        clockTimeTextView.text = getCurrentTime()
        clockDateTextView.text = getCurrentDate()
    }


    private fun getCurrentTime(): String {
        val oneToTwenty =
            arrayOf(
                "O'",
                "One",
                "Two",
                "Three",
                "Four",
                "Five",
                "Six",
                "Seven",
                "Eight",
                "Nine",
                "Ten",
                "Eleven",
                "Twelve",
                "Thirteen",
                "Fourteen",
                "Fifteen",
                "Sixteen",
                "Seventeen",
                "Eighteen",
                "Nineteen",
                "Twenty"
            )
        val tens = arrayOf("O'Clock", "Ten", "Twenty", "Thirty", "Forty", "Fifty")

        hour = SimpleDateFormat("h", Locale.ENGLISH).format(Date()).toInt()
        minute = SimpleDateFormat("m", Locale.ENGLISH).format(Date()).toInt()
        hour24 = SimpleDateFormat("H", Locale.ENGLISH).format(Date()).toInt()

        // hour string added
        var timeString = oneToTwenty[hour] + "\n"

        // minute string generation
        if (minute < 10) {
            timeString +=
                if (minute == 0) tens[0] // O'Clock
                else oneToTwenty[0] + oneToTwenty[minute] // O' + One/Two/Three
        } else if (minute < 20) {
            timeString += oneToTwenty[minute]
        } else {
            val minuteOnes = minute % 10
            val minuteTens = (minute - minuteOnes) / 10

            timeString += tens[minuteTens]

            if (minuteOnes != 0)
                timeString += "\n" + oneToTwenty[minuteOnes]
        }

        return timeString
    }

    private fun getCurrentDate(): String {
        return SimpleDateFormat("EEE, d MMM", Locale.ENGLISH).format(Date())
    }

    fun clockUpdater() {
        setDateTime()
        checkAndSetColors()
    }

    private fun checkAndSetColors() {
        if (hour24 < 7) {
            setScreenBrightness(2)
            val colorRed = resources.getColor(R.color.red)
            changeTextColor(clockHeaderTextView, colorRed)
            changeTextColor(clockTimeTextView, colorRed)
            changeTextColor(clockDateTextView, colorRed)
        } else {
            setScreenBrightness(10)
            changeTextColor(clockHeaderTextView, resources.getColor(headerColors[minute % 6]))
            changeTextColor(clockTimeTextView, Color.WHITE)
            changeTextColor(clockDateTextView, Color.WHITE)
        }
    }

    private fun changeTextColor(view: TextView, to: Int) {
        val colorFade: ObjectAnimator = ObjectAnimator.ofObject(
            view,
            "textColor",
            ArgbEvaluator(),
            view.currentTextColor,
            to
        )
        colorFade.duration = 5000
        colorFade.start()
    }


    private fun setScreenBrightness(level: Int) {
        val activity = activity
        if (activity != null) {
            val valueAnimator: ValueAnimator = ValueAnimator.ofFloat(
                activity.window.attributes.screenBrightness,
                level / 100.0f
            )
            valueAnimator.duration = 5000

            valueAnimator.addUpdateListener { va ->
                val lp = activity.window.attributes
                lp.screenBrightness = va.animatedValue as Float
                activity.window.attributes = lp
            }
            valueAnimator.start()
        }
    }
}