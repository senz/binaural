package com.github.senz.binaural

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private data class BinauralPreset(
    val nameResId: Int,
    val carrierHz: Float,
    val beatHz: Float,
)

private fun formatHz(value: Float): String = if (value == value.toLong().toFloat()) value.toLong().toString() else value.toString()

private val BINAURAL_PRESETS =
    listOf(
        BinauralPreset(R.string.preset_focus, 225f, 16f),
        BinauralPreset(R.string.preset_euphoria, 250f, 8.5f),
        BinauralPreset(R.string.preset_relaxation, 225f, 9f),
        BinauralPreset(R.string.preset_deep_sleep, 150f, 2.5f),
        BinauralPreset(R.string.preset_meditation, 225f, 6f),
        BinauralPreset(R.string.preset_creativity, 225f, 6.5f),
        BinauralPreset(R.string.preset_energy, 225f, 21f),
        BinauralPreset(R.string.preset_power_nap, 175f, 5f),
        BinauralPreset(R.string.preset_40hz_gamma, 225f, 40f),
    )

@Composable
fun BinauralScreen(
    isPlaying: Boolean,
    timerEndTimeMillis: Long,
    onPlayRequested: (carrierHz: Float, beatHz: Float, isBinaural: Boolean, timerMinutes: Int?) -> Unit,
    onStopRequested: () -> Unit,
    onRefreshAndRestart: (carrierHz: Float, beatHz: Float, isBinaural: Boolean, timerMinutes: Int?) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val labelColor = Color(0xFFE8E8E8)
    val errorTextColor = Color(0xFFFF8A80) // readable on dark background
    val textFieldColors =
        OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedPlaceholderColor = Color(0xFFB0B0B0),
            unfocusedPlaceholderColor = Color(0xFFB0B0B0),
            focusedLabelColor = labelColor,
            unfocusedLabelColor = labelColor,
            cursorColor = Color.White,
            focusedBorderColor = MaterialTheme.colorScheme.outline,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        )

    var carrierText by remember { mutableStateOf("225") }
    var beatText by remember { mutableStateOf("40") }
    var isBinaural by remember { mutableStateOf(true) }
    var timerMinutes by remember { mutableStateOf<Int?>(5) } // null = no timer, 5, 10, 15; default 5 min
    var timerDropdownExpanded by remember { mutableStateOf(false) }
    var carrierError by remember { mutableStateOf<String?>(null) }
    var beatError by remember { mutableStateOf<String?>(null) }
    var isDataChanged by remember { mutableStateOf(true) }
    var remainingSeconds by remember { mutableStateOf(0L) }

    LaunchedEffect(timerEndTimeMillis, isPlaying) {
        if (timerEndTimeMillis <= 0L || !isPlaying) {
            remainingSeconds = 0L
            return@LaunchedEffect
        }
        while (true) {
            val remaining = (timerEndTimeMillis - System.currentTimeMillis()) / 1000
            remainingSeconds = maxOf(0L, remaining)
            delay(1000)
        }
    }

    fun validateCarrier(): Boolean {
        if (carrierText.isBlank()) {
            carrierError = "Carrier Frequency is required!"
            return false
        }
        return try {
            val v = carrierText.toFloat()
            when {
                v < 20 -> {
                    carrierError = "Valid range: 20–1200 Hz (min 20)"
                    false
                }
                v > 1200 -> {
                    carrierError = "Valid range: 20–1200 Hz (max 1200)"
                    false
                }
                else -> {
                    carrierError = null
                    true
                }
            }
        } catch (_: NumberFormatException) {
            carrierError = "Carrier Frequency is required!"
            false
        }
    }

    fun validateBeat(): Boolean {
        if (beatText.isBlank()) {
            beatError = "Beat Frequency is required!"
            return false
        }
        val minBeat = if (isBinaural) 0f else 0.5f
        return try {
            val v = beatText.toFloat()
            when {
                v < minBeat -> {
                    beatError = "Valid range: $minBeat–1200 Hz (min $minBeat)"
                    false
                }
                v > 1200 -> {
                    beatError = "Valid range: $minBeat–1200 Hz (max 1200)"
                    false
                }
                else -> {
                    beatError = null
                    true
                }
            }
        } catch (_: NumberFormatException) {
            beatError = "Beat Frequency is required!"
            false
        }
    }

    fun tryRefreshAndRestart() {
        if (!isPlaying || !isDataChanged) return
        val c = carrierText.toFloatOrNull() ?: return
        val b = beatText.toFloatOrNull() ?: return
        if (!validateCarrier() || !validateBeat()) return
        val minBeat = if (isBinaural) 0f else 0.5f
        if (b < minBeat) return
        onRefreshAndRestart(c, b, isBinaural, timerMinutes)
        isDataChanged = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.Top),
        ) {
            BinauralTimerSection(
                state = TimerSectionState(timerMinutes, timerDropdownExpanded, isPlaying),
                onTimerMinutesChange = {
                    timerMinutes = it
                    isDataChanged = true
                },
                onTimerDropdownExpandedChange = { timerDropdownExpanded = it },
                textFieldColors = textFieldColors,
            )

            BinauralStatusSection(
                remainingSeconds = remainingSeconds,
                isPlaying = isPlaying,
                timerEndTimeMillis = timerEndTimeMillis,
                labelColor = labelColor,
            )

            BinauralIsochronicSelector(
                isBinaural = isBinaural,
                isPlaying = isPlaying,
                labelColor = labelColor,
                onBinauralSelected = {
                    isBinaural = true
                    isDataChanged = true
                },
                onIsochronicSelected = {
                    isBinaural = false
                    val b = beatText.toFloatOrNull()
                    if (b != null && b < 0.5f) beatText = "0.5"
                    isDataChanged = true
                },
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth(0.4f)
                            .alpha(if (isPlaying) 0.5f else 1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    OutlinedTextField(
                        value = carrierText,
                        onValueChange = {
                            carrierText = it
                            isDataChanged = false
                            carrierError = null
                            validateCarrier()
                            if (validateBeat()) isDataChanged = true
                        },
                        label = { Text(stringResource(R.string.carrier_label)) },
                        placeholder = { Text(stringResource(R.string.carrier_hint)) },
                        singleLine = true,
                        readOnly = isPlaying,
                        isError = carrierError != null,
                        supportingText =
                            if (carrierError != null) {
                                { Text(carrierError!!, color = errorTextColor) }
                            } else {
                                null
                            },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .onFocusChanged { if (!it.isFocused) tryRefreshAndRestart() },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                        colors = textFieldColors,
                    )
                    Text(
                        text =
                            stringResource(R.string.carrier_secondary_hint) + " " +
                                if (isPlaying && !isDataChanged && carrierError == null) {
                                    if (isBinaural) {
                                        val c = carrierText.toFloatOrNull() ?: 0f
                                        val b = beatText.toFloatOrNull() ?: 0f
                                        "%.2f / %.2f".format(c + b / 2, c - b / 2)
                                    } else {
                                        carrierText
                                    }
                                } else {
                                    stringResource(R.string.not_applicable)
                                },
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE0E0E0),
                    )
                }

                IconButton(
                    onClick = {
                        focusManager.clearFocus()
                        timerDropdownExpanded = false
                        if (isPlaying) {
                            onStopRequested()
                        } else {
                            if (!validateCarrier() || !validateBeat()) return@IconButton
                            val c = carrierText.toFloat()
                            val b = beatText.toFloat()
                            val minBeat = if (isBinaural) 0f else 0.5f
                            if (b < minBeat) return@IconButton
                            onPlayRequested(c, b, isBinaural, timerMinutes)
                            isDataChanged = false
                        }
                    },
                    modifier = Modifier.size(64.dp),
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Stop" else "Play",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }

                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth(0.4f)
                            .alpha(if (isPlaying) 0.5f else 1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    OutlinedTextField(
                        value = beatText,
                        onValueChange = {
                            beatText = it
                            isDataChanged = false
                            beatError = null
                            validateBeat()
                            if (validateCarrier()) isDataChanged = true
                        },
                        label = { Text(stringResource(R.string.beat_label)) },
                        placeholder = { Text(stringResource(R.string.beat_hint)) },
                        singleLine = true,
                        readOnly = isPlaying,
                        isError = beatError != null,
                        supportingText =
                            if (beatError != null) {
                                { Text(beatError!!, color = errorTextColor) }
                            } else {
                                null
                            },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .onFocusChanged { if (!it.isFocused) tryRefreshAndRestart() },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                        colors = textFieldColors,
                    )
                    Text(
                        text =
                            stringResource(R.string.beat_secondary_hint) + " " +
                                if (isPlaying && !isDataChanged && beatError == null) beatText else stringResource(R.string.not_applicable),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFE0E0E0),
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            BinauralPresetsSection(
                presets = BINAURAL_PRESETS,
                labelColor = labelColor,
                onPresetClick = { preset ->
                    carrierText = formatHz(preset.carrierHz)
                    beatText = formatHz(preset.beatHz)
                    carrierError = null
                    beatError = null
                    focusManager.clearFocus()
                    timerDropdownExpanded = false
                    onPlayRequested(preset.carrierHz, preset.beatHz, isBinaural, timerMinutes)
                    isDataChanged = false
                },
            )

            Spacer(modifier = Modifier.weight(1f))
        }

        Text(
            text = "Build: ${BuildConfig.BUILD_TIME} · ${BuildConfig.GIT_SHA}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFAAAAAA),
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 20.dp)
                    .padding(bottom = 32.dp),
        )
    }
}

@Composable
private fun BinauralIsochronicSelector(
    isBinaural: Boolean,
    isPlaying: Boolean,
    labelColor: Color,
    onBinauralSelected: () -> Unit,
    onIsochronicSelected: () -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .alpha(if (isPlaying) 0.5f else 1f),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        RadioButton(
            selected = isBinaural,
            onClick = {
                if (isPlaying) return@RadioButton
                onBinauralSelected()
            },
            enabled = !isPlaying,
        )
        Text(
            stringResource(R.string.binaural_radio),
            color = labelColor,
            style = MaterialTheme.typography.bodyLarge,
            modifier =
                Modifier.clickable(enabled = !isPlaying) {
                    onBinauralSelected()
                },
        )
        Spacer(modifier = Modifier.widthIn(min = 24.dp))
        RadioButton(
            selected = !isBinaural,
            onClick = {
                if (isPlaying) return@RadioButton
                if (!isBinaural) return@RadioButton
                onIsochronicSelected()
            },
            enabled = !isPlaying,
        )
        Text(
            stringResource(R.string.isochronic_radio),
            color = labelColor,
            style = MaterialTheme.typography.bodyLarge,
            modifier =
                Modifier.clickable(enabled = !isPlaying) {
                    onIsochronicSelected()
                },
        )
    }
}

@Composable
private fun BinauralPresetsSection(
    presets: List<BinauralPreset>,
    labelColor: Color,
    onPresetClick: (BinauralPreset) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.presets_label),
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFFB0B0B0),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            presets.chunked(3).forEach { rowPresets ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    rowPresets.forEach { preset ->
                        OutlinedButton(
                            onClick = { onPresetClick(preset) },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                text = stringResource(preset.nameResId),
                                color = labelColor,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                            )
                        }
                    }
                    if (rowPresets.size < 3) {
                        repeat(3 - rowPresets.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

private data class TimerSectionState(
    val timerMinutes: Int?,
    val timerDropdownExpanded: Boolean,
    val isPlaying: Boolean,
)

@Composable
private fun BinauralTimerSection(
    state: TimerSectionState,
    onTimerMinutesChange: (Int?) -> Unit,
    onTimerDropdownExpandedChange: (Boolean) -> Unit,
    textFieldColors: TextFieldColors,
) {
    val timerNo = stringResource(R.string.timer_no)
    val timer5 = stringResource(R.string.timer_5)
    val timer10 = stringResource(R.string.timer_10)
    val timer15 = stringResource(R.string.timer_15)

    fun timerLabel(m: Int?) =
        when (m) {
            null -> timerNo
            5 -> timer5
            10 -> timer10
            15 -> timer15
            else -> timerNo
        }
    val timerOptions = listOf<Int?>(null, 5, 10, 15)
    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .alpha(if (state.isPlaying) 0.5f else 1f),
    ) {
        OutlinedTextField(
            value = timerLabel(state.timerMinutes),
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.timer_label)) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
            modifier =
                Modifier
                    .fillMaxWidth()
                    .widthIn(min = 120.dp),
            colors = textFieldColors,
        )
        // Overlay to open dropdown on tap; disabled while playing
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clickable(enabled = !state.isPlaying) { onTimerDropdownExpandedChange(true) },
        )
        DropdownMenu(
            expanded = state.timerDropdownExpanded && !state.isPlaying,
            onDismissRequest = { onTimerDropdownExpandedChange(false) },
        ) {
            timerOptions.forEach { min ->
                DropdownMenuItem(
                    text = { Text(timerLabel(min)) },
                    onClick = {
                        onTimerMinutesChange(min)
                        onTimerDropdownExpandedChange(false)
                    },
                )
            }
        }
    }
}

@Composable
private fun BinauralStatusSection(
    remainingSeconds: Long,
    isPlaying: Boolean,
    timerEndTimeMillis: Long,
    labelColor: Color,
) {
    val statusText =
        when {
            timerEndTimeMillis > 0L && isPlaying && remainingSeconds > 0 -> {
                val min = (remainingSeconds / 60).toInt()
                val sec = (remainingSeconds % 60).toInt()
                stringResource(R.string.status_remaining, min, sec)
            }
            isPlaying -> stringResource(R.string.status_playing)
            else -> stringResource(R.string.status_stopped)
        }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(R.string.status_label),
            style = MaterialTheme.typography.labelMedium,
            color = Color(0xFFB0B0B0),
        )
        Text(
            text = statusText,
            style = MaterialTheme.typography.titleMedium,
            color = labelColor,
        )
    }
}
