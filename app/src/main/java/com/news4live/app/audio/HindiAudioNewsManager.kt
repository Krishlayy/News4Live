package com.news4live.app.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

data class AudioPlayerState(
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val speechRate: Float = 1.0f,
    val currentParagraphIndex: Int = 0,
    val totalParagraphs: Int = 0,
    val isLanguageSupported: Boolean = true,
    val isEngineReady: Boolean = false
)

class HindiAudioNewsManager(private val context: Context) : TextToSpeech.OnInitListener {

    private val TAG = "HindiAudioNewsManager"
    private var tts: TextToSpeech? = null

    private val _state = MutableStateFlow(AudioPlayerState())
    val state: StateFlow<AudioPlayerState> = _state.asStateFlow()

    private var paragraphsToSpeak: List<String> = emptyList()
    private var currentArticleTitle: String = ""

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize TextToSpeech", e)
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val hindiLocale = Locale("hi", "IN")
            val result = tts?.setLanguage(hindiLocale)

            val isSupported = if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Fallback to generic Hindi or device default
                val fallbackResult = tts?.setLanguage(Locale("hi"))
                fallbackResult != TextToSpeech.LANG_MISSING_DATA && fallbackResult != TextToSpeech.LANG_NOT_SUPPORTED
            } else {
                true
            }

            tts?.setSpeechRate(_state.value.speechRate)
            tts?.setPitch(1.0f)

            setupProgressListener()

            _state.value = _state.value.copy(
                isEngineReady = true,
                isLanguageSupported = isSupported
            )
            Log.d(TAG, "TTS initialized. Hindi support: $isSupported")
        } else {
            Log.e(TAG, "TTS initialization failed with status: $status")
            _state.value = _state.value.copy(isEngineReady = false)
        }
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                val index = utteranceId?.toIntOrNull() ?: 0
                _state.value = _state.value.copy(
                    isPlaying = true,
                    isPaused = false,
                    currentParagraphIndex = index
                )
            }

            override fun onDone(utteranceId: String?) {
                val index = utteranceId?.toIntOrNull() ?: 0
                if (index >= paragraphsToSpeak.size - 1) {
                    _state.value = _state.value.copy(
                        isPlaying = false,
                        isPaused = false,
                        currentParagraphIndex = 0
                    )
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                Log.e(TAG, "TTS utterance error on ID: $utteranceId")
                _state.value = _state.value.copy(isPlaying = false, isPaused = false)
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                Log.e(TAG, "TTS utterance error code: $errorCode on ID: $utteranceId")
                _state.value = _state.value.copy(isPlaying = false, isPaused = false)
            }
        })
    }

    fun playArticle(title: String, bodyParagraphs: List<String>) {
        if (tts == null || !_state.value.isEngineReady) return

        currentArticleTitle = title
        val cleanedList = mutableListOf<String>()
        if (title.isNotBlank()) cleanedList.add(title.trim())

        bodyParagraphs.forEach { p ->
            val clean = p.replace(Regex("<.*?>"), "").trim()
            if (clean.isNotBlank() && clean.length > 3) {
                cleanedList.add(clean)
            }
        }

        if (cleanedList.isEmpty()) return

        paragraphsToSpeak = cleanedList
        _state.value = _state.value.copy(
            totalParagraphs = cleanedList.size,
            currentParagraphIndex = 0
        )

        speakFromIndex(0)
    }

    private fun speakFromIndex(startIndex: Int) {
        tts?.stop()
        tts?.setSpeechRate(_state.value.speechRate)

        for (i in startIndex until paragraphsToSpeak.size) {
            val text = paragraphsToSpeak[i]
            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, i.toString())
            }
            val queueMode = if (i == startIndex) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            tts?.speak(text, queueMode, params, i.toString())
        }

        _state.value = _state.value.copy(
            isPlaying = true,
            isPaused = false,
            currentParagraphIndex = startIndex
        )
    }

    fun pause() {
        if (!_state.value.isPlaying) return
        tts?.stop()
        _state.value = _state.value.copy(
            isPlaying = false,
            isPaused = true
        )
    }

    fun resume() {
        if (!_state.value.isPaused || paragraphsToSpeak.isEmpty()) return
        speakFromIndex(_state.value.currentParagraphIndex)
    }

    fun togglePlayPause(title: String, bodyParagraphs: List<String>) {
        when {
            _state.value.isPlaying -> pause()
            _state.value.isPaused -> resume()
            else -> playArticle(title, bodyParagraphs)
        }
    }

    fun stop() {
        tts?.stop()
        _state.value = _state.value.copy(
            isPlaying = false,
            isPaused = false,
            currentParagraphIndex = 0
        )
    }

    fun setSpeechRate(rate: Float) {
        _state.value = _state.value.copy(speechRate = rate)
        tts?.setSpeechRate(rate)
        if (_state.value.isPlaying) {
            speakFromIndex(_state.value.currentParagraphIndex)
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS", e)
        }
    }
}
