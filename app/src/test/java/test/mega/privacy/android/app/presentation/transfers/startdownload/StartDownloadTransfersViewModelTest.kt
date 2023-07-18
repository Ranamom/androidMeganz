package test.mega.privacy.android.app.presentation.transfers.startdownload

import com.google.common.truth.Truth
import de.palm.composestateevents.StateEventWithContentTriggered
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import mega.privacy.android.app.presentation.transfers.startdownload.StartDownloadTransfersViewModel
import mega.privacy.android.app.presentation.transfers.startdownload.model.StartDownloadTransferEvent
import mega.privacy.android.app.presentation.transfers.startdownload.model.StartDownloadTransferJobInProgress
import mega.privacy.android.domain.entity.node.NodeId
import mega.privacy.android.domain.entity.node.TypedFileNode
import mega.privacy.android.domain.entity.node.TypedFolderNode
import mega.privacy.android.domain.entity.offline.OtherOfflineNodeInformation
import mega.privacy.android.domain.entity.transfer.DownloadNodesEvent
import mega.privacy.android.domain.usecase.BroadcastOfflineFileAvailabilityUseCase
import mega.privacy.android.domain.usecase.GetNodeByIdUseCase
import mega.privacy.android.domain.usecase.downloads.GetDefaultDownloadPathForNodeUseCase
import mega.privacy.android.domain.usecase.favourites.GetOfflineFileUseCase
import mega.privacy.android.domain.usecase.network.MonitorConnectivityUseCase
import mega.privacy.android.domain.usecase.offline.GetOfflineNodeInformationUseCase
import mega.privacy.android.domain.usecase.offline.SaveOfflineNodeInformationUseCase
import mega.privacy.android.domain.usecase.transfer.StartDownloadUseCase
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.File
import java.util.stream.Stream

@OptIn(ExperimentalCoroutinesApi::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class StartDownloadTransfersViewModelTest {

    lateinit var underTest: StartDownloadTransfersViewModel

    private val getNodeByIdUseCase: GetNodeByIdUseCase = mock()
    private val getDefaultDownloadPathForNodeUseCase: GetDefaultDownloadPathForNodeUseCase = mock()
    private val startDownloadUseCase: StartDownloadUseCase = mock()
    private val getOfflineNodeInformationUseCase: GetOfflineNodeInformationUseCase = mock()
    private val getOfflineFileUseCase: GetOfflineFileUseCase = mock()
    private val saveOfflineNodeInformationUseCase: SaveOfflineNodeInformationUseCase = mock()
    private val broadcastOfflineFileAvailabilityUseCase: BroadcastOfflineFileAvailabilityUseCase =
        mock()
    private val monitorConnectivityUseCase: MonitorConnectivityUseCase = mock()
    private val monitorConnectivityFlow: StateFlow<Boolean> = mock()
    private val node: TypedFileNode = mock()
    private val nodes = listOf(node)
    private val parentNode: TypedFolderNode = mock()

    @BeforeAll
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        underTest = StartDownloadTransfersViewModel(
            getNodeByIdUseCase,
            getDefaultDownloadPathForNodeUseCase,
            startDownloadUseCase,
            getOfflineNodeInformationUseCase,
            getOfflineFileUseCase,
            saveOfflineNodeInformationUseCase,
            broadcastOfflineFileAvailabilityUseCase,
            monitorConnectivityUseCase,
        )

    }

    @BeforeEach
    fun resetMocks() {
        reset(
            getNodeByIdUseCase,
            getDefaultDownloadPathForNodeUseCase,
            startDownloadUseCase,
            getOfflineNodeInformationUseCase,
            getOfflineFileUseCase,
            saveOfflineNodeInformationUseCase,
            broadcastOfflineFileAvailabilityUseCase,
            monitorConnectivityUseCase,
            node,
            parentNode,
            monitorConnectivityFlow
        )
    }

    @Test
    fun `test that start download use case is invoked with correct parameters when startDownloadNode is invoked`() =
        runTest {
            stubNodeForDownload()
            underTest.startDownloadNode(nodes)
            verify(startDownloadUseCase).invoke(nodes, destination, null, false)
        }

    @Test
    fun `test that start download use case is invoked with correct parameters when startDownloadForOffline is invoked`() =
        runTest {
            stubNodeForDownload()
            val file = mock<File> {
                on { path }.thenReturn(destination)
            }
            whenever(getOfflineNodeInformationUseCase(any())).thenReturn(mock<OtherOfflineNodeInformation>())
            whenever(getOfflineFileUseCase(any())).thenReturn(file)
            underTest.startDownloadForOffline(node)
            verify(startDownloadUseCase).invoke(listOf(node), destination, null, false)
        }

    @Test
    fun `test that no connection event is emitted when monitorConnectivityUseCase is false`() =
        runTest {
            stubNodeForDownload(false)
            underTest.startDownloadNode(nodes)
            assertCurrentEventIsEqualTo(StartDownloadTransferEvent.NotConnected)
        }

    @Test
    fun `test that job in progress is set to ProcessingFiles when start download use case starts`() =
        runTest {
            stubNodeForDownload()
            stubStartDownload(flow { delay(500) })
            underTest.startDownloadNode(nodes)
            Truth.assertThat(underTest.uiState.value.jobInProgressState)
                .isEqualTo(StartDownloadTransferJobInProgress.ProcessingFiles)
        }

    @Test
    fun `test that FinishProcessing event is emitted if start download use case finishes correctly`() =
        runTest {
            stubNodeForDownload()
            stubStartDownload(flowOf(DownloadNodesEvent.FinishProcessingTransfers))
            underTest.startDownloadNode(nodes)
            assertCurrentEventIsEqualTo(StartDownloadTransferEvent.FinishProcessing(null, 1))
        }

    @Test
    fun `test that NotSufficientSpace event is emitted if start download use case returns NotSufficientSpace`() =
        runTest {
            stubNodeForDownload()
            stubStartDownload(flowOf(DownloadNodesEvent.NotSufficientSpace))
            underTest.startDownloadNode(nodes)
            assertCurrentEventIsEqualTo(StartDownloadTransferEvent.Message.NotSufficientSpace)
        }

    @ParameterizedTest(name = "when StartDownloadUseCase finishes with {0}, then {1} is emitted")
    @MethodSource("provideDownloadNodeParameters")
    fun `test that a specific StartDownloadTransferEvent is emitted`(
        downloadNodesEvent: DownloadNodesEvent,
        startDownloadTransferEvent: StartDownloadTransferEvent,
    ) = runTest {
        stubNodeForDownload()
        stubStartDownload(flowOf(downloadNodesEvent))
        underTest.startDownloadNode(nodes)
        assertCurrentEventIsEqualTo(startDownloadTransferEvent)
    }

    private fun provideDownloadNodeParameters() = Stream.of(
        Arguments.of(
            DownloadNodesEvent.FinishProcessingTransfers,
            StartDownloadTransferEvent.FinishProcessing(null, 1),
        ),
        Arguments.of(
            DownloadNodesEvent.NotSufficientSpace,
            StartDownloadTransferEvent.Message.NotSufficientSpace,
        ),
    )

    private fun assertCurrentEventIsEqualTo(event: StartDownloadTransferEvent) {
        Truth.assertThat(underTest.uiState.value.oneOffViewEvent)
            .isInstanceOf(StateEventWithContentTriggered::class.java)
        Truth.assertThat((underTest.uiState.value.oneOffViewEvent as StateEventWithContentTriggered).content)
            .isEqualTo(event)
    }

    private suspend fun stubNodeForDownload(internetConnection: Boolean = true) {
        whenever(node.id).thenReturn(nodeId)
        whenever(node.parentId).thenReturn(parentId)
        whenever(parentNode.id).thenReturn(parentId)

        whenever(getNodeByIdUseCase(parentId)).thenReturn(parentNode)
        whenever(getDefaultDownloadPathForNodeUseCase(parentNode)).thenReturn(destination)

        whenever(monitorConnectivityFlow.value).thenReturn(internetConnection)
        whenever(monitorConnectivityUseCase()).thenReturn(monitorConnectivityFlow)
    }

    private fun stubStartDownload(flow: Flow<DownloadNodesEvent>) {
        whenever(
            startDownloadUseCase(
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull()
            )
        ).thenReturn(flow)
    }

    companion object {
        private const val NODE_HANDLE = 10L
        private const val PARENT_NODE_HANDLE = 12L
        private val nodeId = NodeId(NODE_HANDLE)
        private val parentId = NodeId(PARENT_NODE_HANDLE)
        private const val destination = "/destination"
    }
}