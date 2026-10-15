package test.gait.presentation.UserInfo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import test.gait.data.UserPrefs

data class UserInfo(
    val userId: String = "",
    val gender: String = "Male",
    val age: String = "",
    val timerDuration: Int = 120,
    val dominantHand: String = "Right",
    val trailNumber: String = "1"
)

@Composable
fun UserInfoScreen(
    onNavigateToSensor: (UserInfo) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val savedPrefs = remember { UserPrefs.loadUserInfo(context) }

    var userId by remember { mutableStateOf(savedPrefs["userId"] ?: "1") }
    var selectedGender by remember { mutableStateOf(savedPrefs["gender"] ?: "Male") }
    var age by remember { mutableStateOf(savedPrefs["age"] ?: "25") }
    var timerDuration by remember { mutableStateOf((savedPrefs["timerDuration"] ?: "120").toInt()) }
    var dominantHand by remember { mutableStateOf(savedPrefs["dominantHand"] ?: "Right") }
    var trailNumber by remember { mutableStateOf(savedPrefs["trailNumber"] ?: "1") }

    val genderOptions = listOf("Male", "Female", "Others")
    val handOptions = listOf("Left", "Right")

    fun incrementTrail() {
        val current = trailNumber.toIntOrNull() ?: 1
        trailNumber = (current + 1).toString()
    }

    fun decrementTrail() {
        val current = trailNumber.toIntOrNull() ?: 1
        if (current > 1) trailNumber = (current - 1).toString()
    }

    fun incrementTimer() {
        if (timerDuration < 600) timerDuration += 30
    }

    fun decrementTimer() {
        if (timerDuration > 30) timerDuration -= 30
    }

    fun formatTimerDuration(seconds: Int): String {
        return when {
            seconds < 60 -> "${seconds}s"
            seconds % 60 == 0 -> "${seconds / 60}min"
            else -> "${seconds / 60}m ${seconds % 60}s"
        }
    }

    fun incrementUserId() {
        val currentId = userId.toIntOrNull() ?: 0
        userId = (currentId + 1).toString()
    }

    fun decrementUserId() {
        val currentId = userId.toIntOrNull() ?: 1
        if (currentId > 1) userId = (currentId - 1).toString()
    }

    fun incrementAge() {
        val currentAge = age.toIntOrNull() ?: 0
        if (currentAge < 120) age = (currentAge + 1).toString()
    }

    fun decrementAge() {
        val currentAge = age.toIntOrNull() ?: 1
        if (currentAge > 1) age = (currentAge - 1).toString()
    }

    val isFormValid = userId.isNotBlank() &&
            age.isNotBlank() &&
            age.toIntOrNull() != null &&
            age.toInt() in 1..120

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("User Info", style = MaterialTheme.typography.title3, color = Color.White, textAlign = TextAlign.Center)

            Spacer(modifier = Modifier.height(12.dp))

            Text("User ID:", style = MaterialTheme.typography.body2, color = Color.White)
            Text(userId, style = MaterialTheme.typography.title2, color = Color.Cyan)

            Row {
                Button({ decrementUserId() }, Modifier.size(32.dp), colors = ButtonDefaults.buttonColors(Color.Blue)) {
                    Text("-", color = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button({ incrementUserId() }, Modifier.size(32.dp), colors = ButtonDefaults.buttonColors(Color.Blue)) {
                    Text("+", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Trial:", style = MaterialTheme.typography.body2, color = Color.White)
            Text(trailNumber, style = MaterialTheme.typography.title2, color = Color.Cyan)
            Row {
                Button({ decrementTrail() }, Modifier.size(32.dp), colors = ButtonDefaults.buttonColors(Color.Blue)) {
                    Text("-", color = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button({ incrementTrail() }, Modifier.size(32.dp), colors = ButtonDefaults.buttonColors(Color.Blue)) {
                    Text("+", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Gender:", style = MaterialTheme.typography.body2, color = Color.White)
            Text(selectedGender, style = MaterialTheme.typography.body2, color = Color.Cyan)
            Row {
                genderOptions.forEach { gender ->
                    Button(
                        { selectedGender = gender },
                        colors = ButtonDefaults.buttonColors(if (selectedGender == gender) Color.Blue else Color.Gray),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) { Text(gender, color = Color.White) }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Dominant Hand:", style = MaterialTheme.typography.body2, color = Color.White)
            Text(dominantHand, style = MaterialTheme.typography.body2, color = Color.Cyan)
            Row {
                handOptions.forEach { hand ->
                    Button(
                        { dominantHand = hand },
                        colors = ButtonDefaults.buttonColors(if (dominantHand == hand) Color.Blue else Color.Gray),
                        modifier = Modifier.padding(horizontal = 2.dp)
                    ) { Text(hand, color = Color.White) }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Age:", style = MaterialTheme.typography.body2, color = Color.White)
            Text(age, style = MaterialTheme.typography.title2, color = Color.Cyan)

            Row {
                Button({ decrementAge() }, enabled = (age.toIntOrNull() ?: 1) > 1, modifier = Modifier.size(32.dp),
                    colors = ButtonDefaults.buttonColors(Color.Blue)) {
                    Text("-", color = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button({ incrementAge() }, enabled = (age.toIntOrNull() ?: 0) < 120, modifier = Modifier.size(32.dp),
                    colors = ButtonDefaults.buttonColors(Color.Blue)) {
                    Text("+", color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Timer:", style = MaterialTheme.typography.body2, color = Color.White)
            Text(formatTimerDuration(timerDuration), style = MaterialTheme.typography.title2, color = Color.Cyan)

            Row {
                Button({ decrementTimer() }, enabled = timerDuration > 30, modifier = Modifier.size(32.dp),
                    colors = ButtonDefaults.buttonColors(Color.Blue)) {
                    Text("-30s", color = Color.White, style = MaterialTheme.typography.caption1)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button({ incrementTimer() }, enabled = timerDuration < 600, modifier = Modifier.size(32.dp),
                    colors = ButtonDefaults.buttonColors(Color.Blue)) {
                    Text("+30s", color = Color.White, style = MaterialTheme.typography.caption1)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isFormValid) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Summary:", style = MaterialTheme.typography.body2, color = Color.Yellow)
                    Text("ID: $userId | $selectedGender", style = MaterialTheme.typography.body2, color = Color.White)
                    Text("Age: $age | Hand: $dominantHand", style = MaterialTheme.typography.body2, color = Color.White)
                    Text("Trial #: $trailNumber", style = MaterialTheme.typography.body2, color = Color.White)
                    Text("Timer: ${formatTimerDuration(timerDuration)}", style = MaterialTheme.typography.body2, color = Color.White)
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row {
                Button(onNavigateBack, colors = ButtonDefaults.buttonColors(Color.Gray), shape = RoundedCornerShape(8.dp)) {
                    Text("Back", color = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    {
                        // Persist settings for the next trial.
                        UserPrefs.saveUserInfo(context, mapOf(
                            "userId" to userId,
                            "gender" to selectedGender,
                            "age" to age,
                            "timerDuration" to timerDuration.toString(),
                            "dominantHand" to dominantHand,
                            "trailNumber" to trailNumber
                        ))

                        val userInfo = UserInfo(
                            userId = userId,
                            gender = selectedGender,
                            age = age,
                            timerDuration = timerDuration,
                            dominantHand = dominantHand,
                            trailNumber = trailNumber
                        )
                        onNavigateToSensor(userInfo)
                    },
                    enabled = isFormValid,
                    colors = ButtonDefaults.buttonColors(if (isFormValid) Color.Green else Color.Gray),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Continue", color = Color.White, style = MaterialTheme.typography.caption1)
                }
            }
        }
    }
}
