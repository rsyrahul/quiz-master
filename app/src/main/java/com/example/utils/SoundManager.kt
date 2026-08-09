package com.example.utils

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log

class SoundManager {

    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 80)
        } catch (e: Exception) {
            Log.e("SoundManager", "Failed to initialize ToneGenerator", e)
        }
    }

    fun playClickSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
        } catch (e: Exception) {
            Log.e("SoundManager", "Click sound error", e)
        }
    }

    fun playCorrectSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 180)
        } catch (e: Exception) {
            Log.e("SoundManager", "Correct sound error", e)
        }
    }

    fun playWrongSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 250)
        } catch (e: Exception) {
            Log.e("SoundManager", "Wrong sound error", e)
        }
    }

    fun playTimerWarningSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_KEYPAD_VOLUME_KEY_LITE, 80)
        } catch (e: Exception) {
            Log.e("SoundManager", "Timer sound error", e)
        }
    }

    fun playCompletionSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 300)
        } catch (e: Exception) {
            Log.e("SoundManager", "Completion sound error", e)
        }
    }

    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
