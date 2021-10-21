package mega.privacy.android.app.imageviewer

import android.app.Activity
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.map
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.kotlin.subscribeBy
import io.reactivex.rxjava3.schedulers.Schedulers
import mega.privacy.android.app.arch.BaseRxViewModel
import mega.privacy.android.app.getLink.useCase.ExportNodeUseCase
import mega.privacy.android.app.imageviewer.data.ImageItem
import mega.privacy.android.app.imageviewer.usecase.GetImageHandlesUseCase
import mega.privacy.android.app.imageviewer.usecase.GetImageUseCase
import mega.privacy.android.app.usecase.CancelTransferUseCase
import mega.privacy.android.app.usecase.GetNodeUseCase
import mega.privacy.android.app.usecase.data.MegaNodeItem
import mega.privacy.android.app.utils.LogUtil.logError

class ImageViewerViewModel @ViewModelInject constructor(
    private val getImageUseCase: GetImageUseCase,
    private val getImageHandlesUseCase: GetImageHandlesUseCase,
    private val getNodeUseCase: GetNodeUseCase,
    private val exportNodeUseCase: ExportNodeUseCase,
    private val cancelTransferUseCase: CancelTransferUseCase
) : BaseRxViewModel() {

    private val currentHandle = MutableLiveData<Long>()
    private val initialPosition = MutableLiveData<Int>()
    private val images: MutableLiveData<List<ImageItem>> = MutableLiveData()
    private val switchToolbar: MutableLiveData<Unit> = MutableLiveData()

    fun getCurrentHandle(): LiveData<Long> = currentHandle

    fun getCurrentImage(): LiveData<MegaNodeItem?> =
        Transformations.switchMap(currentHandle) { currentHandle -> getNode(currentHandle) }

    fun getImagesHandle(): LiveData<List<Long>> =
        images.map { items -> items.map(ImageItem::handle) }

    fun getImage(nodeHandle: Long): LiveData<ImageItem?> =
        images.map { items -> items.firstOrNull { it.handle == nodeHandle } }

    fun getInitialPosition(): LiveData<Int> = initialPosition

    fun onSwitchToolbar(): LiveData<Unit> = switchToolbar

    fun retrieveSingleImage(nodeHandle: Long) {
        getImageHandlesUseCase.get(longArrayOf(nodeHandle))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { imageItems ->
                    images.value = imageItems.toList()
                },
                onError = { error ->
                    logError(error.stackTraceToString())
                }
            )
            .addTo(composite)
    }

    fun retrieveSingleOfflineImage(nodeHandle: Long) {
        getImageHandlesUseCase.getOffline(longArrayOf(nodeHandle))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { imageItems ->
                    images.value = imageItems.toList()
                },
                onError = { error ->
                    logError(error.stackTraceToString())
                }
            )
            .addTo(composite)
    }

    fun retrieveImagesFromParent(
        parentNodeHandle: Long,
        childOrder: Int? = null,
        currentNodeHandle: Long? = null
    ) {
        getImageHandlesUseCase.getChildren(parentNodeHandle, childOrder)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { imageItems ->
                    images.value = imageItems.toList()

                    val currentIndex = imageItems.indexOfFirst { it.handle == currentNodeHandle }
                    if (currentIndex != -1) {
                        currentHandle.value = currentNodeHandle!!
                        initialPosition.value = currentIndex
                    }
                },
                onError = { error ->
                    logError(error.stackTraceToString())
                }
            )
            .addTo(composite)
    }

    fun retrieveImages(
        nodeHandles: LongArray,
        currentNodeHandle: Long? = null
    ) {
        getImageHandlesUseCase.get(nodeHandles)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { imageItems ->
                    images.value = imageItems.toList()

                    val currentIndex = imageItems.indexOfFirst { it.handle == currentNodeHandle }
                    if (currentIndex != -1) {
                        currentHandle.value = currentNodeHandle!!
                        initialPosition.value = currentIndex
                    }
                },
                onError = { error ->
                    logError(error.stackTraceToString())
                }
            )
            .addTo(composite)
    }

    fun retrieveOfflineImages(
        nodeHandles: LongArray,
        currentNodeHandle: Long? = null
    ) {
        getImageHandlesUseCase.getOffline(nodeHandles)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { imageItems ->
                    images.value = imageItems.toList()

                    val currentIndex = imageItems.indexOfFirst { it.handle == currentNodeHandle }
                    if (currentIndex != -1) {
                        currentHandle.value = currentNodeHandle!!
                        initialPosition.value = currentIndex
                    }
                },
                onError = { error ->
                    logError(error.stackTraceToString())
                }
            )
            .addTo(composite)
    }

    fun loadSingleImage(nodeHandle: Long, fullSize: Boolean): LiveData<Unit> {
        val result = MutableLiveData<Unit>()
        getImageUseCase.get(nodeHandle, fullSize)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = { imageItem ->
                    val currentImages = images.value?.toMutableList()!!
                    val index = currentImages.indexOfFirst { it.handle == nodeHandle }
                    currentImages[index] = imageItem

                    images.value = currentImages.toList()
                },
                onError = { error ->
                    logError(error.stackTraceToString())
                },
                onComplete = {
                    result.value = Unit
                }
            )
            .addTo(composite)
        return result
    }

    fun getNode(nodeHandle: Long): LiveData<MegaNodeItem?> {
        val result = MutableLiveData<MegaNodeItem?>()
        getNodeUseCase.getNodeItem(nodeHandle)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { item ->
                    result.value = item
                },
                onError = { error ->
                    result.value = null
                    logError(error.stackTraceToString())
                }
            )
            .addTo(composite)
        return result
    }

    fun markNodeAsFavorite(nodeHandle: Long, isFavorite: Boolean) {
        getNodeUseCase.markAsFavorite(nodeHandle, isFavorite)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onError = { error ->
                    logError(error.stackTraceToString())
                }
            )
            .addTo(composite)
    }

    fun setNodeAvailableOffline(
        activity: Activity,
        nodeHandle: Long,
        setAvailableOffline: Boolean
    ) {
        getNodeUseCase.setNodeAvailableOffline(activity, nodeHandle, setAvailableOffline)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onError = { error ->
                    logError(error.stackTraceToString())
                }
            )
            .addTo(composite)
    }

    fun removeLink(nodeHandle: Long) {
        exportNodeUseCase.disableExport(nodeHandle)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onError = { error ->
                    logError(error.stackTraceToString())
                }
            )
            .addTo(composite)
    }

    fun shareNode(nodeHandle: Long): LiveData<String> {
        val result = MutableLiveData<String>()
        exportNodeUseCase.export(nodeHandle)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { link ->
                    result.value = link
                },
                onError = { error ->
                    logError(error.stackTraceToString())
                }
            )
            .addTo(composite)
        return result
    }

    fun copyNode(nodeHandle: Long, newParentHandle: Long) {
        getNodeUseCase.copyNode(nodeHandle, newParentHandle)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onError = { error ->
                    logError(error.stackTraceToString())
                }
            )
            .addTo(composite)
    }

    fun moveNode(nodeHandle: Long, newParentHandle: Long) {
        getNodeUseCase.moveNode(nodeHandle, newParentHandle)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onError = { error ->
                    logError(error.stackTraceToString())
                }
            )
            .addTo(composite)
    }

    fun moveNodeToRubbishBin(nodeHandle: Long) {
        getNodeUseCase.moveToRubbishBin(nodeHandle)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onError = { error ->
                    logError(error.stackTraceToString())
                }
            )
            .addTo(composite)
    }

    fun removeNode(nodeHandle: Long) {
        getNodeUseCase.removeNode(nodeHandle)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onError = { error ->
                    logError(error.stackTraceToString())
                }
            )
            .addTo(composite)
    }

    fun stopImageLoading(nodeHandle: Long) {
        images.value?.find { nodeHandle == it.handle }?.transferTag?.let { transferTag ->
            cancelTransferUseCase.cancel(transferTag)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                    onError = { error ->
                        logError(error.stackTraceToString())
                    }
                )
        }
    }

    fun reloadCurrentImage(fullSize: Boolean) {
        currentHandle.value?.let { handle -> loadSingleImage(handle, fullSize) }
    }

    fun updateCurrentPosition(position: Int) {
        images.value?.get(position)?.handle?.let { handle ->
            if (handle != currentHandle.value) {
                currentHandle.value = handle
            }
        }
    }

    fun switchToolbar() {
        switchToolbar.value = Unit
    }
}
