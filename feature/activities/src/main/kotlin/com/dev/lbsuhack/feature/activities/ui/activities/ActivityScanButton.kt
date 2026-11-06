package com.dev.lbsuhack.feature.activities.ui.activities

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.navigation.ActivityLink
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

@Composable
fun ActivityScanButton(onOpenDetails: (String) -> Unit) {
    val context = LocalContext.current
    var scanning by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<Int?>(null) }
    val scanner = remember(context) {
        GmsBarcodeScanning.getClient(context, GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build())
    }
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        OutlinedButton(
            enabled = !scanning,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                scanning = true
                error = null
                scanner.startScan()
                    .addOnSuccessListener { barcode ->
                        scanning = false
                        val id = ActivityLink.parse(barcode.rawValue)
                        if (id == null) error = R.string.activity_qr_invalid else onOpenDetails(id)
                    }
                    .addOnCanceledListener { scanning = false }
                    .addOnFailureListener {
                        scanning = false
                        error = R.string.activity_qr_scan_error
                    }
            },
        ) {
            Icon(Icons.Outlined.QrCodeScanner, contentDescription = null)
            Text(stringResource(R.string.activity_qr_scan), modifier = Modifier.padding(start = 8.dp))
        }
        error?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
    }
}
