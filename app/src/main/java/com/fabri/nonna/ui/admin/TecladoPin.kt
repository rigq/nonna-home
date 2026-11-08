package com.fabri.nonna.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TecladoPin(titulo: String, mensaje: String?, onCompleto: (String) -> Unit, onCancelar: (() -> Unit)?) {
    var pin by remember { mutableStateOf("") }
    Column(
        Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        Text(titulo, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("●".repeat(pin.length).ifEmpty { " " }, fontSize = 40.sp)
        if (mensaje != null) Text(mensaje, fontSize = 20.sp)
        listOf(listOf("1", "2", "3"), listOf("4", "5", "6"), listOf("7", "8", "9"), listOf("⌫", "0", "OK")).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                fila.forEach { t ->
                    Button(onClick = {
                        when (t) {
                            "⌫" -> pin = pin.dropLast(1)
                            "OK" -> { onCompleto(pin); pin = "" }
                            else -> if (pin.length < 6) pin += t
                        }
                    }, modifier = Modifier.size(88.dp)) { Text(t, fontSize = 28.sp) }
                }
            }
        }
        if (onCancelar != null) TextButton(onClick = onCancelar) { Text("Cancelar", fontSize = 22.sp) }
    }
}
