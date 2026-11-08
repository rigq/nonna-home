package com.fabri.nonna.acciones

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract

object WhatsAppContactos {
    const val MIME_VIDEO = "vnd.android.cursor.item/vnd.com.whatsapp.video.call"

    fun tienePermiso(ctx: Context) =
        ctx.checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

    /** Sin CALL_PHONE, WhatsApp rechaza el intent de videollamada (SecurityException) y se cae al chat. */
    fun tienePermisoLlamar(ctx: Context) =
        ctx.checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED

    val PERMISOS = arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE)

    fun tienePermisos(ctx: Context) = tienePermiso(ctx) && tienePermisoLlamar(ctx)

    fun buscarVideollamada(ctx: Context, telefono: String): Uri? {
        if (!tienePermiso(ctx)) return null
        val objetivo = Telefono.normalizar(telefono)
        if (objetivo.isEmpty()) return null
        ctx.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            arrayOf(ContactsContract.Data._ID, ContactsContract.Data.DATA1),
            "${ContactsContract.Data.MIMETYPE} = ?", arrayOf(MIME_VIDEO), null
        )?.use { c ->
            while (c.moveToNext()) {
                val data1 = c.getString(1) ?: continue
                val numero = data1.substringBefore("@") // formato confirmado en fase 0: 34XXXXXXXXX@s.whatsapp.net
                if (Telefono.normalizar(numero) == objetivo) {
                    return ContentUris.withAppendedId(ContactsContract.Data.CONTENT_URI, c.getLong(0))
                }
            }
        }
        return null
    }

    fun nombreEnAgenda(ctx: Context, telefono: String): String? {
        if (!tienePermiso(ctx) || Telefono.normalizar(telefono).isEmpty()) return null
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(telefono))
        return try {
            ctx.contentResolver.query(uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null)
                ?.use { if (it.moveToFirst()) it.getString(0) else null }
        } catch (e: Exception) { null }
    }
}
