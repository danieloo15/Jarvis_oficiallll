package com.example.device

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AdminModeManager {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var timerJob: Job? = null

    private val _isAdminActive = MutableStateFlow(false)
    val isAdminActive: StateFlow<Boolean> = _isAdminActive.asStateFlow()

    private val _remainingSeconds = MutableStateFlow(0L)
    val remainingSeconds: StateFlow<Long> = _remainingSeconds.asStateFlow()

    fun enableAdminMode(minutes: Int) {
        _isAdminActive.value = true
        _remainingSeconds.value = minutes * 60L

        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive && _remainingSeconds.value > 0) {
                delay(1000)
                _remainingSeconds.value -= 1
            }
            // Auto revert when timer reaches zero
            _isAdminActive.value = false
            _remainingSeconds.value = 0L
        }
    }

    fun disableAdminMode() {
        timerJob?.cancel()
        _isAdminActive.value = false
        _remainingSeconds.value = 0L
    }

    fun formatRemainingTime(): String {
        val totalSecs = _remainingSeconds.value
        val mins = totalSecs / 60
        val secs = totalSecs % 60
        return String.format("%02d:%02d", mins, secs)
    }
}
