package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

object AlarmUtil {
    fun playBellAlarm(context: Context, isStart: Boolean) {
        try {
            // Play Alarm Tone
            val toneType = if (isStart) ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD else ToneGenerator.TONE_SUP_ERROR
            val toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100)
            toneGenerator.startTone(toneType, 1500)

            // Vibrate device
            vibrateDevice(context)
        } catch (e: Exception) {
            Log.e("AlarmUtil", "Error playing alarm sound: ${e.message}")
        }
    }

    private fun vibrateDevice(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                vibrator.vibrate(VibrationEffect.createOneShot(800, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(800, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(800)
                }
            }
        } catch (e: Exception) {
            Log.e("AlarmUtil", "Error vibrating device: ${e.message}")
        }
    }
}
