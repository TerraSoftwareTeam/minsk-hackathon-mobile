package com.dev.lbsuhack.feature.activities.presentation

import com.dev.lbsuhack.feature.activities.navigation.ActivityLink
import com.dev.lbsuhack.feature.activities.presentation.details.ActivityQrCode
import com.google.zxing.BinaryBitmap
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.junit.Assert.assertEquals
import org.junit.Test

class ActivityQrCodeTest {
    @Test
    fun generatedQrDecodesToTheActivityLink() {
        val link = ActivityLink.create("act-4")
        val matrix = ActivityQrCode.encode(link)
        val pixels = IntArray(matrix.width * matrix.height) { index ->
            if (matrix[index % matrix.width, index / matrix.width]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
        }
        val bitmap = BinaryBitmap(HybridBinarizer(RGBLuminanceSource(matrix.width, matrix.height, pixels)))
        val decoded = QRCodeReader().decode(bitmap)

        assertEquals(link, decoded.text)
        assertEquals("act-4", ActivityLink.parse(decoded.text))
    }
}
