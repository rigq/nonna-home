package com.fabri.nonna.ui.dia

import android.app.Activity
import android.graphics.Bitmap
import android.view.WindowManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.fabri.nonna.datos.TipoTarjeta
import com.fabri.nonna.dia.CodigoTarjeta
import com.fabri.nonna.ui.components.BotonVolver
import com.fabri.nonna.ui.theme.Medidas
import android.graphics.Color as AColor

@Composable
fun TarjetaDiaScreen(numero: String, tipo: TipoTarjeta, onVolver: () -> Unit) {
    val activity = LocalContext.current as Activity
    DisposableEffect(Unit) {
        val w = activity.window
        val antes = w.attributes.screenBrightness
        w.attributes = w.attributes.apply { screenBrightness = 1f }
        w.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            w.attributes = w.attributes.apply { screenBrightness = antes }
            w.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    val (ancho, alto) = if (tipo == TipoTarjeta.BARRAS) 1000 to 350 else 800 to 800
    val bmp = remember(numero, tipo) {
        CodigoTarjeta.matriz(numero, tipo, ancho, alto)?.let { m ->
            val pixeles = IntArray(m.width * m.height) { i ->
                if (m[i % m.width, i / m.width]) AColor.BLACK else AColor.WHITE
            }
            Bitmap.createBitmap(pixeles, m.width, m.height, Bitmap.Config.RGB_565)
        }
    }
    Column(
        Modifier.fillMaxSize().background(Color.White).safeDrawingPadding().padding(Medidas.separacion),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Enseña esto en la caja", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (bmp != null) Image(bmp.asImageBitmap(), contentDescription = "Tarjeta DIA", modifier = Modifier.fillMaxWidth())
            else Text("Falta configurar la tarjeta", fontSize = 28.sp, color = Color.Black)
        }
        BotonVolver(onVolver)
    }
}
