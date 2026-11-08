package com.fabri.nonna.datos

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File

private val Context.almacen by preferencesDataStore("nonna")

class Repositorio(private val ctx: Context) {
    private val kFamiliares = stringPreferencesKey("familiares")
    private val kAjustes = stringPreferencesKey("ajustes")
    private val kTiempo = stringPreferencesKey("tiempo")

    private val prefs = ctx.almacen.data.catch { e ->
        Log.e("Repositorio", "Error leyendo DataStore", e); emit(emptyPreferences())
    }

    val familiares: Flow<List<Familiar>> = prefs.map { p -> Codec.familiares(p[kFamiliares]).sortedBy { it.orden } }
    val ajustes: Flow<Ajustes> = prefs.map { Codec.ajustes(it[kAjustes]) }
    val tiempo: Flow<TiempoCache?> = prefs.map { Codec.tiempo(it[kTiempo]) }

    suspend fun guardarFamiliares(l: List<Familiar>) {
        val ordenados = l.mapIndexed { i, f -> f.copy(orden = i) }
        ctx.almacen.edit { it[kFamiliares] = Codec.texto(ordenados) }
    }
    suspend fun guardarAjustes(a: Ajustes) { ctx.almacen.edit { it[kAjustes] = Codec.texto(a) } }
    suspend fun guardarTiempo(t: TiempoCache) { ctx.almacen.edit { it[kTiempo] = Codec.texto(t) } }

    /**
     * Copia la foto elegida dentro de la app, reducida a 512 px como máximo, con un nombre nuevo
     * (para que la interfaz la refresque) y borra la [anterior]. Devuelve la ruta, o null si falla.
     */
    suspend fun copiarFoto(uri: Uri, id: String, anterior: String? = null): String? = withContext(Dispatchers.IO) {
        runCatching {
            val original = ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
                ?: return@runCatching null
            val escala = 512f / maxOf(original.width, original.height)
            val bmp = if (escala < 1f) Bitmap.createScaledBitmap(
                original, (original.width * escala).toInt(), (original.height * escala).toInt(), true
            ) else original
            val dir = File(ctx.filesDir, "fotos").apply { mkdirs() }
            val f = File(dir, "${id}_${System.currentTimeMillis()}.jpg")
            f.outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 85, it) }
            anterior?.let { File(it).takeIf { a -> a != f }?.delete() }
            f.absolutePath
        }.getOrNull()
    }
}
