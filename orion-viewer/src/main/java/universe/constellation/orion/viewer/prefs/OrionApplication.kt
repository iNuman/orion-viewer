package universe.constellation.orion.viewer.prefs

import android.app.Application
import universe.constellation.orion.viewer.LastPageInfo
import kotlin.properties.Delegates

class OrionApplication : Application() {


    val options: GlobalOptions by lazy {
        GlobalOptions(this, androidx.preference.PreferenceManager.getDefaultSharedPreferences(this))
    }

    var tempOptions: TemporaryOptions? = null
        private set

    var currentBookParameters: LastPageInfo? = null

    override fun onCreate() {
        instance = this
        super.onCreate()
    }

    fun onNewBook(fileName: String) {
        tempOptions = TemporaryOptions().also { it.openedFile = fileName }
    }

    fun destroyMainActivity() {
        currentBookParameters = null
    }



    companion object {
        var instance: OrionApplication by Delegates.notNull()
            private set

    }
}
