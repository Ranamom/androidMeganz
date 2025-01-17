package test.mega.privacy.android.app.presentation.meeting.chat.view.sheet

import androidx.activity.ComponentActivity
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.MutableStateFlow
import mega.privacy.android.app.presentation.meeting.chat.view.sheet.ChatGalleryState
import mega.privacy.android.app.presentation.meeting.chat.view.sheet.ChatGalleryViewModel
import mega.privacy.android.app.presentation.meeting.chat.view.sheet.ChatToolbarBottomSheet
import mega.privacy.android.app.presentation.meeting.chat.view.sheet.TEST_TAG_ATTACH_FROM_CONTACT
import mega.privacy.android.app.presentation.meeting.chat.view.sheet.TEST_TAG_ATTACH_FROM_FILE
import mega.privacy.android.app.presentation.meeting.chat.view.sheet.TEST_TAG_ATTACH_FROM_GALLERY
import mega.privacy.android.app.presentation.meeting.chat.view.sheet.TEST_TAG_ATTACH_FROM_GIF
import mega.privacy.android.app.presentation.meeting.chat.view.sheet.TEST_TAG_ATTACH_FROM_LOCATION
import mega.privacy.android.app.presentation.meeting.chat.view.sheet.TEST_TAG_ATTACH_FROM_SCAN
import mega.privacy.android.app.presentation.meeting.chat.view.sheet.TEST_TAG_GALLERY_LIST
import mega.privacy.mobile.analytics.event.ChatConversationContactMenuItemEvent
import mega.privacy.mobile.analytics.event.ChatConversationFileMenuItemEvent
import mega.privacy.mobile.analytics.event.ChatConversationLocationMenuItemEvent
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import test.mega.privacy.android.app.AnalyticsTestRule

@OptIn(ExperimentalMaterialApi::class)
@RunWith(AndroidJUnit4::class)
class ChatToolbarBottomSheetTest {
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()
    private val analyticsRule = AnalyticsTestRule()

    @get:Rule
    val ruleChain: RuleChain = RuleChain.outerRule(analyticsRule).around(composeTestRule)

    private val onAttachFileClicked: () -> Unit = mock()
    private val onAttachContactClicked: () -> Unit = mock()
    private val onPickLocation: () -> Unit = mock()

    private val chatGalleryViewModel = mock<ChatGalleryViewModel> {
        on { state } doReturn MutableStateFlow(ChatGalleryState())
    }

    private val viewModelStore = mock<ViewModelStore> {
        on { get(argThat<String> { contains(ChatGalleryViewModel::class.java.canonicalName.orEmpty()) }) } doReturn chatGalleryViewModel
    }
    private val viewModelStoreOwner = mock<ViewModelStoreOwner> {
        on { viewModelStore } doReturn viewModelStore
    }

    @Test
    fun `test that gallery list shows`() {
        initComposeRuleContent()
        composeTestRule.onNodeWithTag(TEST_TAG_GALLERY_LIST).assertExists()
    }

    @Test
    fun `test that gallery list is shown`() {
        initComposeRuleContent()
        composeTestRule.onNodeWithTag(TEST_TAG_ATTACH_FROM_GALLERY).assertIsDisplayed()
    }

    @Test
    fun `test that file button is shown in chat bottom sheet`() {
        initComposeRuleContent()
        composeTestRule.onNodeWithTag(TEST_TAG_ATTACH_FROM_FILE).assertIsDisplayed()
    }

    @Test
    fun `test that gif button is shown in chat bottom sheet`() {
        initComposeRuleContent()
        composeTestRule.onNodeWithTag(TEST_TAG_ATTACH_FROM_GIF).assertIsDisplayed()
    }

    @Test
    fun `test that scan button is shown in chat bottom sheet`() {
        initComposeRuleContent()
        composeTestRule.onNodeWithTag(TEST_TAG_ATTACH_FROM_SCAN).assertIsDisplayed()
    }

    @Test
    fun `test that location button is shown in chat bottom sheet`() {
        initComposeRuleContent()
        composeTestRule.onNodeWithTag(TEST_TAG_ATTACH_FROM_LOCATION).assertIsDisplayed()
    }

    @Test
    fun `test that contact button is shown in chat bottom sheet`() {
        initComposeRuleContent()
        composeTestRule.onNodeWithTag(TEST_TAG_ATTACH_FROM_CONTACT).assertIsDisplayed()
    }

    @Test
    fun `test that contact button click is passed to upper caller`() {
        initComposeRuleContent()
        composeTestRule.onNodeWithTag(TEST_TAG_ATTACH_FROM_CONTACT).performClick()
        verify(onAttachContactClicked).invoke()
        assertThat(analyticsRule.events).contains(ChatConversationContactMenuItemEvent)
    }

    @Test
    fun `test that file button click is passed to upper caller`() {
        initComposeRuleContent()
        composeTestRule.onNodeWithTag(TEST_TAG_ATTACH_FROM_FILE).performClick()
        verify(onAttachFileClicked).invoke()
        assertThat(analyticsRule.events).contains(ChatConversationFileMenuItemEvent)
    }

    @Test
    fun `test that location button click is passed to upper caller`() {
        initComposeRuleContent()
        composeTestRule.onNodeWithTag(TEST_TAG_ATTACH_FROM_LOCATION).performClick()
        verify(onPickLocation).invoke()
        assertThat(analyticsRule.events).contains(ChatConversationLocationMenuItemEvent)
    }

    private fun initComposeRuleContent() {
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalViewModelStoreOwner provides viewModelStoreOwner
            ) {
                ChatToolbarBottomSheet(
                    onAttachFileClicked = onAttachFileClicked,
                    onAttachContactClicked = onAttachContactClicked,
                    onPickLocation = onPickLocation,
                    onSendGiphyMessage = { },
                    onTakePicture = {},
                    hideSheet = {},
                    isVisible = true,
                )
            }
        }
    }
}