package universe.constellation.orion.viewer

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Debug
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
import kotlinx.coroutines.*
import universe.constellation.orion.viewer.layout.SimpleLayoutStrategy
import universe.constellation.orion.viewer.prefs.OrionApplication
import universe.constellation.orion.viewer.prefs.initalizer
import universe.constellation.orion.viewer.selection.NewTouchProcessor
import universe.constellation.orion.viewer.selection.NewTouchProcessorWithScale
import universe.constellation.orion.viewer.selection.SelectionAutomata
import universe.constellation.orion.viewer.view.FullScene
import universe.constellation.orion.viewer.view.OrionDrawScene
import universe.constellation.orion.viewer.view.StatusBar
import java.io.File
import java.util.concurrent.Executors

class OrionViewerActivity : AppCompatActivity() {


    @SuppressLint("MissingSuperCall")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pdf_activity)

        if (savedInstanceState == null) {
            val pdfFragment = PdfFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, pdfFragment)
                .commit()
        }
    }

}
