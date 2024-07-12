package universe.constellation.orion.viewer.formats

import android.content.ContentResolver
import android.content.Intent
import android.webkit.MimeTypeMap
import java.io.File
import java.util.Locale


enum class FileFormats(val extensions: List<String>, vararg val mimeTypes: String) {
    DJVU(
        listOf("djvu", "djv"),
        "image/vnd.djvu",
        "image/x-djvu",
        "image/djvu",
        "application/djvu",
        "application/vnd.djvu"
    ),
    PDF(listOf("pdf"), "application/pdf"),

    XPS(
        listOf("xps", "oxps"),
        "application/vnd.ms-xpsdocument",
        "application/oxps",
        "application/xps"
    ),

    TIFF(listOf("tiff", "tif"), "image/tiff", "image/x-tiff"),
    CBZ(listOf("cbz"), "application/vnd.comicbook+zip", "application/x-cbz"),
    CBR(listOf("cbr"), "application/vnd.comicbook-rar", "application/x-cbr"),
    //CB7(listOf("cb7"), "application/x-cb7"),
    CBT(listOf("cbt"),  "application/x-cbt"),

    PNG(listOf("png"), "image/png"),
    JPEG(listOf("jpg", "jpeg"), "image/jpeg", "image/pjpeg");

    companion object {
        val supportedMimeTypes by lazy {
            FileFormats.entries.flatMap { it.mimeTypes.toList() }.toTypedArray()
        }

        private val supportedExtensions by lazy {
            FileFormats.entries.flatMap { it.extensions }.toSet()
        }

        private val extToMimeType by lazy {
            entries.associate { it.extensions.first() to it.mimeTypes.first() }
        }

        private val mimeType2Extension by lazy {
            entries.associate { it.mimeTypes.first() to it.extensions.first() }
        }


        fun String?.isExplicit() = !this.isNullOrBlank() && !contains('*')

        fun getMimeTypeFromExtension(ext: String): String? {
            val extLC = ext.lowercase(Locale.getDefault())
            return FileFormats.extToMimeType[extLC] ?: MimeTypeMap.getSingleton().getMimeTypeFromExtension(extLC)
        }

        val String?.isSupportedFileExt: Boolean
            get() {
                if (this == null) return false
                return supportedExtensions.contains(this.lowercase(Locale.getDefault()))
            }

        val String?.isSupportedMimeType: Boolean
            get() {
                if (this == null) return false
                return mimeType2Extension.containsKey(this)
            }
    }
}

