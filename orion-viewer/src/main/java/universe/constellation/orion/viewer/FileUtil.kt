package universe.constellation.orion.viewer

import universe.constellation.orion.viewer.document.Document
import universe.constellation.orion.viewer.pdf.PdfDocument
import java.io.File

object FileUtil {



    @JvmStatic
    @Throws(Exception::class)
    fun openFile(file: File): Document {
        val absolutePath = file.absolutePath
        try {
            return PdfDocument(absolutePath)

        } catch (e: Exception) {
            throw RuntimeException(
                "Error during file opening `${file.name}`: " + e.message + "\n" +
                        "(File size: ${file.beautifiedFileSize()}, file path: ${absolutePath})", e
            )
        }
    }

    private fun File.beautifiedFileSize(): String {
        return length().beautifyFileSize()
    }

    @JvmStatic
    fun Long.beautifyFileSize(): String {
        if (this < 1024) {
            return "$this bytes"
        }

        var size = this / 1024.0
        if (size < 1024) {
            return String.format("%.2f KB", size)
        }

        size /= 1024.0
        if (size < 1024) {
            return String.format("%.2f MB", size)
        }

        size /= 1024.0
        return String.format("%.2f GB", size)
    }

}
