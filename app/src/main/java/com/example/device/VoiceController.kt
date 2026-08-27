package com.example.device

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class VoiceState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    ERROR
}

enum class VoiceMode {
    INDIVIDUAL_COMMAND,
    CONTINUOUS_CONVERSATION
}

class VoiceController(
    private val context: Context,
    private val onSpeechRecognized: (String) -> Unit
) {

    private var textToSpeech: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null

    private var isTtsReady = false
    private var destroyed = false

    // Control real de la sesión de voz
    private var manuallyStopped = false
    private var listening = false

    private val mainHandler = Handler(Looper.getMainLooper())

    private val _voiceState =
        MutableStateFlow(VoiceState.IDLE)

    val voiceState: StateFlow<VoiceState> =
        _voiceState.asStateFlow()

    private val _voiceMode =
        MutableStateFlow(
            VoiceMode.INDIVIDUAL_COMMAND
        )

    val voiceMode: StateFlow<VoiceMode> =
        _voiceMode.asStateFlow()

    private val _isHotwordEnabled =
        MutableStateFlow(true)

    val isHotwordEnabled: StateFlow<Boolean> =
        _isHotwordEnabled.asStateFlow()

    private val _voiceVolume =
        MutableStateFlow(1.0f)

    val voiceVolume: StateFlow<Float> =
        _voiceVolume.asStateFlow()

    private val _voicePitch =
        MutableStateFlow(0.95f)

    val voicePitch: StateFlow<Float> =
        _voicePitch.asStateFlow()

    init {
        initTts()
    }

    // =========================================================
    // TEXT TO SPEECH
    // =========================================================

    private fun initTts() {

        textToSpeech =
            TextToSpeech(context) { status ->

                if (status != TextToSpeech.SUCCESS) {
                    return@TextToSpeech
                }

                val spanish =
                    Locale("es", "ES")

                val result =
                    textToSpeech?.setLanguage(spanish)

                if (
                    result ==
                    TextToSpeech.LANG_MISSING_DATA ||
                    result ==
                    TextToSpeech.LANG_NOT_SUPPORTED
                ) {

                    textToSpeech?.setLanguage(
                        Locale.getDefault()
                    )
                }

                textToSpeech?.setPitch(
                    _voicePitch.value
                )

                textToSpeech?.setSpeechRate(
                    1.02f
                )

                isTtsReady = true

                textToSpeech?.setOnUtteranceProgressListener(
                    object : UtteranceProgressListener() {

                        override fun onStart(
                            utteranceId: String?
                        ) {

                            if (destroyed) return

                            _voiceState.value =
                                VoiceState.SPEAKING
                        }

                        override fun onDone(
                            utteranceId: String?
                        ) {

                            if (destroyed) return

                            if (manuallyStopped) {
                                _voiceState.value =
                                    VoiceState.IDLE
                                return
                            }

                            if (
                                _voiceMode.value ==
                                VoiceMode.CONTINUOUS_CONVERSATION
                            ) {

                                scheduleListening()

                            } else {

                                _voiceState.value =
                                    VoiceState.IDLE
                            }
                        }

                        override fun onError(
                            utteranceId: String?
                        ) {

                            if (destroyed) return

                            if (
                                _voiceMode.value ==
                                VoiceMode.CONTINUOUS_CONVERSATION &&
                                !manuallyStopped
                            ) {

                                scheduleListening()

                            } else {

                                _voiceState.value =
                                    VoiceState.IDLE
                            }
                        }
                    }
                )
            }
    }

    fun speak(text: String) {

        if (destroyed) return

        if (
            !isTtsReady ||
            textToSpeech == null
        ) {

            _voiceState.value =
                VoiceState.IDLE

            return
        }

        manuallyStopped = false

        _voiceState.value =
            VoiceState.SPEAKING

        val params =
            Bundle().apply {

                putFloat(
                    TextToSpeech.Engine.KEY_PARAM_VOLUME,
                    _voiceVolume.value
                )
            }

        textToSpeech?.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            params,
            "JARVIS_${System.currentTimeMillis()}"
        )
    }

    // =========================================================
    // SPEECH RECOGNITION
    // =========================================================

    fun startListening() {

        if (destroyed) return

        manuallyStopped = false

        // Cancelamos cualquier reinicio automático pendiente
        mainHandler.removeCallbacksAndMessages(null)

        if (
            !SpeechRecognizer
                .isRecognitionAvailable(context)
        ) {

            _voiceState.value =
                VoiceState.ERROR

            return
        }

        try {

            speechRecognizer?.cancel()
            speechRecognizer?.destroy()

            speechRecognizer =
                SpeechRecognizer
                    .createSpeechRecognizer(context)

            speechRecognizer?.setRecognitionListener(
                object : RecognitionListener {

                    override fun onReadyForSpeech(
                        params: Bundle?
                    ) {

                        if (destroyed ||
                            manuallyStopped
                        ) return

                        listening = true

                        _voiceState.value =
                            VoiceState.LISTENING
                    }

                    override fun onBeginningOfSpeech