package com.transformer.dashboard

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import java.text.SimpleDateFormat
import java.util.*


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class MainActivity : AppCompatActivity() {
    private lateinit var fullscreenContent: ConstraintLayout
    private lateinit var fullscreenContentControls: LinearLayout

    private lateinit var wifiManager: WifiManager
    private lateinit var wifiSwitch: SwitchCompat

    private val hideHandler = Handler()

    @SuppressLint("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        fullscreenContent.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
        fullscreenContentControls.visibility = View.VISIBLE
    }
    private var isFullscreen: Boolean = false

    private val hideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private val delayHideTouchListener = View.OnTouchListener { view, motionEvent ->
        when (motionEvent.action) {
            MotionEvent.ACTION_DOWN -> if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS)
            }
            MotionEvent.ACTION_UP -> view.performClick()
            else -> {
            }
        }
        false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fullscreen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Set up the user interaction to manually show or hide the system UI.
        isFullscreen = true
        fullscreenContent = findViewById(R.id.fullscreen_content)
        fullscreenContent.setOnClickListener { toggle() }
        fullscreenContentControls = findViewById(R.id.fullscreen_content_controls)
        fullscreenContentControls.setOnTouchListener(delayHideTouchListener)

        /** clock stuff */
        clockComponentView = findViewById(R.id.clock_component)
        clockHeaderTextView = findViewById(R.id.clock_header_text)
        clockTimeTextView = findViewById(R.id.clock_time_text)
        clockDateTextView = findViewById(R.id.clock_date_text)

        /** wifi stuff */
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        wifiSwitch = findViewById(R.id.wifi_switch)

        wifiSwitch.isChecked = wifiManager.isWifiEnabled

        wifiSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleWiFi(isChecked)
        }

    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    override fun onResume() {
        tickerRunning = true
        super.onResume()
        setDateTime()
        clockUpdater()
    }

    override fun onPause() {
        super.onPause()
        tickerRunning = false
    }


    private fun toggle() {
        if (isFullscreen) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        fullscreenContentControls.visibility = View.GONE
        isFullscreen = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    private fun show() {
        // Show the system bar
        fullscreenContent.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        isFullscreen = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
    }

    /** clock stuff begins */
    private lateinit var clockComponentView: View
    private lateinit var clockHeaderTextView: TextView
    private lateinit var clockTimeTextView: TextView
    private lateinit var clockDateTextView: TextView

    private val clockHandler = Handler()
    private lateinit var clockTicker: Runnable
    private var tickerRunning = true

    private val random = Random()

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
    private var seconds: Int = 0
    private var wifiCounter: Int = 0

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

    private fun clockUpdater() {
        clockTicker = Runnable {
            if (!tickerRunning) return@Runnable
            setDateTime()
            checkAndSetColors()
            checkWifiStatus()

            // every two minutes
            if (minute % 2 == 0) {
                moveClockAround(getRandomFloat(0.95f, 0.05f), getRandomFloat(0.95f, 0.05f))
            }

            clockHandler.postDelayed(clockTicker, 60000)
        }
        seconds = SimpleDateFormat("s", Locale.ENGLISH).format(Date()).toInt()
        val startTime = (60 - seconds) * 1000
        clockHandler.postDelayed(clockTicker, startTime.toLong())
    }

    private fun checkWifiStatus() {
        if (wifiManager.isWifiEnabled) {
            val wifiSSID = wifiManager.connectionInfo.ssid
            val networkId = wifiManager.connectionInfo.networkId
            when {
                networkId != -1 -> {
                    Log.i("checkWifiStatus", "Wifi Connected to $wifiSSID. Updating text")
                    wifiCounter = 0
                    wifiSwitch.text = getString(R.string.wifi_switch_connected, wifiSSID)
                }
                wifiCounter != 2 -> {
                    Log.i(
                        "checkWifiStatus",
                        "Wifi Disconnected. Waiting ${2 - wifiCounter} minute(s) before turning off wifi"
                    )
                    wifiCounter++
                    wifiSwitch.text = getString(R.string.wifi_switch)
                }
                else -> {
                    Log.i("checkWifiStatus", "Wifi Turned Off")
                    wifiCounter = 0
                    wifiSwitch.performClick()
                }
            }
        }
    }

    private fun checkAndSetColors() {
        if (hour24 < 7) {
            setScreenBrightness(-1)
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

    private fun getRandomFloat(upper: Float, lower: Float) = random.nextFloat() * (upper - lower) + lower

    private fun moveClockAround(x: Float, y: Float) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(fullscreenContent)
        constraintSet.setVerticalBias(R.id.clock_component, x)
        constraintSet.setHorizontalBias(R.id.clock_component, y)

        val transition = AutoTransition()
        transition.duration = 1500
        transition.interpolator = AccelerateDecelerateInterpolator()

        TransitionManager.beginDelayedTransition(fullscreenContent, transition)
        constraintSet.applyTo(fullscreenContent)
    }

    private fun setScreenBrightness(level: Int) {
        val valueAnimator: ValueAnimator = ValueAnimator.ofFloat(
            window.attributes.screenBrightness,
            level / 100.0f
        )
        valueAnimator.duration = 5000

        valueAnimator.addUpdateListener { valueAnimator ->
            val lp = window.attributes
            lp.screenBrightness = valueAnimator.animatedValue as Float
            window.attributes = lp
        }
        valueAnimator.start()
    }

    /** clock stuff ends */

    private fun toggleWiFi(status: Boolean) {
        wifiManager.isWifiEnabled = status
    }


    companion object {
        /**
         * Whether or not the system UI should be auto-hidden after
         * [AUTO_HIDE_DELAY_MILLIS] milliseconds.
         */
        private const val AUTO_HIDE = true

        /**
         * If [AUTO_HIDE] is set, the number of milliseconds to wait after
         * user interaction before hiding the system UI.
         */
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private const val UI_ANIMATION_DELAY = 300
    }
}