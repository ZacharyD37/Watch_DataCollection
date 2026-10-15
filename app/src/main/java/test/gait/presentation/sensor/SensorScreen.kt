package test.gait.presentation.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import kotlinx.coroutines.delay
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

data class SensorReading(
    val x: Float,
    val y: Float,
    val z: Float,
    val timestamp: Long  // Unix timestamp in milliseconds
)

@Composable
fun SensorScreen(
    userId: String,
    gender: String,
    age: String,
    timerDuration: Int,
    dominantHand: String,
    trailNumber: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val sensorManager = remember {
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    // Vibrator setup
    val vibrator = remember {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    var accelData by remember { mutableStateOf("Loading...") }
    var gyroData by remember { mutableStateOf("Loading...") }
    var isRecording by remember { mutableStateOf(false) }
    var currentSamplingRate by remember { mutableStateOf(40000) } // Default to 25Hz (40000 microseconds)
    var exportStatus by remember { mutableStateOf("") }
    var hasExported by remember { mutableStateOf(false) }

    // Timer states
    var isTimerRunning by remember { mutableStateOf(false) }
    var isCountdownActive by remember { mutableStateOf(false) }
    var countdownTime by remember { mutableStateOf(3) }
    var remainingTime by remember { mutableStateOf(timerDuration) }
    var timerFinished by remember { mutableStateOf(false) }
    var recordingStartTime by remember { mutableStateOf(0L) }

    // Data storage with millisecond-precision timestamps
    val accelReadings = remember { mutableListOf<SensorReading>() }
    val gyroReadings = remember { mutableListOf<SensorReading>() }
    var totalReadings by remember { mutableStateOf(0) }

    // Sensor listener state
    var currentListener by remember { mutableStateOf<SensorEventListener?>(null) }

    // Sampling rate options
    val samplingRates = listOf(
        Pair("Fastest", SensorManager.SENSOR_DELAY_FASTEST),
        Pair("50Hz", SensorManager.SENSOR_DELAY_GAME),
        Pair("25Hz", 40000)
    )
    var selectedRateIndex by remember { mutableStateOf(2) } // Changed from 0 to 2 to default to 25Hz

    // Attempt to align registration with a whole wall-clock second.
    LaunchedEffect(isCountdownActive) {
        if (isCountdownActive) {
            countdownTime = 3

            // Countdown phase
            while (countdownTime > 0 && isCountdownActive) {
                delay(1000)
                countdownTime--
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(100)
                }
            }

            if (countdownTime == 0 && isCountdownActive) {
                // Calculate precise delay to start at next full second
                val preStartTime = System.currentTimeMillis()
                val delayMillis = 1000 - (preStartTime % 1000)

                // Wait for the precise moment
                delay(delayMillis)

                // Coroutine scheduling may start after the requested boundary.
                val preciseStartTime = System.currentTimeMillis()
                recordingStartTime = preciseStartTime

                // Create and register sensor listener RIGHT NOW
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        val currentMillis = System.currentTimeMillis()

                        // Only record if we're in recording mode
                        if (!isRecording) return

                        when (event.sensor.type) {
                            Sensor.TYPE_ACCELEROMETER -> {
                                accelData = "Accel: x=%.2f y=%.2f z=%.2f".format(
                                    event.values[0], event.values[1], event.values[2]
                                )

                                accelReadings.add(
                                    SensorReading(
                                        x = event.values[0],
                                        y = event.values[1],
                                        z = event.values[2],
                                        timestamp = currentMillis
                                    )
                                )
                            }
                            Sensor.TYPE_GYROSCOPE -> {
                                gyroData = "Gyro: x=%.2f y=%.2f z=%.2f".format(
                                    event.values[0], event.values[1], event.values[2]
                                )

                                gyroReadings.add(
                                    SensorReading(
                                        x = event.values[0],
                                        y = event.values[1],
                                        z = event.values[2],
                                        timestamp = currentMillis
                                    )
                                )
                            }
                        }

                        totalReadings = accelReadings.size + gyroReadings.size
                    }

                    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
                }

                // Register listener at precise moment
                currentListener = listener
                val accelRegistered = sensorManager.registerListener(listener, accelerometer, currentSamplingRate)
                val gyroRegistered = sensorManager.registerListener(listener, gyroscope, currentSamplingRate)
                if (!accelRegistered || !gyroRegistered) {
                    sensorManager.unregisterListener(listener)
                    currentListener = null
                    isCountdownActive = false
                    exportStatus = "❌ Could not register both sensors. Recording cancelled."
                    return@LaunchedEffect
                }

                // Set flags
                isCountdownActive = false
                isTimerRunning = true
                isRecording = true

                // Success vibration
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(500)
                }
            }
        }
    }

    // Main timer effect
    LaunchedEffect(isTimerRunning) {
        if (isTimerRunning) {
            while (remainingTime > 0 && isTimerRunning) {
                delay(1000)
                remainingTime--
            }

            if (remainingTime == 0 && isTimerRunning) {
                timerFinished = true
                isTimerRunning = false
                isRecording = false

                // Unregister sensors immediately
                currentListener?.let { listener ->
                    sensorManager.unregisterListener(listener)
                    currentListener = null
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    val vibrationEffect = VibrationEffect.createWaveform(
                        longArrayOf(0, 1000, 200, 300, 200, 300, 200, 300),
                        intArrayOf(0, 255, 0, 255, 0, 255, 0, 255),
                        -1
                    )
                    vibrator.vibrate(vibrationEffect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(longArrayOf(0, 1000, 200, 300, 200, 300, 200, 300), -1)
                }
            }
        }
    }

    // Cleanup effect
    DisposableEffect(Unit) {
        onDispose {
            currentListener?.let { listener ->
                sensorManager.unregisterListener(listener)
            }
        }
    }

    fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return "%02d:%02d".format(minutes, secs)
    }

    fun startCountdownAndRecording() {
        if (accelerometer == null || gyroscope == null) {
            exportStatus = "❌ This watch must provide an accelerometer and gyroscope."
            return
        }
        hasExported = false
        recordingStartTime = 0L
        remainingTime = timerDuration
        isCountdownActive = true
        isTimerRunning = false
        isRecording = false
        timerFinished = false
        exportStatus = ""
        accelReadings.clear()
        gyroReadings.clear()
        totalReadings = 0

        // Make sure any existing listener is cleaned up
        currentListener?.let { listener ->
            sensorManager.unregisterListener(listener)
            currentListener = null
        }
    }

    fun stopTimerAndRecording() {
        isCountdownActive = false
        isTimerRunning = false
        isRecording = false

        // Unregister sensors
        currentListener?.let { listener ->
            sensorManager.unregisterListener(listener)
            currentListener = null
        }
    }

    fun resetTimer() {
        isCountdownActive = false
        isTimerRunning = false
        isRecording = false
        remainingTime = timerDuration
        countdownTime = 3
        timerFinished = false

        // Unregister sensors
        currentListener?.let { listener ->
            sensorManager.unregisterListener(listener)
            currentListener = null
        }
    }

    fun exportToCSV(): String {
        return try {
            val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val timestamp = dateFormat.format(Date())
            val filename = "U${userId}_T${trailNumber}"
            val externalDir = context.getExternalFilesDir(null)
                ?: throw IOException("External storage is unavailable")
            val csvFile = File(externalDir, "${filename}.csv")
            // Reserve the filename exclusively; never overwrite another trial.
            if (!csvFile.createNewFile()) {
                throw IOException("${csvFile.name} already exists. Archive the existing file off this path, then retry.")
            }

            try {
                FileWriter(csvFile).use { writer ->
                    // Write CSV header with user information
                    writer.append("# User ID: $userId\n")
                    writer.append("# Trail #: $trailNumber\n")
                    writer.append("# Gender: $gender\n")
                    writer.append("# Age: $age\n")
                    writer.append("# Dominant Hand: $dominantHand\n")
                    writer.append("# Timer Duration: ${timerDuration}s\n")
                    writer.append("# Export Time: $timestamp\n")
                    writer.append("# Sampling Rate: ${samplingRates[selectedRateIndex].first}\n")
                    writer.append("# Total Readings: $totalReadings\n")
                    writer.append("# Recording Start Time: $recordingStartTime\n")
                    writer.append("# Start Time Alignment: ${recordingStartTime % 1000}ms into second\n")

                    // Modified data format with millisecond timestamps
                    writer.append("sensor_type,x,y,z,timestamp_ms\n")

                    // Write accelerometer data
                    accelReadings.forEach { reading ->
                        writer.append("accel,${reading.x},${reading.y},${reading.z},${reading.timestamp}\n")
                    }

                    // Write gyroscope data
                    gyroReadings.forEach { reading ->
                        writer.append("gyro,${reading.x},${reading.y},${reading.z},${reading.timestamp}\n")
                    }
                }

            } catch (error: IOException) {
                // Do not leave a partial export that blocks retrying this trial.
                csvFile.delete()
                throw error
            }
            hasExported = true

            // Confirm successful export.
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(200)
            }

            "✅ Exported ${totalReadings} readings to:\n${csvFile.name}\n\n\n"
        } catch (e: IOException) {
            // Error vibration - double pulse pattern
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(
                    longArrayOf(0, 100, 100, 100), // pause, vibrate, pause, vibrate
                    -1 // don't repeat
                ))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 100, 100, 100), -1)
            }

            "❌ Export failed: ${e.message}\n\n\n"
        }
    }

    val hasUnexportedReadings = totalReadings > 0 && !hasExported
    BackHandler(enabled = isCountdownActive || isRecording || hasUnexportedReadings) {
        exportStatus = "Stop and export or clear the readings before leaving."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // User Info Display
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "User ID: $userId | $gender",
                style = MaterialTheme.typography.body2,
                color = Color.Cyan,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Age: $age | Hand: $dominantHand",
                style = MaterialTheme.typography.body2,
                color = Color.Cyan,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Trial #: $trailNumber",
                style = MaterialTheme.typography.body2,
                color = Color.Cyan,
                textAlign = TextAlign.Center
            )
        }


        Spacer(modifier = Modifier.height(4.dp))

        // Timer Display
        Text(
            text = "${formatTime(timerDuration)} Timer",
            style = MaterialTheme.typography.body1,
            color = Color.White
        )

        if (isCountdownActive) {
            Text(
                text = "Get Ready: $countdownTime",
                style = MaterialTheme.typography.title1,
                color = Color.Red
            )
        } else {
            Text(
                text = formatTime(remainingTime),
                style = MaterialTheme.typography.title1,
                color = when {
                    timerFinished -> Color.Green
                    remainingTime <= 10 -> Color.Red
                    isTimerRunning -> Color.Yellow
                    else -> Color.White
                }
            )
        }

        if (timerFinished) {
            Text(
                text = "🎉 Timer Finished! 🎉",
                style = MaterialTheme.typography.body2,
                color = Color.Green
            )
        }

        if (isCountdownActive) {
            Text(
                text = "Prepare to start walking...",
                style = MaterialTheme.typography.body2,
                color = Color.Yellow
            )
        } else if (isRecording) {
            Text(
                text = "📊 Recording Data",
                style = MaterialTheme.typography.body2,
                color = Color.Green
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Timer Controls
        Row {
            Button(
                onClick = { startCountdownAndRecording() },
                enabled = !isTimerRunning && !isCountdownActive && !hasUnexportedReadings,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Green)
            ) {
                Text("Start", color = Color.White)
            }

            Spacer(modifier = Modifier.width(4.dp))

            Button(
                onClick = { stopTimerAndRecording() },
                enabled = isTimerRunning || isCountdownActive,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red)
            ) {
                Text("Stop", color = Color.White)
            }

            Spacer(modifier = Modifier.width(4.dp))

            Button(
                onClick = { resetTimer() },
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
            ) {
                Text("Reset", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Back button
        Button(
            onClick = onNavigateBack,
            enabled = !isCountdownActive && !isRecording && !hasUnexportedReadings,
            colors = ButtonDefaults.buttonColors(backgroundColor = Color.DarkGray)
        ) {
            Text("Back", color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Sampling rate selection
        Text(
            text = "Sampling Rate:",
            style = MaterialTheme.typography.body2,
            color = Color.White
        )

        Text(
            text = samplingRates[selectedRateIndex].first,
            style = MaterialTheme.typography.body2,
            color = Color.Cyan
        )

        Row {
            Button(
                onClick = {
                    if (selectedRateIndex < samplingRates.size - 1) {
                        selectedRateIndex++
                        currentSamplingRate = samplingRates[selectedRateIndex].second
                    }
                },
                enabled = !isRecording && !isCountdownActive && totalReadings == 0,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
            ) {
                Text("Slower", color = Color.White)
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (selectedRateIndex > 0) {
                        selectedRateIndex--
                        currentSamplingRate = samplingRates[selectedRateIndex].second
                    }
                },
                enabled = !isRecording && !isCountdownActive && totalReadings == 0,
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Blue)
            ) {
                Text("Faster", color = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Data display
        if (isRecording) {
            Text(text = accelData, style = MaterialTheme.typography.body2, color = Color.White)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = gyroData, style = MaterialTheme.typography.body2, color = Color.White)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Data storage info
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Data Stored:",
                style = MaterialTheme.typography.body2,
                color = Color.Yellow
            )
            Text(
                text = "Accel: ${accelReadings.size} readings",
                style = MaterialTheme.typography.body2,
                color = Color.White
            )
            Text(
                text = "Gyro: ${gyroReadings.size} readings",
                style = MaterialTheme.typography.body2,
                color = Color.White
            )
            Text(
                text = "Total: $totalReadings readings",
                style = MaterialTheme.typography.body2,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Control buttons
        Row {
            if (totalReadings > 0) {
                Button(
                    onClick = {
                        accelReadings.clear()
                        gyroReadings.clear()
                        totalReadings = 0
                        hasExported = false
                        recordingStartTime = 0L
                        exportStatus = ""
                    },
                    enabled = !isRecording && !isCountdownActive,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
                ) {
                    Text("Clear", color = Color.White)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = { exportStatus = exportToCSV() },
                    enabled = !isRecording && !isCountdownActive && !hasExported,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Magenta)
                ) {
                    Text("Export", color = Color.White)
                }
            }
        }
        // Export status (Centered)
        if (exportStatus.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = exportStatus,
                    style = MaterialTheme.typography.body2,
                    color = if (exportStatus.startsWith("✅")) Color.Green else Color.Red,
                    textAlign = TextAlign.Center
                )
            }
        }

    }
}