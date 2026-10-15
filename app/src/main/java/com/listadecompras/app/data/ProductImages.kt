package com.listadecompras.app.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.util.UUID

/** Only opaque, app-owned JPEG names are stored in ShoppingItem or exported in backups. */
object ProductImages {
    private val filePattern = Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\.jpg")
    fun validName(name: String): Boolean = name.isEmpty() || filePattern.matches(name)
    private fun directory(context: Context): File = File(context.filesDir, "product_images").apply { mkdirs() }
    fun file(context: Context, name: String): File? = name.takeIf { it.isNotEmpty() && validName(it) }
        ?.let { File(directory(context), it) }
    fun cameraFile(context: Context): File = File(context.cacheDir, "capture_${UUID.randomUUID()}.jpg")
    fun cameraUri(context: Context, target: File): Uri = FileProvider.getUriForFile(
        context, "${context.packageName}.photos", target)

    /** Downsample first so a high-resolution camera image never needs full decoding. */
    fun importUri(context: Context, source: Uri): String {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        // When inJustDecodeBounds is true, decodeStream returns null by design.
        // The dimensions in bounds, not the returned Bitmap, determine validity.
        val previewStream = resolver.openInputStream(source) ?: error("Imagem indisponível")
        previewStream.use { BitmapFactory.decodeStream(it, null, bounds) }
        require(bounds.outWidth in 1..16000 && bounds.outHeight in 1..16000) { "Dimensões de imagem inválidas." }
        var sample = 1
        while (bounds.outWidth / sample > 1400 || bounds.outHeight / sample > 1400) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val bitmap = resolver.openInputStream(source)?.use { BitmapFactory.decodeStream(it, null, options) }
            ?: error("Não foi possível ler esta foto.")
        val orientation = runCatching {
            resolver.openInputStream(source)?.use {
                ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            }
        }.getOrNull() ?: ExifInterface.ORIENTATION_NORMAL
        val transform = Matrix().apply {
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> postRotate(270f)
            }
        }
        val rotated = if (!transform.isIdentity) Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, transform, true) else bitmap
        val scaled = if (rotated.width > 1280 || rotated.height > 1280) {
            val ratio = 1280f / maxOf(rotated.width, rotated.height)
            Bitmap.createScaledBitmap(rotated, (rotated.width * ratio).toInt().coerceAtLeast(1),
                (rotated.height * ratio).toInt().coerceAtLeast(1), true)
        } else rotated
        val name = "${UUID.randomUUID()}.jpg"
        val destination = requireNotNull(file(context, name))
        try {
            destination.outputStream().use { require(scaled.compress(Bitmap.CompressFormat.JPEG, 78, it)) }
            require(destination.length() in 1..1_000_000) { "A imagem é grande demais." }
        } catch (e: Exception) { destination.delete(); throw e }
        finally {
            if (scaled !== rotated) scaled.recycle()
            if (rotated !== bitmap) rotated.recycle()
            bitmap.recycle()
        }
        return name
    }

    fun verifyStoredJpeg(bytes: ByteArray): Boolean {
        if (bytes.size !in 4..1_000_000 || bytes[0] != 0xFF.toByte() || bytes[1] != 0xD8.toByte()) return false
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        return options.outWidth in 1..1280 && options.outHeight in 1..1280
    }

    fun decodeThumbnail(context: Context, name: String): Bitmap? {
        val imageFile = file(context, name) ?: return null
        if (!imageFile.isFile) return null
        return BitmapFactory.decodeFile(imageFile.absolutePath, BitmapFactory.Options().apply { inSampleSize = 8 })
    }

    fun decode(context: Context, name: String): Bitmap? {
        val imageFile = file(context, name) ?: return null
        if (!imageFile.isFile) return null
        return BitmapFactory.decodeFile(imageFile.absolutePath)
    }
}
