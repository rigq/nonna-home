package com.fabri.nonna.ui.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.fabri.nonna.datos.inicial

@Composable
fun FotoFamiliar(nombre: String, rutaFoto: String?, tamano: Dp) {
    val bmp = remember(rutaFoto) {
        rutaFoto?.let { runCatching { BitmapFactory.decodeFile(it) }.getOrNull() }
    }
    val m = Modifier.size(tamano).clip(CircleShape)
    if (bmp != null) {
        Image(bmp.asImageBitmap(), contentDescription = nombre, modifier = m, contentScale = ContentScale.Crop)
    } else {
        Box(m.background(Color(0xFF90CAF9)), contentAlignment = Alignment.Center) {
            Text(inicial(nombre), fontSize = (tamano.value / 2).sp, fontWeight = FontWeight.Bold)
        }
    }
}
