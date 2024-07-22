package universe.constellation.orion.viewer.filemanager

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import universe.constellation.orion.viewer.OrionViewerActivity
import universe.constellation.orion.viewer.R
import universe.constellation.orion.viewer.log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

open class OrionFileManagerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.title = null
        copyFileFromAssetsToInternal(this, "Android.pdf")
        startActivity(Intent(this@OrionFileManagerActivity, OrionViewerActivity::class.java))
    }
    open fun copyFileFromAssetsToInternal(context: Context, assetFileName: String): String? {
        val inputStream: InputStream
        val outputStream: OutputStream
        try {
            inputStream = context.assets.open(assetFileName)
            val outputFile = File(context.filesDir, assetFileName)
            outputStream = FileOutputStream(outputFile)
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            return outputFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }

}