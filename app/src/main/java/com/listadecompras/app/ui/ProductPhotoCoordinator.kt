package com.listadecompras.app.ui

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.listadecompras.app.data.ProductImages
import java.io.File

/** Activity-result launchers must be registered before the activity reaches STARTED. */
internal class ProductPhotoCoordinator(
    private val activity: AppCompatActivity,
    private val fallback: ((String) -> Unit)? = null
) {
    private var consumer: ((String) -> Unit)? = null
    private var capture: File? = null
    private val gallery = activity.registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) import(uri) else consumer = null
    }
    private val camera = activity.registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val file = capture
        if (ok && file != null) import(ProductImages.cameraUri(activity, file)) else consumer = null
        file?.delete()
        capture = null
    }

    fun saveState(state: Bundle) { state.putString("productCameraCapture", capture?.absolutePath) }
    fun restoreState(state: Bundle?) {
        val path = state?.getString("productCameraCapture") ?: return
        val file = File(path)
        if (file.parentFile?.canonicalPath == activity.cacheDir.canonicalPath &&
            file.name.startsWith("capture_") && file.name.endsWith(".jpg")) capture = file
    }

    fun request(useCamera: Boolean, onSelected: (String) -> Unit) {
        consumer = onSelected
        if (!useCamera) { gallery.launch("image/*"); return }
        val file = ProductImages.cameraFile(activity)
        capture = file
        runCatching { camera.launch(ProductImages.cameraUri(activity, file)) }
            .onFailure { file.delete(); capture = null; consumer = null; error("Não foi possível abrir a câmera") }
    }

    private fun import(uri: Uri) {
        runCatching { ProductImages.importUri(activity, uri) }
            .onSuccess { (consumer ?: fallback)?.invoke(it) }
            .onFailure { error(it.message ?: "Não foi possível importar esta imagem") }
        consumer = null
    }
    private fun error(message: String) = Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
}
