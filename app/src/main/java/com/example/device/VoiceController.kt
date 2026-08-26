package com.example.device

import android.content.Context
import android.content.Intent
import android.os.Bundle
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

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _voiceMode =
        MutableStateFlow(VoiceMode.INDIVIDUAL_COMMAND)

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

    // ---------------------------------------------------------
    // TEXT TO SPEECH
    // ---------------------------------------------------------

    private fun initTts() {

        textToSpeech = TextToSpeech(context) { status ->

            if (status == TextToSpeech.SUCCESS) {

                val spanish = Locale("es", "ES")

                val result =
                    textToSpeech?.setLanguage(spanish)

                if (
                    result == TextToSpeech.LANG_MISSING_DATA ||
                    result == TextToSpeech.LANG_NOT_SUPPORTED
                ) {
                    textToSpeech?.setLanguage(Locale.getDefault())
                }

                textToSpeech?.setPitch(_voicePitch.value)
                textToSpeech?.setSpeechRate(1.02f)

                isTtsReady = true

                textToSpeech?.setOnUtteranceProgressListener(
                    object : UtteranceProgressListener() {

                        override fun onStart(
                            utteranceId: String?
                        ) {
                            _voiceState.value =
                                VoiceState.SPEAKING
                        }

                        override fun onDone(
                            utteranceId: String?
                        ) {

                            if (destroyed) return

                            /*
                             * Si estamos en conversación continua,
                             * volvemos a escuchar automáticamente
                             * después de terminar de hablar.
                             */
                            if (
                                _voiceMode.value ==
                                VoiceMode.CONTINUOUS_CONVERSATION
                            ) {

                                startListeningDelayed()

                            } else {

                                _voiceState.value =
                                    VoiceState.IDLE
                            }
                        }

                        override fun onError(
                            utteranceId: String?
                        ) {

                            if (!destroyed) {
                                _voiceState.value =
                                    VoiceState.IDLE
                            }
                        }
                    }
                )
            }
        }
    }

    fun speak(text: String) {

        if (destroyed) return

        if (!isTtsReady || textToSpeech == null) {
            _voiceState.value = VoiceState.IDLE
            return
        }

        _voiceState.value = VoiceState.SPEAKING

        val params = Bundle().apply {

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

    // ---------------------------------------------------------
    // SPEECH RECOGNITION
    // ---------------------------------------------------------

    fun startListening() {

        if (destroyed) return

        if (
            !SpeechRecognizer
                .isRecognitionAvailable(context)
        ) {

            _voiceState.value =
                VoiceState.ERROR

            return
        }

        try {

            /*
             * Detenemos cualquier reconocimiento anterior.
             */
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

                        _voiceState.value =
                            VoiceState.LISTENING
                    }

                    override fun onBeginningOfSpeech() {

                        _voiceState.value =
                            VoiceState.LISTENING
                    }

                    override fun onRmsChanged(
                        rmsdB: Float
                    ) {
                        // Volumen del micrófono.
                    }

                    override fun onBufferReceived(
                        buffer: ByteArray?
                    ) {
                    }

                    override fun onEndOfSpeech() {

                        _voiceState.value =
                            VoiceState.THINKING
                    }

                    override fun onError(
                        error: Int
                    ) {

                        if (destroyed) return

                        /*
                         * Algunos errores son normales cuando
                         * el reconocimiento termina.
                         *
                         * En conversación continua intentamos
                         * escuchar de nuevo.
                         */
                        if (
                            _voiceMode.value ==
                            VoiceMode.CONTINUOUS_CONVERSATION
                        ) {

                            startListeningDelayed()

                        } else {

                            _voiceState.value =
                                VoiceState.IDLE
                        }
                    }

                    override fun onResults(
                        results: Bundle?
                    ) {

                        if (destroyed) return

                        val matches =
                            results?.getStringArrayList(
                                SpeechRecognizer.RESULTS_RECOGNITION
                            )

                        val text =
                            matches
                                ?.firstOrNull()
                                ?.trim()
                                ?: ""

                        if (text.isNotBlank()) {

                            _voiceState.value =
                                VoiceState.THINKING

                            /*
                             * Mandamos el texto al cerebro
                             * de JARVIS.
                             */
                            onSpeechRecognized(text)

                        } else {

                            if (
                                _voiceMode.value ==
                                VoiceMode.CONTINUOUS_CONVERSATION
                            ) {

                                startListeningDelayed()

                            } else {

                                _voiceState.value =
                                    VoiceState.IDLE
                            }
                        }
                    }

                    override fun onPartialResults(
                        partialResults: Bundle?
                    ) {
                    }

                    override fun onEvent(
                        eventType: Int,
                        params: Bundle?
                    ) {
                    }
                }
            )

            val intent =
                Intent(
                    RecognizerIntent.ACTION_RECOGNIZE_SPEECH
                ).apply {

                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )

                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE,
                        "es-ES"
                    )

                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
                        "es-ES"
                    )

                    putExtra(
                        RecognizerIntent.EXTRA_MAX_RESULTS,
                        1
                    )

                    putExtra(
                        RecognizerIntent.EXTRA_PARTIAL_RESULTS,
                        true
                    )
                }

            speechRecognizer?.startListening(intent)

            _voiceState.value =
                VoiceState.LISTENING

        } catch (e: Exception) {

            _voiceState.value =
                VoiceState.ERROR
        }
    }

    /*
     * Pequeña pausa antes de volver a escuchar.
     *
     * Evita que Android intente iniciar el reconocimiento
     * inmediatamente mientras todavía está cerrando la sesión
     * anterior.
     */
    private fun startListeningDelayed() {

        if (destroyed) return

        Thread {

            try {

                Thread.sleep(500)

            } catch (_: InterruptedException) {
                return@Thread
            }

            if (!destroyed) {

                android.os.Handler(
                    android.os.Looper.getMainLooper()
                ).post {

                    if (!destroyed) {
                        startListening()
                    }
                }
            }

        }.start()
    }

    // ---------------------------------------------------------
    // STOP
    // ---------------------------------------------------------

    fun stopListening() {

        try {

            speechRecognizer?.cancel()

        } catch (_: Exception) {
        }

        _voiceState.value =
            VoiceState.IDLE
    }

    fun stopSpeaking() {

        try {

            textToSpeech?.stop()

        } catch (_: Exception) {
        }

        if (
            _voiceState.value ==
            VoiceState.SPEAKING
        ) {

            _voiceState.value =
                VoiceState.IDLE
        }
    }

    /*
     * Parada total de JARVIS.
     */
    fun emergencyStopAll() {

        try {
            speechRecognizer?.cancel()
        } catch (_: Exception) {
        }

        try {
            textToSpeech?.stop()
        } catch (_: Exception) {
        }

        _voiceState.value =
            VoiceState.IDLE
    }

    // ---------------------------------------------------------
    // SETTINGS
    // ---------------------------------------------------------

    fun setVoiceState(
        state: VoiceState
    ) {

        _voiceState.value =
            state
    }

    fun toggleVoiceMode() {

        _voiceMode.value =
            if (
                _voiceMode.value ==
                VoiceMode.INDIVIDUAL_COMMAND
            ) {

                VoiceMode.CONTINUOUS_CONVERSATION

            } else {

                VoiceMode.INDIVIDUAL_COMMAND
            }
    }

    fun setVoiceMode(
        mode: VoiceMode
    ) {

        _voiceMode.value =
            mode

        /*
         * Si se desactiva el modo continuo,
         * detenemos cualquier escucha automática.
         */
        if (
            mode ==
            VoiceMode.INDIVIDUAL_COMMAND
        ) {

            stopListening()
        }
    }

    fun toggleHotword() {

        _isHotwordEnabled.value =
            !_isHotwordEnabled.value
    }

    fun setPitch(
        pitch: Float
    ) {

        _voicePitch.value =
            pitch

        textToSpeech?.setPitch(
            pitch
        )
    }

    fun setVolume(
        volume: Float
    ) {

        _voiceVolume.value =
            volume.coerceIn(
                0.0f,
                1.0f
            )
    }

    // ---------------------------------------------------------
    // DESTROY
    // ---------------------------------------------------------

    fun destroy() {

        destroyed = true

        try {
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
        } catch (_: Exception) {
        }

        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (_: Exception) {
        }

        speechRecognizer = null
        textToSpeech = null

        _voiceState.value =
            VoiceState.IDLE
    }
}