package com.dev.lbsuhack.feature.activities.ui.details

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.navigation.ActivityLink
import com.dev.lbsuhack.feature.activities.presentation.details.ActivityQrCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ActivityQrCard(activityId: String, title: String) {
    val context = LocalContext.current
    val link = remember(activityId) { ActivityLink.create(activityId) }
    val chooserTitle = stringResource(R.string.activity_qr_share)
    val bitmap by produceState<ImageBitmap?>(null, link) {
        value = withContext(Dispatchers.Default) {
            val matrix = ActivityQrCode.encode(link)
            val pixels = IntArray(matrix.width * matrix.height) { index ->
                if (matrix[index % matrix.width, index / matrix.width]) {
                    android.graphics.Color.BLACK
                } else {
                    android.graphics.Color.WHITE
                }
            }
            Bitmap.createBitmap(pixels, matrix.width, matrix.height, Bitmap.Config.ARGB_8888)
                .asImageBitmap()
        }
    }
    Surface(shape = MaterialTheme.shapes.extraLarge, color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.activity_qr_title), style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.activity_qr_hint), style = MaterialTheme.typography.bodyMedium)
            bitmap?.let {
                Image(
                    bitmap = it,
                    contentDescription = stringResource(R.string.activity_qr_description, title),
                    modifier = Modifier.sizeIn(maxWidth = 280.dp, maxHeight = 280.dp).fillMaxWidth(),
                    filterQuality = FilterQuality.None,
                )
            }
            OutlinedButton(onClick = {
                context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, "$title\n$link")
                }, chooserTitle))
            }) {
                Text(chooserTitle)
            }
        }
    }
}
