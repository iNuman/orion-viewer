package universe.constellation.orion.viewer

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Debug
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.doOnLayout
import kotlinx.coroutines.*
import universe.constellation.orion.viewer.dialog.SearchDialog
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

    internal val subscriptionManager = SubscriptionManager()

    private var lastPageInfo: LastPageInfo? = null

    var controller: Controller? = null
        private set

    @JvmField
    var _isResumed: Boolean = false

    val selectionAutomata: SelectionAutomata by lazy {
        SelectionAutomata(this)
    }

    private var newTouchProcessor: NewTouchProcessor? = null

    lateinit var fullScene: FullScene
        private set

    val view: OrionDrawScene
        get() = fullScene.drawView

    private val statusBarHelper: StatusBar
        get() = fullScene.statusBarHelper


    val orionApplication: OrionApplication
        get() = applicationContext as OrionApplication


    val globalOptions
        get() = orionApplication.options

    @SuppressLint("MissingSuperCall")
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        log("Creating OrionViewerActivity...")
        setContentView(R.layout.main_view)

        val book = File("${filesDir.path}/Android.pdf")
        if (book.exists()) {
            log("Opening recent book $book")
            openFile(book)
        }

        val view = findViewById<OrionDrawScene>(R.id.view)

        fullScene = FullScene(
            findViewById<View>(R.id.orion_full_scene) as ViewGroup,
            view,
            findViewById<View>(R.id.orion_status_bar) as ViewGroup,
            this
        )
        fullScene.setDrawOffPage(true)
//        fullScene.setDrawOffPage(globalOptions.isDrawOffPage)

        newTouchProcessor = NewTouchProcessorWithScale(view, this)
        view.setOnTouchListener { _, event ->
            newTouchProcessor!!.onTouch(event)
        }

    }

    @Throws(Exception::class)
    private fun openFile(file: File) {
        log("Runtime.getRuntime().totalMemory(): ${Runtime.getRuntime().totalMemory()}")
        log("Debug.getNativeHeapSize(): ${Debug.getNativeHeapSize()}")
        log("openFileAndDestroyOldController")

        GlobalScope.launch(Dispatchers.Main) {
            log("Trying to open file: $file")
            val rootJob = Job()
            val executor = Executors.newSingleThreadExecutor().asCoroutineDispatcher()
            val newDocument = try {
                withContext(executor + rootJob) {
                    FileUtil.openFile(file)
                }
            } catch (e: Exception) {
                executor.close()
                return@launch
            }

            try {

                if (newDocument.pageCount == 0) {
                    newDocument.destroy()
                    return@launch
                }

                val layoutStrategy = SimpleLayoutStrategy.create()
                val controller1 = Controller(
                    this@OrionViewerActivity,
                    newDocument,
                    layoutStrategy,
                    rootJob,
                    context = executor
                )


                val lastPageInfo1 = loadBookParameters(rootJob, file)
                log("Read LastPageInfo for page ${lastPageInfo1.pageNumber}")
                lastPageInfo = lastPageInfo1
                orionApplication.currentBookParameters = lastPageInfo1

                controller = controller1
                bind(view, controller1)

                updateViewOnNewBook((newDocument.title?.takeIf { it.isNotBlank() }
                    ?: file.name.substringBeforeLast(".")))

                val drawView = fullScene.drawView
                controller1.init(lastPageInfo1, drawView.sceneWidth, drawView.sceneHeight)

                subscriptionManager.sendDocOpenedNotification(controller1)

                lastPageInfo1.totalPages = newDocument.pageCount
                orionApplication.onNewBook(file.name)
                doOnLayout(lastPageInfo1)
            } catch (e: Exception) {
                if (controller != null) {
                    destroyController()
                } else {
                    newDocument.destroy()
                }
            } finally {
//                orionApplication.idlingRes.free()
            }
        }
    }

    fun showWarning(warning: String) {
        Toast.makeText(this, warning, Toast.LENGTH_SHORT).show()
    }

    fun showFastMessage(stringId: Int) {
        showWarning(resources.getString(stringId))
    }

    fun showFastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private suspend fun loadBookParameters(
        rootJob: CompletableJob,
        bookFile: File,
    ): LastPageInfo {

        return withContext(Dispatchers.Default + rootJob) {
            loadBookParameters(
                this@OrionViewerActivity,
                bookFile.absolutePath,
                initalizer(globalOptions)
            )
        }
    }

    private fun bind(view: OrionDrawScene, controller: Controller) {
        this.controller = controller
        view.setDimensionAware(controller)
    }

    private fun updateViewOnNewBook(title: String?) {
        fullScene.onNewBook(title, controller!!.pageCount)
        supportActionBar?.title = title
    }

    public override fun onPause() {
        log("Orion: onPause")
        _isResumed = false
        super.onPause()
        statusBarHelper.onPause(this)
    }


    override fun onResume() {
        _isResumed = true
        super.onResume()
        updateBrightness()
        log("onResume")

        if (controller != null) {
            controller!!.processPendingEvents()
        }
        statusBarHelper.onResume(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        log("onDestroy")
        destroyController()
        orionApplication.destroyMainActivity()

    }


    fun doAction(actionCode: Int) {
        val action = Action.getAction(actionCode)
        doAction(action)
        log("Code action $actionCode")
    }


    internal fun doAction(action: Action) {
        action.doAction(controller, this, null)
    }


    private fun updateBrightness() {
        val params = window.attributes
        val oldBrightness = params.screenBrightness
        if (globalOptions.isCustomBrightness) {
            params.screenBrightness = globalOptions.brightness.toFloat() / 100
            window.attributes = params
        } else {
        if (oldBrightness >= 0) {
            params.screenBrightness = -1f
            window.attributes = params
        }
        }
    }

    fun textSelectionMode(isSingleSelection: Boolean, translate: Boolean) {
        selectionAutomata.startSelection(isSingleSelection, translate)
    }


    private fun doOnLayout(lastPageInfo1: LastPageInfo) {
        (view as View).doOnLayout {
            controller?.drawPage(
                lastPageInfo1.pageNumber,
                lastPageInfo1.newOffsetX,
                lastPageInfo1.newOffsetY,
                lastPageInfo1.isSinglePageMode
            )
            controller?.pageLayoutManager?.updateCacheAndRender()
        }
    }

    fun startSearch() {
        SearchDialog.newInstance().show(supportFragmentManager, "search")
    }

    private fun destroyController() {
        log("Controller: destroy")
        controller?.destroy()
        controller = null
    }

}
