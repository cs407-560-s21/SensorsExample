package com.example.sensorsexample

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), SensorEventListener {

    private val TAG = "MainActivity"

    var myMediaPlayer : MediaPlayer? = null

    var currentThreshold = 0

    private lateinit var sensorManager: SensorManager
    private var mLight: Sensor? = null
    private var mAccelerometer: Sensor? = null
    private var mGyroscope: Sensor? = null
    private var mMagneticField: Sensor? = null
    private var mProximity: Sensor? = null

    private var mGravity: Sensor? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        // Accelerometer Sensor
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        registerSensor(mAccelerometer)

        // Gyroscope Sensor
        mGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        registerSensor(mGyroscope)

        // MagneticField Sensor
        mMagneticField = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        registerSensor(mMagneticField)

        // Proximity Sensor
        mProximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
        registerSensor(mProximity)

        // Light Sensor
        mLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
        registerSensor(mLight)


        // Gravity Sensor
        mGravity = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
        registerSensor(mLight)

        initializeSeekBar()
    }

    private fun initializeSeekBar() {

        val seekBar = findViewById<SeekBar>(R.id.seekBar)
        currentThreshold = seekBar.max
        Log.d(TAG, "currentThreshold:$currentThreshold ")

        // Set a SeekBar change listener
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, value: Int, b: Boolean) {
                // Display the current progress of SeekBar
                tv_threshold.text = "$value"
                currentThreshold = value
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                // Nothing to do
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                // Nothing to do

            }
        })

    }


    // A helper function to register sensor with a delay, this can be done onResume instead of onCreate
    private fun registerSensor(sensor: Sensor?){
        if (sensor != null){
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }


    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do something here if sensor accuracy changes.
    }

    override fun onSensorChanged(event: SensorEvent?) {

        // Many sensors return 3 values, one for each axis.

        val sensor = event?.sensor ?: return
        when (sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {

                // Axis of the Accelerometer sample, not normalized yet.
                val x: Float = event.values[0]
                val y: Float = event.values[1]
                val z: Float = event.values[2]

                Log.d(TAG, "Accelerometer: ${event.values.joinToString(" ")}")

                tv_accelerometer.text = "X: $x" + ", Y: $y" + ", Z: $z"

                shakeDetection(x, y, z)

                //if(toggle && (Math.abs(prevY - gravity[1]) > threshold) && !ignore){

                //}


                // To measure the real acceleration of the device, the contribution of the force of
                // gravity must be removed from the accelerometer data.
                // This can be achieved by applying a high-pass filter.
                // Conversely, a low-pass filter can be used to isolate the force of gravity.

                //val alpha: Float = 0.8f

                // Isolate the force of gravity with the low-pass filter.
                //gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
                //gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
                //gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

                // Remove the gravity contribution with the high-pass filter.
                //linear_acceleration[0] = event.values[0] - gravity[0]
                //linear_acceleration[1] = event.values[1] - gravity[1]
                //linear_acceleration[2] = event.values[2] - gravity[2]
            }
            Sensor.TYPE_GYROSCOPE -> {


                // Axis of the rotation sample, not normalized yet.
                val x: Float = event.values[0]
                val y: Float = event.values[1]
                val z: Float = event.values[2]

                Log.d(TAG, event.values.joinToString(" "))

                tv_gyroscope.text = "X: $x" + ", Y: $y" + ", Z: $z"

            }

            Sensor.TYPE_MAGNETIC_FIELD -> {

                // Axis of the magnetic filed, not normalized yet.
                val x: Float = event.values[0]
                val y: Float = event.values[1]
                val z: Float = event.values[2]

                Log.d(TAG, event.values.joinToString(" "))

                tv_magnetic_field.text = "X: $x" + ", Y: $y" + ", Z: $z"
            }
            Sensor.TYPE_PROXIMITY -> {
                //lets you determine how far away an object is from a device

                Log.d(TAG, event.values.joinToString(" "))

                tv_proximity.text = "${event.values[0]}"

            }
            Sensor.TYPE_LIGHT -> {

                // The light sensor returns a single value.
                val lux = event.values[0]
                // Do something with this sensor value.
                Log.d(TAG, "lux: $lux")
                tv_light.text = "$lux"
            }

        }
    }

    private fun shakeDetection(x: Float, y: Float, z: Float) {

        val acc = Math.sqrt( (x * x + y * y + z * z).toDouble() )
        Log.d(TAG, "acc:$acc ")

        if (acc > currentThreshold) {
            Log.d(TAG, "x: " + x + " y: " + y + " z: " + z)
            Log.d("TAG", "acceleration: $acc, currentSensitivity: $currentThreshold")
            playSound()
        }
    }


    

    override fun onResume() {
        super.onResume()
        /* mLight?.also { light ->
             sensorManager.registerListener(this, light, SensorManager.SENSOR_DELAY_NORMAL)
         }*/

    }


    override fun onPause() {
        super.onPause()
        stopSong()
        sensorManager.unregisterListener(this)
    }

    /*
       A button for Starting sound.
       If it is the first time, it creates the MediaPlayer object with the song provided
   */
    private fun playSound() {
        if (myMediaPlayer == null) {
            myMediaPlayer = MediaPlayer.create(this, R.raw.whip_crack)
        }
        myMediaPlayer?.start()
    }



    /*
        Helper function to handle stopping the sound
    */
    private fun stopSong(){
        if(myMediaPlayer != null){
            //myMediaPlayer?.stop()
            // Release is probably better option to release the system resources used
            myMediaPlayer?.release()
            myMediaPlayer = null
        }
    }


}
