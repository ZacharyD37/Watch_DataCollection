package test.gait.data

import android.content.Context

object UserPrefs {
    private const val PREF_NAME = "user_prefs"

    fun saveUserInfo(context: Context, data: Map<String, String>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        data.forEach { (key, value) -> editor.putString(key, value) }
        editor.apply()
    }

    fun loadUserInfo(context: Context): Map<String, String> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        return mapOf(
            "userId" to (prefs.getString("userId", "1") ?: "1"),
            "gender" to (prefs.getString("gender", "Male") ?: "Male"),
            "age" to (prefs.getString("age", "25") ?: "25"),
            "dominantHand" to (prefs.getString("dominantHand", "Right") ?: "Right"),
            "trailNumber" to (prefs.getString("trailNumber", "1") ?: "1"),
            "timerDuration" to (prefs.getString("timerDuration", "120") ?: "120")
        )
    }
}
