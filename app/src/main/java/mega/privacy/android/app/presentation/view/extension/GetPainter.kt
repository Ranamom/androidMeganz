package mega.privacy.android.app.presentation.view.extension

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import mega.privacy.android.app.presentation.fileinfo.model.getNodeIcon
import mega.privacy.android.domain.entity.node.TypedNode
import mega.privacy.android.core.ui.mapper.FileTypeIconMapper

/**
 * This method will return different type of folder icons based on their type
 */
@Composable
internal fun TypedNode.getPainter(fileTypeIconMapper: FileTypeIconMapper): Painter =
    painterResource(id = getIcon(fileTypeIconMapper))

@Composable
internal fun TypedNode.getIcon(fileTypeIconMapper: FileTypeIconMapper): Int {
    return getNodeIcon(
        typedNode = this,
        originShares = false,
        fileTypeIconMapper = fileTypeIconMapper
    )
}