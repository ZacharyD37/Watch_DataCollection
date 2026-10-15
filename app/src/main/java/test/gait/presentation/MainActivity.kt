package test.gait.presentation

import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import test.gait.R
import test.gait.presentation.sensor.SensorScreen
import test.gait.presentation.UserInfo.UserInfoScreen

class MainActivity : ComponentActivity() {
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "GaitApp::WakeLock"
        )

        setContent {
            WearApp()
        }
    }

    override fun onResume() {
        super.onResume()
        wakeLock?.let { if (!it.isHeld) it.acquire(11 * 60 * 1000L) }
    }

    override fun onPause() {
        wakeLock?.let { if (it.isHeld) it.release() }
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeLock?.let {
            if (it.isHeld) it.release()
        }
        wakeLock = null
    }
}

@Composable
fun WearApp() {
    val navController = rememberNavController()
    MaterialTheme {
        NavHost(navController = navController, startDestination = "home") {
            composable("home") {
                SimpleGreeting(
                    onNavigateToUserInfo = { navController.navigate("userinfo") }
                )
            }

            composable("userinfo") {
                UserInfoScreen(
                    onNavigateToSensor = { userInfo ->
                        navController.navigate(
                            "sensor/${userInfo.userId}/${userInfo.gender}/${userInfo.age}/${userInfo.timerDuration}/${userInfo.dominantHand}/${userInfo.trailNumber}"
                        )
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(
                "sensor/{userId}/{gender}/{age}/{timerDuration}/{dominantHand}/{trailNumber}",
                arguments = listOf(
                    navArgument("userId") { type = NavType.StringType },
                    navArgument("gender") { type = NavType.StringType },
                    navArgument("age") { type = NavType.StringType },
                    navArgument("timerDuration") { type = NavType.IntType },
                    navArgument("dominantHand") { type = NavType.StringType },
                    navArgument("trailNumber") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                val gender = backStackEntry.arguments?.getString("gender") ?: "Male"
                val age = backStackEntry.arguments?.getString("age") ?: "0"
                val timerDuration = backStackEntry.arguments?.getInt("timerDuration") ?: 120
                val dominantHand = backStackEntry.arguments?.getString("dominantHand") ?: "Right"
                val trailNumber = backStackEntry.arguments?.getString("trailNumber") ?: "1"

                SensorScreen(
                    userId = userId,
                    gender = gender,
                    age = age,
                    timerDuration = timerDuration,
                    dominantHand = dominantHand,
                    trailNumber = trailNumber,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

@Composable
fun SimpleGreeting(onNavigateToUserInfo: () -> Unit) {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.welcome),
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateToUserInfo,
                modifier = Modifier.width(120.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color.LightGray,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Start Sensors", color = Color.White)
            }
        }
    }
}
