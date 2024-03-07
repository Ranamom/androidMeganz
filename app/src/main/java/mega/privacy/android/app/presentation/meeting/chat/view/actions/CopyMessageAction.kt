package mega.privacy.android.app.presentation.meeting.chat.view.actions

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import mega.privacy.android.analytics.Analytics
import mega.privacy.android.app.R
import mega.privacy.android.app.presentation.extensions.copyToClipboard
import mega.privacy.android.domain.entity.chat.messages.TypedMessage
import mega.privacy.android.domain.entity.chat.messages.meta.LocationMessage
import mega.privacy.android.domain.entity.chat.messages.meta.RichPreviewMessage
import mega.privacy.android.domain.entity.chat.messages.normal.NormalMessage
import mega.privacy.mobile.analytics.event.ChatConversationCopyActionMenuItemEvent

internal class CopyMessageAction : MessageAction(
    text = R.string.context_copy,
    icon = R.drawable.ic_icon_copy_medium_regular_outline,
    testTag = "action_copy"
) {
    override fun appliesTo(messages: Set<TypedMessage>) = messages.isNotEmpty() &&
            messages.all { it is NormalMessage || it is RichPreviewMessage || it is LocationMessage }

    @Composable
    override fun OnTrigger(messages: Set<TypedMessage>, onHandled: () -> Unit) {
        Analytics.tracker.trackEvent(ChatConversationCopyActionMenuItemEvent)
        messages.joinToString(separator = "\n") { it.content.orEmpty() }
            .copyToClipboard(LocalContext.current)
        onHandled()
    }
}