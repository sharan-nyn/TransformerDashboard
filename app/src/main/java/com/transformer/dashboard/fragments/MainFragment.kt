package com.transformer.dashboard.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.transition.AutoTransition
import android.transition.TransitionManager
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.fragment.app.Fragment
import com.transformer.dashboard.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * An example full-screen fragment that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class MainFragment : Fragment(R.layout.fragment_main) {
    private val hideHandler = Handler()
    private var clockFragment: ClockFragment = ClockFragment()
    private var statsFragment: StatsFragment = StatsFragment()

    private val tickerHandler = Handler()
    private lateinit var ticker: Runnable
    private var tickerRunning = true

    private var seconds = 0

    private val random = Random()

    @Suppress("InlinedApi")
    private val hidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        val flags =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        activity?.window?.decorView?.systemUiVisibility = flags
        (activity as? AppCompatActivity)?.supportActionBar?.hide()
    }
    private val showPart2Runnable = Runnable {
        // Delayed display of UI elements
        fullscreenContentControls?.visibility = View.VISIBLE
    }
    private var visible: Boolean = false
    private val hideRunnable = Runnable { hide() }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    @SuppressLint("ClickableViewAccessibility")
    private val delayHideTouchListener = View.OnTouchListener { _, _ ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    private var fullscreenContent: LinearLayout? = null
    private var fullscreenContentControls: View? = null

    private lateinit var clockConstraintLayout: ConstraintLayout
    private lateinit var statsConstraintLayout: ConstraintLayout

    private lateinit var wifiManager: WifiManager
    private lateinit var wifiSwitch: SwitchCompat
    private lateinit var statsSwitch: SwitchCompat


    private var wifiCounter: Int = 0

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        visible = true

        fullscreenContent = view.findViewById(R.id.fullscreen_content)
        fullscreenContentControls = view.findViewById(R.id.fullscreen_content_controls)
        // Set up the user interaction to manually show or hide the system UI.
        fullscreenContent?.setOnClickListener { toggle() }
        fullscreenContentControls?.setOnTouchListener(delayHideTouchListener)

        clockConstraintLayout = view.findViewById(R.id.clock_constraintLayout)
        statsConstraintLayout = view.findViewById(R.id.stats_constraintLayout)
        wifiSwitch = view.findViewById(R.id.wifi_switch)
        statsSwitch = view.findViewById(R.id.stats_switch)

        // inflate clock fragment
        fragmentManager?.beginTransaction()?.replace(R.id.clock_fragment_content, clockFragment)
            ?.commit()

        // inflate stats fragment
        fragmentManager?.beginTransaction()?.replace(R.id.stats_fragment_content, statsFragment)
            ?.commit()

        wifiManager =
            requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        wifiSwitch.isChecked = wifiManager.isWifiEnabled
        statsSwitch.isEnabled = true

        wifiSwitch.setOnCheckedChangeListener { _, isChecked ->
            toggleWiFi(isChecked)
            if (!isChecked) {
                toggleStats(false);
            }
        }

        statsSwitch.setOnCheckedChangeListener {_, isChecked ->
            toggleStats(isChecked)
        }

    }

    private fun toggleWiFi(status: Boolean) {
        wifiManager.isWifiEnabled = status
        if (status)  {
            wifiSwitch.isChecked = true
        } else {
            wifiSwitch.isChecked = false
            toggleStats(false);
            statsSwitch.isEnabled = false
            wifiSwitch.text = getString(R.string.wifi_switch)
        }
    }

    private fun toggleStats(status: Boolean) {
        if (statsSwitch.isEnabled) {
            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT,
                1.0f
            )

            if (status) {
                clockConstraintLayout.layoutParams = layoutParams
                statsSwitch.isChecked = true
            } else {
                layoutParams.weight = 0f
                clockConstraintLayout.layoutParams = layoutParams
                statsSwitch.isChecked = false
            }
        }

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
                    wifiSwitch.isChecked = true
                    statsSwitch.isEnabled = true
                }
                wifiCounter != 2 -> {
                    Log.i(
                        "checkWifiStatus",
                        "Wifi Disconnected. Waiting ${2 - wifiCounter} minute(s) before turning off wifi"
                    )
                    wifiCounter++
                    wifiSwitch.text = getString(R.string.wifi_switch)
                    toggleStats(false);
                    statsSwitch.isEnabled = false
                }
                else -> {
                    Log.i("checkWifiStatus", "Wifi Turned Off")
                    wifiCounter = 0
                    wifiSwitch.performClick()
                }
            }
        } else {
            toggleWiFi(false);
        }
    }


    private fun startTicker() {
        ticker = Runnable {
            if (!tickerRunning) return@Runnable
            tickerHandler.postDelayed(ticker, 60000)
            clockUpdater()
            if (statsSwitch.isChecked) statsUpdater()
            checkWifiStatus()

        }
        seconds = SimpleDateFormat("s", Locale.ENGLISH).format(Date()).toInt()
        val startTime = (60 - seconds) * 1000
        tickerHandler.postDelayed(ticker, startTime.toLong())
    }

    private fun clockUpdater() {
        clockFragment.clockUpdater()
        moveContentAround(
            getRandomFloat(0.95f, 0.05f),
            getRandomFloat(0.95f, 0.05f),
            R.id.clock_fragment_content,
            clockConstraintLayout
        )
    }


    private fun statsUpdater() {
        statsFragment.getStatsData()
        moveContentAround(
            getRandomFloat(0.95f, 0.05f),
            getRandomFloat(0.95f, 0.05f),
            R.id.stats_fragment_content,
            statsConstraintLayout
        )
    }


    private fun moveContentAround(x: Float, y: Float, component: Int, layout: ConstraintLayout) {
        val constraintSet = ConstraintSet()
        constraintSet.clone(layout)
        constraintSet.setVerticalBias(component, x)
        constraintSet.setHorizontalBias(component, y)

        val transition = AutoTransition()
        transition.duration = 1500
        transition.interpolator = AccelerateDecelerateInterpolator()

        TransitionManager.beginDelayedTransition(layout, transition)
        constraintSet.applyTo(layout)
    }

    override fun onResume() {
        super.onResume()
        tickerRunning = true
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
        startTicker()
        clockFragment.clockUpdater()
    }


    override fun onPause() {
        super.onPause()
        tickerRunning = false;
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Clear the systemUiVisibility flag
        activity?.window?.decorView?.systemUiVisibility = 0
        show()
    }

    override fun onDestroy() {
        super.onDestroy()
        fullscreenContent = null
        fullscreenContentControls = null
    }

    private fun toggle() {
        if (visible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        fullscreenContentControls?.visibility = View.GONE
        visible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        hideHandler.removeCallbacks(showPart2Runnable)
        hideHandler.postDelayed(hidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    @Suppress("InlinedApi")
    private fun show() {
        // Show the system bar
        fullscreenContent?.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        visible = true

        // Schedule a runnable to display UI elements after a delay
        hideHandler.removeCallbacks(hidePart2Runnable)
        hideHandler.postDelayed(showPart2Runnable, UI_ANIMATION_DELAY.toLong())
        (activity as? AppCompatActivity)?.supportActionBar?.show()
    }

    private fun getRandomFloat(upper: Float, lower: Float) = random.nextFloat() * (upper - lower) + lower

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        hideHandler.removeCallbacks(hideRunnable)
        hideHandler.postDelayed(hideRunnable, delayMillis.toLong())
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