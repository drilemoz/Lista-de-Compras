package com.listadecompras.app.data

import android.content.Context
import com.listadecompras.app.model.ShoppingItem
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/** Portable ZIP: backup.json + local product photos (never absolute paths). */
object ProductBackupArchive {
    private const val MAX_PHOTOS = 2000
    private const val MAX_PHOTO_SIZE = 1_000_000
    private const val MAX_ARCHIVE_UNCOMPRESSED = 60_000_000L
    private const val MAX_JSON_SIZE = 15_000_000

    data class Imported(val json: String, val images: Map<String, ByteArray>)

    fun write(context: Context, json: String, products: List<ShoppingItem>, stream: OutputStream) {
        val names = products.map { it.imageFile }.filter { it.isNotBlank() }.distinct().sorted()
        require(names.size <= MAX_PHOTOS) { "Número de imagens excedido." }
        ZipOutputStream(stream).use { zip ->
            zip.putNextEntry(ZipEntry("backup.json"))
            zip.write(json.toByteArray(Charsets.UTF_8))
            zip.closeEntry()
            for (name in names) {
                require(ProductImages.validName(name)) { "Referência de foto inválida." }
                val image = requireNotNull(ProductImages.file(context, name))
                require(image.isFile && image.length() in 1..MAX_PHOTO_SIZE.toLong()) {
                    "Imagem $name não encontrada ou muito grande. Corrija a foto antes de exportar."
                }
                zip.putNextEntry(ZipEntry("images/$name"))
                image.inputStream().use { it.copyTo(zip) }
                zip.closeEntry()
            }
        }
    }

    fun read(stream: InputStream): Imported {
        val images = linkedMapOf<String, ByteArray>()
        var json: String? = null
        var total = 0L
        ZipInputStream(stream).use { zip ->
            while (true) {
                val entry = zip.nextEntry ?: break
                require(!entry.isDirectory) { "Pasta inesperada dentro do backup." }
                val isJson = entry.name == "backup.json"
                val name = if (entry.name.startsWith("images/")) entry.name.removePrefix("images/") else ""
                require(isJson || (ProductImages.validName(name) && name.isNotEmpty() && entry.name == "images/$name")) {
                    "Arquivo inesperado no ZIP."
                }
                val limit = if (isJson) MAX_JSON_SIZE else MAX_PHOTO_SIZE
                val out = ByteArrayOutputStream()
                val block = ByteArray(8192)
                while (true) {
                    val size = zip.read(block)
                    if (size < 0) break
                    total += size
                    require(total <= MAX_ARCHIVE_UNCOMPRESSED && out.size() + size <= limit) { "Backup excede os limites de tamanho." }
                    out.write(block, 0, size)
                }
                if (isJson) {
                    require(json == null) { "JSON duplicado no backup." }
                    json = out.toString(Charsets.UTF_8.name())
                } else {
                    require(images.size < MAX_PHOTOS && name !in images) { "Imagem duplicada ou excesso de imagens." }
                    val bytes = out.toByteArray()
                    require(bytes.size >= 4 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xD8.toByte()) { "Imagem JPEG inválida." }
                    images[name] = bytes
                }
                zip.closeEntry()
            }
        }
        return Imported(requireNotNull(json) { "backup.json ausente no ZIP." }, images)
    }

    fun referencedImages(products: List<ShoppingItem>): Set<String> =
        products.mapNotNull { it.imageFile.takeIf { name -> name.isNotBlank() } }.toSet()

    /** Validate complete coverage before applying any imported list changes. */
    fun verify(imported: Imported, required: Set<String>) {
        require(required.all { it in imported.images }) { "Backup incompleto: uma ou mais fotos estão ausentes." }
    }

    fun restoreImages(context: Context, images: Map<String, ByteArray>) {
        images.forEach { (name, bytes) ->
            val file = requireNotNull(ProductImages.file(context, name))
            require(ProductImages.verifyStoredJpeg(bytes)) { "Imagem JPEG inválida ou com dimensões excessivas." }
            if (file.exists()) {
                require(file.readBytes().contentEquals(bytes)) { "Conflito de imagem existente. Importação não realizada." }
            }
        }
        images.forEach { (name, bytes) ->
            val file = requireNotNull(ProductImages.file(context, name))
            if (!file.exists()) file.writeBytes(bytes)
        }
    }
}
