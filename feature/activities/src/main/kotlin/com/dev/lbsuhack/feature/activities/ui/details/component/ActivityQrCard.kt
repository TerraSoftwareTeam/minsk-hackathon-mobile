package com.dev.lbsuhack.feature.activities.ui.details.component

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.dev.lbsuhack.feature.activities.R
import com.dev.lbsuhack.feature.activities.navigation.ActivityLink
import com.dev.lbsuhack.feature.activities.presentation.details.ActivityQrCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun ActivityQrCard(activityId: String, title: String, modifier: Modifier = Modifier) {
    val link = remember(activityId) { ActivityLink.create(activityId) }
    val bitmap by produceState<ImageBitmap?>(null, link) {
        value = withContext(Dispatchers.Default) {
            runCatching {
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
            }.getOrNull()
        }
    }
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.activity_qr_title), style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(R.string.activity_qr_hint),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            val image = bitmap
            if (image == null) {
                Text(
                    text = stringResource(R.string.activity_qr_encode_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            } else {
                Image(
                    bitmap = image,
                    contentDescription = stringResource(R.string.activity_qr_description, title),
                    modifier = Modifier.sizeIn(maxWidth = 280.dp, maxHeight = 280.dp).fillMaxWidth(),
                    filterQuality = FilterQuality.None,
                )
            }
        }
    }
}
