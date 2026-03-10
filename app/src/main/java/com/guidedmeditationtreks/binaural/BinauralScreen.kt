package com.guidedmeditationtreks.binaural

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BinauralScreen(
    isPlaying: Boolean,
    onPlayRequested: (carrierHz: Float, beatHz: Float, isBinaural: Boolean) -> Unit,
    onStopRequested: () -> Unit,
    onRefreshAndRestart: (carrierHz: Float, beatHz: Float, isBinaural: Boolean) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val labelColor = Color(0xFFE8E8E8)
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedPlaceholderColor = Color(0xFFB0B0B0),
        unfocusedPlaceholderColor = Color(0xFFB0B0B0),
        focusedLabelColor = labelColor,
        unfocusedLabelColor = labelColor,
        cursorColor = Color.White,
        focusedBorderColor = MaterialTheme.colorScheme.outline,
        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
    )

    var carrierText by remember { mutableStateOf("200") }
    var beatText by remember { mutableStateOf("8") }
    var isBinaural by remember { mutableStateOf(true) }
    var carrierError by remember { mutableStateOf<String?>(null) }
    var beatError by remember { mutableStateOf<String?>(null) }
    var isDataChanged by remember { mutableStateOf(true) }

    fun validateCarrier(): Boolean {
        if (carrierText.isBlank()) {
            carrierError = "Carrier Frequency is required!"
            return false
        }
        return try {
            val v = carrierText.toFloat()
            when {
                v < 20 -> { carrierError = "Carrier Frequency must be greater than 20!"; false }
                v > 1200 -> { carrierError = "Carrier Frequency must be less than 1200!"; false }
                else -> { carrierError = null; true }
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
                    beatError = "Beat Frequency must be greater than $minBeat!"
                    false
                }
                v > 1200 -> { beatError = "Beat Frequency must be less than 1200!"; false }
                else -> { beatError = null; true }
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
        onRefreshAndRestart(c, b, isBinaural)
        isDataChanged = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
        val url = stringResource(R.string.url)
        val titleText = stringResource(R.string.gmt)
        val annotated = buildAnnotatedString {
            append(titleText)
            addStyle(
                style = SpanStyle(color = MaterialTheme.colorScheme.primary),
                start = 0,
                end = titleText.length
            )
        }
        ClickableText(
            text = annotated,
            style = MaterialTheme.typography.headlineSmall,
            onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            RadioButton(
                selected = isBinaural,
                onClick = {
                    isBinaural = true
                    isDataChanged = true
                }
            )
            Text(
                stringResource(R.string.binaural_radio),
                color = labelColor,
                style = MaterialTheme.typography.bodyLarge
            )
            RadioButton(
                selected = !isBinaural,
                onClick = {
                    if (!isBinaural) return@RadioButton
                    isBinaural = false
                    val b = beatText.toFloatOrNull()
                    if (b != null && b < 0.5f) beatText = "0.5"
                    isDataChanged = true
                }
            )
            Text(
                stringResource(R.string.isochronic_radio),
                color = labelColor,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(0.4f),
                horizontalAlignment = Alignment.CenterHorizontally
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
                    isError = carrierError != null,
                    supportingText = if (carrierError != null) { { Text(carrierError!!) } } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (!it.isFocused) tryRefreshAndRestart() },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                    colors = textFieldColors
                )
                Text(
                    text = if (isPlaying && !isDataChanged && carrierError == null) {
                        if (isBinaural) {
                            val c = carrierText.toFloatOrNull() ?: 0f
                            val b = beatText.toFloatOrNull() ?: 0f
                            "%.2f / %.2f".format(c + b / 2, c - b / 2)
                        } else {
                            carrierText
                        }
                    } else stringResource(R.string.not_applicable),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFB0B0B0)
                )
            }

            IconButton(
                onClick = {
                    focusManager.clearFocus()
                    if (isPlaying) {
                        onStopRequested()
                    } else {
                        if (!validateCarrier() || !validateBeat()) return@IconButton
                        val c = carrierText.toFloat()
                        val b = beatText.toFloat()
                        val minBeat = if (isBinaural) 0f else 0.5f
                        if (b < minBeat) return@IconButton
                        onPlayRequested(c, b, isBinaural)
                        isDataChanged = false
                    }
                },
                modifier = Modifier.size(64.dp)
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Stop" else "Play",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(0.4f),
                horizontalAlignment = Alignment.CenterHorizontally
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
                    isError = beatError != null,
                    supportingText = if (beatError != null) { { Text(beatError!!) } } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .onFocusChanged { if (!it.isFocused) tryRefreshAndRestart() },
                    textStyle = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
                    colors = textFieldColors
                )
                Text(
                    text = if (isPlaying && !isDataChanged && beatError == null) beatText else stringResource(R.string.not_applicable),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFB0B0B0)
                )
            }
        }
        }

        Text(
            text = "Build: ${BuildConfig.BUILD_TIME} · ${BuildConfig.GIT_SHA}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFAAAAAA),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .padding(bottom = 32.dp)
        )
    }
}
