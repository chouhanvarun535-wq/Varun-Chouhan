package com.example.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

/**
 * Real hardware step sensor manager.
 * Uses hardware step counter or detector, with an accelerometer-based step peak fallback.
 */
class StepSensorManager(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private var stepCounterSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    private var stepDetectorSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
    private var accelerometerSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var initialStepCount = -1f
    private var onStepCallback: ((Int) -> Unit)? = null

    // Accelerometer peak detection variables
    private var lastAccelMagnitude = 0f
    private var lastStepTimeMs = 0L

    var isListening: Boolean = false
        private set

    fun startListening(onStep: (Int) -> Unit) {
        if (isListening || sensorManager == null) return
        this.onStepCallback = onStep
        isListening = true

        var registered = false
        if (stepDetectorSensor != null) {
            registered = sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_UI)
        }
        if (!registered && stepCounterSensor != null) {
            registered = sensorManager.registerListener(this, stepCounterSensor, SensorManager.SENSOR_DELAY_UI)
        }
        if (!registered && accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        if (!isListening || sensorManager == null) return
        sensorManager.unregisterListener(this)
        isListening = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        when (event.sensor.type) {
            Sensor.TYPE_STEP_DETECTOR -> {
                onStepCallback?.invoke(1)
            }
            Sensor.TYPE_STEP_COUNTER -> {
                val totalSteps = event.values[0]
                if (initialStepCount < 0) {
                    initialStepCount = totalSteps
                } else {
                    val delta = (totalSteps - initialStepCount).toInt()
                    if (delta > 0) {
                        onStepCallback?.invoke(delta)
                        initialStepCount = totalSteps
                    }
                }
            }
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
                val now = System.currentTimeMillis()
                if (magnitude > 11.5f && lastAccelMagnitude <= 11.5f && (now - lastStepTimeMs) > 320) {
                    lastStepTimeMs = now
                    onStepCallback?.invoke(1)
                }
                lastAccelMagnitude = magnitude
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
