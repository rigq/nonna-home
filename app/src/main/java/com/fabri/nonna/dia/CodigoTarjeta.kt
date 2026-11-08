package com.fabri.nonna.dia

import com.fabri.nonna.datos.TipoTarjeta
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

object CodigoTarjeta {
    fun matriz(numero: String, tipo: TipoTarjeta, ancho: Int, alto: Int): BitMatrix? {
        val n = numero.trim()
        if (n.isEmpty()) return null
        val formato = when (tipo) {
            TipoTarjeta.QR -> BarcodeFormat.QR_CODE
            TipoTarjeta.BARRAS -> BarcodeFormat.CODE_128
            TipoTarjeta.APP -> return null
        }
        return runCatching {
            MultiFormatWriter().encode(n, formato, ancho, alto, mapOf(EncodeHintType.MARGIN to 2))
        }.getOrNull()
    }
}
