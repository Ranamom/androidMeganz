package test.mega.privacy.android.app.presentation.qrcode

import android.graphics.Bitmap
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.compose.ui.unit.sp
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import de.palm.composestateevents.consumed
import de.palm.composestateevents.triggered
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import mega.privacy.android.app.presentation.avatar.mapper.AvatarContentMapper
import mega.privacy.android.app.presentation.avatar.model.PhotoAvatarContent
import mega.privacy.android.app.presentation.qrcode.QRCodeViewModel
import mega.privacy.android.app.presentation.qrcode.mapper.MyQRCodeTextErrorMapper
import mega.privacy.android.app.presentation.qrcode.mapper.SaveBitmapToFileMapper
import mega.privacy.android.app.presentation.qrcode.mycode.model.MyCodeUIState
import mega.privacy.android.domain.entity.contacts.InviteContactRequest
import mega.privacy.android.domain.entity.qrcode.ScannedContactLinkResult
import mega.privacy.android.domain.usecase.CopyToClipBoard
import mega.privacy.android.domain.usecase.GetMyAvatarColorUseCase
import mega.privacy.android.domain.usecase.GetUserFullNameUseCase
import mega.privacy.android.domain.usecase.account.qr.GetQRCodeFileUseCase
import mega.privacy.android.domain.usecase.avatar.GetMyAvatarFileUseCase
import mega.privacy.android.domain.usecase.contact.InviteContactUseCase
import mega.privacy.android.domain.usecase.qrcode.CreateContactLinkUseCase
import mega.privacy.android.domain.usecase.qrcode.DeleteQRCodeUseCase
import mega.privacy.android.domain.usecase.qrcode.QueryScannedContactLinkUseCase
import mega.privacy.android.domain.usecase.qrcode.ResetContactLinkUseCase
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.TestInstance
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.io.File

/**
 * Test cases for [QRCodeViewModel]
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(AndroidJUnit4::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QRCodeViewModelTest {

    private lateinit var underTest: QRCodeViewModel

    private val copyToClipBoard = mock<CopyToClipBoard>()
    private val createContactLinkUseCase: CreateContactLinkUseCase = mock()
    private val deleteQRCodeUseCase: DeleteQRCodeUseCase = mock()
    private val getQRCodeFileUseCase: GetQRCodeFileUseCase = mock()
    private val resetContactLinkUseCase: ResetContactLinkUseCase = mock()
    private val getMyAvatarColorUseCase: GetMyAvatarColorUseCase = mock()
    private val getMyAvatarFileUseCase: GetMyAvatarFileUseCase = mock()
    private val getUserFullNameUseCase: GetUserFullNameUseCase = mock()
    private val saveBitmapToFileMapper: SaveBitmapToFileMapper = mock()
    private val queryScannedContactLinkUseCase = mock<QueryScannedContactLinkUseCase>()
    private val inviteContactUseCase = mock<InviteContactUseCase>()
    private val avatarContentMapper = mock<AvatarContentMapper>()
    private val myQRCodeTextErrorMapper = mock<MyQRCodeTextErrorMapper>()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val initialContactLink = "https://contact_link1"

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        underTest = QRCodeViewModel(
            copyToClipBoard = copyToClipBoard,
            createContactLinkUseCase = createContactLinkUseCase,
            getQRCodeFileUseCase = getQRCodeFileUseCase,
            deleteQRCodeUseCase = deleteQRCodeUseCase,
            resetContactLinkUseCase = resetContactLinkUseCase,
            getMyAvatarColorUseCase = getMyAvatarColorUseCase,
            getMyAvatarFileUseCase = getMyAvatarFileUseCase,
            getUserFullNameUseCase = getUserFullNameUseCase,
            saveBitmapToFile = saveBitmapToFileMapper,
            inviteContactUseCase = inviteContactUseCase,
            queryScannedContactLinkUseCase = queryScannedContactLinkUseCase,
            avatarContentMapper = avatarContentMapper,
            myQRCodeTextErrorMapper = myQRCodeTextErrorMapper
        )
    }

    @AfterAll
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test that initial state is correct`() = runTest {
        underTest.uiState.test {
            val initialState = awaitItem()
            with(initialState) {
                assertThat(myQRCodeState).isEqualTo(MyCodeUIState.Idle)
                assertThat(contactLink).isNull()
                assertThat(qrCodeBitmap).isNull()
                assertThat(isInProgress).isFalse()
                assertThat(localQRCodeFile).isNull()
                assertThat(hasQRCodeBeenDeleted).isFalse()
                assertThat(resultMessage).isInstanceOf(consumed<Int>().javaClass)
                assertThat(inviteContactResult).isInstanceOf(consumed<InviteContactRequest>().javaClass)
                assertThat(scannedContactLinkResult).isInstanceOf(consumed<ScannedContactLinkResult>().javaClass)
            }
        }
    }

    @Test
    fun `test that no contact link text can be copied to clipboard when it is initial state`() =
        runTest {
            underTest.copyContactLink()
            verifyNoInteractions(copyToClipBoard)
        }

    @Test
    fun `test that bitmap can be used`() = runTest {
        val expectedBitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888)
        assertThat(expectedBitmap.width).isEqualTo(100)
    }

    @Test
    fun `test that QR code file can be shared when it exists`() = runTest {
        val file: File = mock {
            on { exists() }.thenReturn(true)
        }
        whenever(getQRCodeFileUseCase()).thenReturn(file)
        underTest.startSharing()
        underTest.uiState.test {
            assertThat(awaitItem().localQRCodeFile).isEqualTo(file)
        }
    }

    @Test
    fun `test that QR code file is not shared when it does not exist`() = runTest {
        whenever(getQRCodeFileUseCase()).thenReturn(null)
        underTest.startSharing()
        underTest.uiState.test {
            assertThat(awaitItem().localQRCodeFile).isNull()
        }
    }

    @Test
    fun `test that exception is captured when getQR Code file throws exception when sharing`() =
        runTest {
            whenever(getQRCodeFileUseCase()).thenAnswer { throw Exception() }
            underTest.startSharing()
        }

    @Test
    fun `test that QRCode can be reset successfully`() = runTest {
        prepareQRCode()
        val localAvatarFile = mock<File> {
            on { exists() }.thenReturn(true)
            on { length() }.thenReturn(100)
        }
        val newContactLink = "https://contact_link2"
        whenever(resetContactLinkUseCase()).thenReturn(newContactLink)
        whenever(getMyAvatarFileUseCase(isForceRefresh = false)).thenReturn(localAvatarFile)
        whenever(myQRCodeTextErrorMapper(any())).thenReturn("error")

        underTest.resetQRCode()
        underTest.uiState.test {
            val result = awaitItem()
            assertThat(result.myQRCodeState).isInstanceOf(MyCodeUIState.QRCodeAvailable::class.java)
            assertThat((result.myQRCodeState as MyCodeUIState.QRCodeAvailable).contactLink)
                .isEqualTo(newContactLink)
        }
    }

    @Test
    fun `test that QRCode reset fails when resetContactLink use case throws exception`() = runTest {
        prepareQRCode()

        whenever(resetContactLinkUseCase()).thenAnswer { throw Exception() }
        whenever(myQRCodeTextErrorMapper(any())).thenReturn("error")
        underTest.resetQRCode()
        underTest.uiState.test {
            val result = awaitItem()
            assertThat((result.myQRCodeState as MyCodeUIState.QRCodeAvailable).contactLink)
                .isEqualTo(initialContactLink)
        }
    }

    @Test
    fun `test that QR Code can be deleted successfully`() = runTest {
        prepareQRCode()
        underTest.deleteQRCode()
        underTest.uiState.test {
            val state = awaitItem()
            assertThat(state.myQRCodeState).isInstanceOf(MyCodeUIState.QRCodeDeleted::class.java)
        }
    }

    @Test
    fun `test that error message is shown when delete QR code fails`() = runTest {
        prepareQRCode()

        whenever(deleteQRCodeUseCase(any())).thenAnswer { throw Exception() }
        whenever(myQRCodeTextErrorMapper(any())).thenReturn("error")

        underTest.deleteQRCode()
        underTest.uiState.test {
            val result = awaitItem()
            assertThat((result.myQRCodeState as MyCodeUIState.QRCodeAvailable).contactLink)
                .isEqualTo(initialContactLink)
        }
    }

    @Test
    fun `test that inviteContactResult is set correctly when send invite is success`() =
        runTest {
            whenever(inviteContactUseCase(any(), any(), anyOrNull()))
                .thenReturn(InviteContactRequest.Sent)
            underTest.uiState.test {
                awaitItem()
                underTest.sendInvite(123L, "abc@gmail.com")
                val newValue = awaitItem()
                assertThat(newValue.inviteContactResult).isInstanceOf(triggered(InviteContactRequest.Sent).javaClass)
            }
        }

    @Test
    fun `test that inviteContactResult is set to InvalidStatus when send invite throws exception`() =
        runTest {
            whenever(inviteContactUseCase(any(), any(), anyOrNull()))
                .thenAnswer { throw RuntimeException() }
            underTest.uiState.test {
                awaitItem()
                underTest.sendInvite(123L, "abc@gmail.com")
                val newValue = awaitItem()
                assertThat(newValue.inviteContactResult).isInstanceOf(triggered(InviteContactRequest.InvalidStatus).javaClass)
            }
        }

    private suspend fun prepareQRCode() {
        val localAvatarFile = mock<File> {
            on { exists() }.thenReturn(true)
            on { length() }.thenReturn(100)
        }

        whenever(createContactLinkUseCase(any())).thenReturn(initialContactLink)
        whenever(getMyAvatarFileUseCase(any())).thenReturn(localAvatarFile)
        whenever(getUserFullNameUseCase(any())).thenReturn("FullName")
        whenever(getMyAvatarColorUseCase()).thenReturn(0xFFFFF)

        whenever(
            avatarContentMapper.invoke(
                fullName = "FullName",
                localFile = localAvatarFile,
                showBorder = true,
                textSize = 38.sp,
                backgroundColor = 0xFFFFF
            )
        ).thenReturn(PhotoAvatarContent(path = "photo_path", size = 1L, showBorder = true))
        underTest.createQRCode()
    }
}