package mega.privacy.android.domain.usecase.chat.message

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import mega.privacy.android.domain.entity.chat.ChatMessage
import mega.privacy.android.domain.entity.chat.ChatMessageStatus
import mega.privacy.android.domain.entity.chat.messages.VoiceClipMessage
import mega.privacy.android.domain.entity.chat.messages.invalid.InvalidMessage
import mega.privacy.android.domain.entity.chat.messages.request.CreateTypedMessageRequest
import mega.privacy.android.domain.entity.node.FileNode
import mega.privacy.android.domain.entity.node.FolderNode
import mega.privacy.android.domain.entity.node.chat.ChatDefaultFile
import mega.privacy.android.domain.usecase.node.chat.AddChatFileTypeUseCase
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.whenever
import kotlin.time.Duration.Companion.seconds

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CreateVoiceClipMessageUseCaseTest {
    private lateinit var underTest: CreateVoiceClipMessageUseCase

    private val createInvalidMessageUseCase = mock<CreateInvalidMessageUseCase>()
    private val addChatFileTypeUseCase = mock<AddChatFileTypeUseCase>()

    @BeforeAll
    internal fun setUp() {
        underTest =
            CreateVoiceClipMessageUseCase(
                createInvalidMessageUseCase = createInvalidMessageUseCase,
                addChatFileTypeUseCase = addChatFileTypeUseCase,
            )
    }

    @BeforeEach
    internal fun resetMocks() =
        reset(
            createInvalidMessageUseCase,
            addChatFileTypeUseCase,
        )

    @Test
    fun `test that if message has no nodes it returns an invalid message`() = runTest {
        val message = mock<ChatMessage> {
            on { nodeList } doReturn emptyList()
        }
        val expected = mock<InvalidMessage>()
        whenever(createInvalidMessageUseCase(any())).thenReturn(expected)
        val actual = underTest(buildRequest(message))
        assertThat(actual).isEqualTo(expected)
    }

    @Test
    fun `test that if message has no File nodes it returns an invalid message`() = runTest {
        val message = mock<ChatMessage> {
            on { nodeList } doReturn listOf(mock<FolderNode>())
        }
        val expected = mock<InvalidMessage>()
        whenever(createInvalidMessageUseCase(any())).thenReturn(expected)
        val actual = underTest(buildRequest(message))
        assertThat(actual).isEqualTo(expected)
    }

    @ParameterizedTest(name = "when node exists : {0}")
    @ValueSource(booleans = [true, false])
    fun `test that the use case returns the correctly mapped message`(
        exists: Boolean,
    ) = runTest {
        val fileNode = mock<FileNode>()
        val message = mock<ChatMessage> {
            on { nodeList } doReturn listOf(fileNode)
            on { status } doReturn ChatMessageStatus.UNKNOWN
            on { rowId } doReturn 5432L
        }
        val typedNode = mock<ChatDefaultFile> {
            on { name } doReturn "name"
        }
        val request = buildRequest(message, exists)
        val expected = with(request) {
            VoiceClipMessage(
                chatId = chatId,
                msgId = messageId,
                time = timestamp,
                isDeletable = false,
                isEditable = false,
                isMine = isMine,
                userHandle = userHandle,
                shouldShowAvatar = shouldShowAvatar,
                reactions = emptyList(),
                status = ChatMessageStatus.UNKNOWN,
                content = null,
                fileNode = typedNode,
                name = typedNode.name,
                size = typedNode.size,
                duration = 0.seconds,
                exists = exists,
                rowId = rowId,
            )
        }
        whenever(addChatFileTypeUseCase(fileNode, request.chatId, request.messageId))
            .thenReturn(typedNode)
        val actual = underTest(request)
        assertThat(actual).isEqualTo(expected)
    }

    private fun buildRequest(message: ChatMessage, exists: Boolean = true) =
        CreateTypedMessageRequest(
            chatMessage = message,
            chatId = 123L,
            isMine = true,
            shouldShowAvatar = true,
            reactions = emptyList(),
            exists = exists
        )
}