package com.mikokernel.ui.component.choosekmidialog

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mikokernel.R
import com.mikokernel.ui.component.material.SegmentedColumn
import com.mikokernel.ui.component.material.SegmentedRadioItem
import com.mikokernel.ui.util.getCurrentKmi
import com.mikokernel.ui.util.getSupportedKmis
import com.mikokernel.ui.util.getSupportedKmisFromAssets

@Composable
fun ChooseKmiDialogMaterial(
    show: Boolean,
    onDismissRequest: () -> Unit,
    onSelected: (String?) -> Unit
) {
    if (!show) return

    val currentKmi by produceState(initialValue = "") {
        value = getCurrentKmi()
    }

    val supportedKMIs by produceState(initialValue = emptyList()) {
        val fromKsud = getSupportedKmis()
        val fromAssets = getSupportedKmisFromAssets()
        value = (fromKsud + fromAssets).filter { it.isNotBlank() }.distinct()
    }

    val displayKMIs = remember(supportedKMIs, currentKmi) {
        if (currentKmi.isNotBlank() && currentKmi !in supportedKMIs) {
            listOf(currentKmi) + supportedKMIs
        } else {
            supportedKMIs
        }
    }

    val selectedKmi = remember(displayKMIs, currentKmi) {
        mutableStateOf(currentKmi.takeIf { it in displayKMIs } ?: displayKMIs.firstOrNull() ?: "")
    }

    AlertDialog(
        onDismissRequest = {
            onDismissRequest()
            selectedKmi.value = currentKmi
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSelected(selectedKmi.value)
                    onDismissRequest()
                },
                enabled = selectedKmi.value.isNotBlank() && displayKMIs.contains(selectedKmi.value)
            ) {
                Text(stringResource(id = R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismissRequest()
                selectedKmi.value = currentKmi
            }) {
                Text(stringResource(id = android.R.string.cancel))
            }
        },
        title = {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                text = stringResource(R.string.select_kmi),
                textAlign = TextAlign.Center
            )
        },
        text = {
            SegmentedColumn(
                content = displayKMIs.map { kmi ->
                    {
                        SegmentedRadioItem(
                            title = kmi,
                            summary = if (kmi == currentKmi) stringResource(R.string.current_device_kmi) else null,
                            selected = selectedKmi.value == kmi,
                            onClick = { selectedKmi.value = kmi }
                        )
                    }
                }
            )
        }
    )
}
