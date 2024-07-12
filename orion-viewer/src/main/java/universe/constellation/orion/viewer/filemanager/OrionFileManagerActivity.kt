package universe.constellation.orion.viewer.filemanager

import android.content.Intent
import android.os.Bundle
import androidx.viewpager.widget.ViewPager
import universe.constellation.orion.viewer.OrionViewerActivity
import universe.constellation.orion.viewer.R
import universe.constellation.orion.viewer.log
import java.io.File

open class OrionFileManagerActivity : OrionFileManagerActivityBase(
    true,
    FileChooserAdapter.DEFAULT_FILTER,
    true
) {
    companion object {
        const val OPEN_RECENTS_TAB = "OPEN_RECENTS_FILE"
        const val LAST_OPENED_DIRECTORY = "LAST_OPENED_DIR"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.title = null
        copyFileFromAssetsToInternal(this, "Android.pdf")
        startActivity(Intent(this@OrionFileManagerActivity, OrionViewerActivity::class.java))
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        log("OrionFileManager: On new intent $intent")
        val book = File("${filesDir.path}/Android.pdf")
        if (book.exists()) {
            log("Opening recent book $book")
            openFile(book)
        }

//        if (intent.getBooleanExtra(OPEN_RECENTS_TAB, false)) {
//            findViewById<ViewPager>(R.id.viewpager).setCurrentItem(1, false)
//            return
//        }
//
//        val dontStartRecent = intent.getBooleanExtra(DONT_OPEN_RECENT_FILE, false)
//        if (!dontStartRecent && globalOptions.isOpenRecentBook) {
//            if (!globalOptions.recentFiles.isEmpty()) {
//                val entry = globalOptions.recentFiles[0]
//                val book = File(entry.path)
//                if (book.exists()) {
//                    log("Opening recent book $book")
//                    openFile(book)
//                }
//            }
//        }
    }
}