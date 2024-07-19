package universe.constellation.orion.viewer.filemanager

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.widget.ListView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager.widget.ViewPager
import com.google.android.material.color.MaterialColors
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import universe.constellation.orion.viewer.OrionBaseActivity
import universe.constellation.orion.viewer.OrionViewerActivity
import universe.constellation.orion.viewer.R
import universe.constellation.orion.viewer.getVectorDrawable
import universe.constellation.orion.viewer.log
import java.io.File
import java.io.FileOutputStream
import java.io.FilenameFilter
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


abstract class OrionFileManagerActivityBase @JvmOverloads constructor() : OrionBaseActivity() {


    var prefs: SharedPreferences? = null
        private set


    @SuppressLint("MissingSuperCall")
    override fun onCreate(savedInstanceState: Bundle?) {
        onOrionCreate(savedInstanceState, R.layout.file_manager, true)
        log("Creating file manager")

        prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(applicationContext)
        onNewIntent(intent)
    }




    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()

    }

    fun openFile(uri: Uri, isFromSystemFM: Boolean = false) {
        log("Opening new book: $uri")

        startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                setClass(applicationContext, OrionViewerActivity::class.java)
                data = uri
                addCategory(Intent.CATEGORY_DEFAULT)
                putExtra(SYSTEM_FILE_MANAGER, isFromSystemFM)
            }
        )
    }

    open fun openFile(file: File) {
        openFile(Uri.fromFile(file))
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


    companion object {
        const val DONT_OPEN_RECENT_FILE = "DONT_OPEN_RECENT_FILE"
        const val SYSTEM_FILE_MANAGER = "SYSTEM_FILE_MANAGER"
    }
}


