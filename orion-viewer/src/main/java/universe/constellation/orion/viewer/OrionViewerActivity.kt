package universe.constellation.orion.viewer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.os.Debug
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialog
import androidx.core.internal.view.SupportMenuItem
import androidx.core.math.MathUtils
import androidx.core.view.doOnLayout
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*
import universe.constellation.orion.viewer.FallbackDialogs.Companion.saveFileByUri
import universe.constellation.orion.viewer.android.getFileInfo
import universe.constellation.orion.viewer.android.isRestrictedAccessPath
import universe.constellation.orion.viewer.device.Device
import universe.constellation.orion.viewer.dialog.SearchDialog
import universe.constellation.orion.viewer.dialog.create
import universe.constellation.orion.viewer.document.Document
import universe.constellation.orion.viewer.layout.SimpleLayoutStrategy
import universe.constellation.orion.viewer.prefs.GlobalOptions
import universe.constellation.orion.viewer.prefs.initalizer
import universe.constellation.orion.viewer.selection.NewTouchProcessor
import universe.constellation.orion.viewer.selection.NewTouchProcessorWithScale
import universe.constellation.orion.viewer.selection.SelectionAutomata
import universe.constellation.orion.viewer.view.FullScene
import universe.constellation.orion.viewer.view.OrionDrawScene
import universe.constellation.orion.viewer.view.StatusBar
import updateGlobalOptionsFromIntent
import java.io.File
import java.util.concurrent.Executors
import kotlin.coroutines.resume

enum class MyState {
    PROCESSING_INTENT,
    WAITING_ACTION,
    FINISHED
}

class OrionViewerActivity : OrionBaseActivity(viewerType = Device.VIEWER_ACTIVITY) {

    internal val subscriptionManager = SubscriptionManager()

    private var lastPageInfo: LastPageInfo? = null

    var controller: Controller? = null
        private set

    private var myState: MyState = MyState.PROCESSING_INTENT

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

    private var openAsTempTestBook = false

    var isNewUI: Boolean = false
        private set

//    val bookId: Long
//        get() {
//            log("Selecting book id...")
//            val info = lastPageInfo!!
//            var bookId: Long? = orionApplication.tempOptions!!.bookId
//            if (bookId == null || bookId == -1L) {
//                bookId = orionApplication.getBookmarkAccessor().selectBookId(info.simpleFileName, info.fileSize)
//                orionApplication.tempOptions!!.bookId = bookId
//            }
//            log("...book id = $bookId")
//            return bookId
//        }

    @SuppressLint("MissingSuperCall")
    public override fun onCreate(savedInstanceState: Bundle?) {
        log("Creating OrionViewerActivity...")
        openAsTempTestBook = updateGlobalOptionsFromIntent(intent)
        isNewUI = globalOptions.isNewUI
        orionApplication.viewActivity = this

        onOrionCreate(savedInstanceState, R.layout.main_view, !isNewUI)

        val view = findViewById<OrionDrawScene>(R.id.view)

        fullScene = FullScene(
            findViewById<View>(R.id.orion_full_scene) as ViewGroup,
            view,
            findViewById<View>(R.id.orion_status_bar) as ViewGroup,
            this
        )
        fullScene.setDrawOffPage(globalOptions.isDrawOffPage)

        newTouchProcessor = NewTouchProcessorWithScale(view, this)
        view.setOnTouchListener { _, event ->
            newTouchProcessor!!.onTouch(event)
        }
        processIntentAndCheckPermission(intent, true)

    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray,
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
////        if (ASK_READ_PERMISSION_FOR_BOOK_OPEN == requestCode) {
//            println("Permission callback $requestCode...")
//            processIntentAndCheckPermission(intent ?: return)
////        }
//    }

    internal fun onNewIntentInternal(intent: Intent) {
        onNewIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        processIntentAndCheckPermission(intent, intent.getBooleanExtra(USER_INTENT, true))
    }

    private fun askReadPermissionOrOpenExisting(fileInfo: FileInfo, intent: Intent) {
        log("Checking permissions for: $fileInfo")
        myState = MyState.WAITING_ACTION
//        if (fileInfo.isRestrictedAccessPath() || hasReadStoragePermission(this)) {
//            FallbackDialogs().createPrivateResourceFallbackDialog(this, fileInfo, intent).show()
//        } else {
//            FallbackDialogs().createGrantReadPermissionsDialog(
//                this@OrionViewerActivity,
//                fileInfo,
//                intent
//            ).show()
//        }
    }

    internal fun processIntentAndCheckPermission(intent: Intent, isUserIntent: Boolean = false) {
        log("Trying to open document by $intent...")
//        showErrorPanel(false)

        if (!openAsTempTestBook) {
            //UGLY hack: otherwise Espresso can't recognize that it's test activity
            setIntent(intent)
        }
        myState = MyState.PROCESSING_INTENT

        val uri = intent.data
        if (uri != null) {
            log("Try to open file by $uri")
            try {
                val fileInfo = getFileInfo(this, uri)
                val filePath = fileInfo?.path

                if (fileInfo == null || filePath.isNullOrBlank()) {
                    FallbackDialogs().createBadIntentFallbackDialog(this, null, intent).show()
                    destroyController()
                    return
                }

                if (controller != null && lastPageInfo != null) {
                    lastPageInfo?.apply {
                        if (openingFileName == filePath) {
                            log("Fast processing")
                            controller!!.drawPage(
                                pageNumber,
                                newOffsetX,
                                newOffsetY,
                                controller!!.pageLayoutManager.isSinglePageMode
                            )
                            return
                        }
                    }
                }
                destroyController()

                val fileToOpen = if (!fileInfo.file.canRead()) {
                    val cacheFileIfExists =
                        getStableTmpFileIfExists(fileInfo)?.takeIf { it.length() == fileInfo.size }

                    if (cacheFileIfExists == null) {
                        askReadPermissionOrOpenExisting(fileInfo, intent)
                        log("Waiting for read permissions for $intent")
                        return
                    } else {
                        cacheFileIfExists
                    }
                } else {
                    fileInfo.file
                }


                if (fileToOpen.length() == 0L) {
                    return
                }

                openFile(fileToOpen)
                myState = MyState.FINISHED
            } catch (e: Exception) {

            }

        } else {
        }
    }

    @Throws(Exception::class)
    private fun openFile(file: File) {
        log("Runtime.getRuntime().totalMemory(): ${Runtime.getRuntime().totalMemory()}")
        log("Debug.getNativeHeapSize(): ${Debug.getNativeHeapSize()}")
        log("openFileAndDestroyOldController")

        orionApplication.idlingRes.busy()

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
                orionApplication.idlingRes.free()
                return@launch
            }

            try {
                if (!askPassword(newDocument)) {
                    return@launch
                }

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
                controller1.changeOrinatation(lastPageInfo1.screenOrientation)

                updateViewOnNewBook((newDocument.title?.takeIf { it.isNotBlank() }
                    ?: file.name.substringBeforeLast(".")))

                val drawView = fullScene.drawView
                controller1.init(lastPageInfo1, drawView.sceneWidth, drawView.sceneHeight)

                subscriptionManager.sendDocOpenedNotification(controller1)

                globalOptions.addRecentEntry(GlobalOptions.RecentEntry(file.absolutePath))

                lastPageInfo1.totalPages = newDocument.pageCount
                orionApplication.onNewBook(file.name)
//                invalidateOrHideMenu()
                doOnLayout(lastPageInfo1)
            } catch (e: Exception) {
                if (controller != null) {
                    destroyController()
                } else {
                    newDocument.destroy()
                }
            } finally {
                orionApplication.idlingRes.free()
            }
        }
    }

    private suspend fun loadBookParameters(
        rootJob: CompletableJob,
        bookFile: File,
    ): LastPageInfo {
        if (openAsTempTestBook) {
            return loadBookParameters(
                this@OrionViewerActivity,
                "temp-test-bookx",
                initalizer(globalOptions)
            )
        }

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
        controller?.let {
            it.onPause()
            saveBookPositionAndRecentFiles()
        }
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
        if (openAsTempTestBook) {
        }
    }

    private fun saveBookPositionAndRecentFiles() {
        try {
            lastPageInfo?.let {
                if (!openAsTempTestBook) {
                    controller?.serializeAndSave(it, this)
                }
            }
        } catch (ex: Exception) {
            log(ex)
        }
        globalOptions.saveRecentFiles()
    }


    fun doAction(actionCode: Int) {
        val action = Action.getAction(actionCode)
        doAction(action)
        log("Code action $actionCode")
    }


    internal fun doAction(action: Action) {
        action.doAction(controller, this, null)
    }


    fun AppCompatDialog.onApplyAction() {
        if (globalOptions.isApplyAndClose) {
            dismiss()
        }
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

//    private fun insertOrGetBookId(): Long {
//        val info = lastPageInfo!!
//        var bookId: Long? = orionApplication.tempOptions!!.bookId
//        if (bookId == null || bookId == -1L) {
//            bookId = orionApplication.getBookmarkAccessor().insertOrUpdate(info.simpleFileName, info.fileSize)
//            orionApplication.tempOptions!!.bookId = bookId
//        }
//        return bookId.toInt().toLong()
//    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        log("On activity result requestCode=$requestCode resultCode=$resultCode data=$data originalIntent=$intent")
        when (requestCode) {
            OPEN_BOOKMARK_ACTIVITY_RESULT -> {
                if (resultCode == Activity.RESULT_OK) {
                    if (controller != null) {

                        doAction(Action.GOTO)

                    }
                }
            }

            SAVE_FILE_RESULT -> {
                if (resultCode == Activity.RESULT_OK) {
                    val inputFileIntentData = intent.data
                    if (data?.data != null && inputFileIntentData != null) {
                        return
                    }
                }
                processIntentAndCheckPermission(intent ?: return)
            }

        }
    }

    fun textSelectionMode(isSingleSelection: Boolean, translate: Boolean) {
        selectionAutomata.startSelection(isSingleSelection, translate)
    }


    private suspend fun askPassword(controller: Document): Boolean {
        if (controller.needPassword()) {
            val view = layoutInflater.inflate(R.layout.password, null)
            val builder = createThemedAlertBuilder()

            builder.setView(view)
                .setNegativeButton(R.string.string_cancel) { dialog, _ -> dialog.cancel() }
                .setPositiveButton(R.string.string_apply) { _, _ -> }


            val dialog = builder.create()
            dialog.setCanceledOnTouchOutside(false)
            dialog.show()

            return suspendCancellableCoroutine { continuation ->
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                    lifecycleScope.launch {
                        val input = view.findViewById<TextInputEditText>(R.id.password)!!
                        if (controller.authenticate(input.text.toString())) {
                            dialog.dismiss()
                            continuation.resume(true)
                        } else {
                            input.error = getString(R.string.string_wrong_password)
                        }
                    }
                }

                dialog.setOnCancelListener {
                    continuation.resume(false)
                }

                continuation.invokeOnCancellation {
                    controller.destroy()
                    dialog.cancel()
                }
            }
        } else {
            return true
        }
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
        orionApplication.currentBookParameters = null
    }

    companion object {

        const val OPEN_BOOKMARK_ACTIVITY_RESULT = 1

        const val SAVE_FILE_RESULT = 2

        const val CROP_RESTRICTION_MIN = -10

        const val CROP_RESTRICTION_MAX = 40

        const val USER_INTENT = "USER_INTENT"
    }
}
