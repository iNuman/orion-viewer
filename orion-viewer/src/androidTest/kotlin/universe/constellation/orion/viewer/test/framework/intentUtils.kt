package universe.constellation.orion.viewer.test.framework

import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.webkit.MimeTypeMap
import universe.constellation.orion.viewer.BuildConfig
import universe.constellation.orion.viewer.OrionViewerActivity
import universe.constellation.orion.viewer.prefs.GlobalOptions

fun createTestViewerIntent(body: Intent.() -> Unit): Intent {
    return Intent(Intent.ACTION_VIEW).apply {
        setClassName(
            BuildConfig.APPLICATION_ID,
            OrionViewerActivity::class.qualifiedName!!
        )
        addCategory(Intent.CATEGORY_DEFAULT)
        body()
        if (!hasExtra(GlobalOptions.SHOW_TAP_HELP)) {
            putExtra(GlobalOptions.SHOW_TAP_HELP, false)
        }
        putExtra(GlobalOptions.OPEN_AS_TEMP_BOOK, true)
        putExtra(GlobalOptions.TEST_FLAG, true)
    }
}


fun BookFile.toOpenIntentWithNewUI(): Intent {
    return toOpenIntent {
        putExtra(GlobalOptions.OLD_UI, false)
    }
}