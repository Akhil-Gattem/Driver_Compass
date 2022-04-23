package com.example.compass

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), SensorEventListener {
    lateinit var sensorManager: SensorManager
    lateinit var accelerometer: Sensor
    lateinit var magnetometer: Sensor

    var currentDegree = 0.0f
    var lastAccelerometer = FloatArray(3)
    var lastMagnetometer = FloatArray(3)
    var lastAccelerometerSet = false
    var lastMagnetometerSet = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
    }

    override fun onResume() {
        super.onResume()

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this, accelerometer)
        sensorManager.unregisterListener(this, magnetometer)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor === accelerometer) {
            lowPass(event.values, lastAccelerometer)
            lastAccelerometerSet = true
        } else if (event.sensor === magnetometer) {
            lowPass(event.values, lastMagnetometer)
            lastMagnetometerSet = true
        }

        if (lastAccelerometerSet && lastMagnetometerSet) {
            val r = FloatArray(9)
            if (SensorManager.getRotationMatrix(r, null, lastAccelerometer, lastMagnetometer)) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(r, orientation)
                val degree = (Math.toDegrees(orientation[0].toDouble()) + 360).toFloat() % 360
                customView.rotateAngle = -degree - 45
                currentDegree = -degree
                val d = degree.toInt()
                lbid_degree_heading.text = d.toString()+"°"
                if (degree<22 || degree>337) {
                    compass_text_view.text = "N"
                }
                else if (degree < 337 && degree > 292) {
                    compass_text_view.text = "NW"
                }
                else if (degree < 292 && degree > 248) {
                    compass_text_view.text = "W"
                }
               else if (degree < 248 && degree > 203) {
                    compass_text_view.text = "SW"
                }
               else if (degree < 203 && degree > 157) {
                    compass_text_view.text = "S"
                }
               else if (degree < 157 && degree > 112) {
                    compass_text_view.text = "SE"
                }
               else if (degree < 112 && degree > 75) {
                    compass_text_view.text = "E"
                }
                else if (degree < 75 && degree > 23) {
                    compass_text_view.text = "NE"
                }
                }
            }
        }
        private fun lowPass(input: FloatArray, output: FloatArray) {
            val alpha = 0.05f

            for (i in input.indices) {
                output[i] = output[i] + alpha * (input[i] - output[i])
            }
        }
    }
