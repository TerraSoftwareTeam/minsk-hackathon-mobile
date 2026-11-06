package com.dev.lbsuhack.feature.activities.presentation.details

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter

object ActivityQrCode {
    fun encode(link: String): BitMatrix = QRCodeWriter().encode(
        link,
        BarcodeFormat.QR_CODE,
        512,
        512,
        mapOf(EncodeHintType.MARGIN to 4),
    )
}
