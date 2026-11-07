package com.dev.lbsuhack.feature.activities.ui.component

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.dev.lbsuhack.feature.activities.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun ActivityPhoto(
    photoUri: String,
    title: String,
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
) {
    val resolver = LocalContext.current.contentResolver
    val remoteLoader = LocalActivityPhotoLoader.current
    val cached = remember(photoUri) { ActivityPhotoDecoder.cached(photoUri) }
    val result by produceState<Result<Bitmap>?>(cached?.let { Result.success(it) }, photoUri, resolver) {
        value = cached?.let { Result.success(it) }
        value = withContext(Dispatchers.IO) {
            try {
                val uri = Uri.parse(photoUri)
                val bitmap = if (uri.scheme == "https" || uri.scheme == "http") {
                    ActivityPhotoDecoder.decodeRemote(photoUri, remoteLoader(photoUri))
                } else {
                    ActivityPhotoDecoder.decodeLocal(photoUri, resolver, uri)
                }
                Result.success(bitmap)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                Result.failure(error)
            }
        }
    }
    Box(
        modifier = modifier.clip(shape).background(MaterialTheme.colorScheme.surfaceContainerLow),
        contentAlignment = Alignment.Center,
    ) {
        val bitmap = result?.getOrNull()
        when {
            bitmap != null -> Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = stringResource(R.string.activity_photo_description, title),
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )

            result == null -> CircularProgressIndicator()
            else -> Text(stringResource(R.string.activity_photo_error), color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
