package universe.constellation.orion.viewer.view

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.plus
import kotlinx.coroutines.withContext
import universe.constellation.orion.viewer.Controller
import universe.constellation.orion.viewer.PageSize
import universe.constellation.orion.viewer.document.Document
import universe.constellation.orion.viewer.document.Page

open class CorePageView(
    val pageNum: Int,
    val document: Document,
    val controller: Controller,
    rootJob: Job,
    val page: Page = document.getOrCreatePageAdapter(pageNum)
) {


    private val renderingPageJobs = SupervisorJob(rootJob)

    private val dataPageJobs = SupervisorJob(rootJob)

    @Volatile
    private var rawSize: Deferred<PageSize>? = null

    @Volatile
    private var pageData: Deferred<Unit>? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    protected val renderingScope = CoroutineScope(
        controller.renderingDispatcher.limitedParallelism(2) + renderingPageJobs
    )

    protected val renderingScopeOnUI = renderingScope + Dispatchers.Main

    protected val dataPageScope = CoroutineScope(controller.context + dataPageJobs)


    fun readPageDataFromUI(): Deferred<Unit> {
        if (pageData == null) {
            pageData = dataPageScope.async { page.readPageDataForRendering() }
        }
        return pageData!!
    }

    fun readRawSizeFromUI(): Deferred<PageSize> {
        if (rawSize == null) {
            rawSize = dataPageScope.async { page.getPageSize() }
        }
        return rawSize!!
    }


    internal fun cancelChildJobs(allJobs: Boolean = false) {
        renderingPageJobs.cancelChildren()
        if (allJobs) {
            dataPageJobs.cancelChildren()
        }
    }

    protected suspend fun waitJobsCancellation(allJobs: Boolean = false) {
        if (allJobs) {
            dataPageJobs.cancel()
        }
        renderingPageJobs.cancelAndJoin()
        if (allJobs) {
            dataPageJobs.cancelAndJoin()
        }
    }

    suspend fun <T> renderForCrop(body: suspend () -> T): T {
        return withContext(controller.renderingDispatcher) {
            body()
        }
    }

}