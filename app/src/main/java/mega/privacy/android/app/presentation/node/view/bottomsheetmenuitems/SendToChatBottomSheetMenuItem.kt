package mega.privacy.android.app.presentation.node.view.bottomsheetmenuitems

import mega.privacy.android.app.presentation.node.model.menuaction.SendToChatMenuAction
import mega.privacy.android.core.ui.model.MenuActionWithIcon
import mega.privacy.android.domain.entity.node.TypedFileNode
import mega.privacy.android.domain.entity.node.TypedNode
import mega.privacy.android.domain.entity.shares.AccessPermission
import javax.inject.Inject

/**
 * Send to chat bottom sheet menu item
 *
 * @param menuAction [SendToChatMenuAction]
 */
class SendToChatBottomSheetMenuItem @Inject constructor(
    override val menuAction: SendToChatMenuAction,
) : NodeBottomSheetMenuItem<MenuActionWithIcon> {
    override suspend fun shouldDisplay(
        isNodeInRubbish: Boolean,
        accessPermission: AccessPermission?,
        isInBackups: Boolean,
        node: TypedNode,
        isConnected: Boolean,
    ) = isConnected
            && node is TypedFileNode
            && node.isTakenDown.not()
            && isNodeInRubbish.not()

    override val groupId = 7
}