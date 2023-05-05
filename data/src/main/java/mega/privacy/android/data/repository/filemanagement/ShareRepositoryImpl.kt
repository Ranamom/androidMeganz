package mega.privacy.android.data.repository.filemanagement

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import mega.privacy.android.data.gateway.api.MegaApiGateway
import mega.privacy.android.data.mapper.SortOrderIntMapper
import mega.privacy.android.data.mapper.node.NodeMapper
import mega.privacy.android.domain.entity.SortOrder
import mega.privacy.android.domain.qualifier.IoDispatcher
import mega.privacy.android.domain.repository.filemanagement.ShareRepository
import javax.inject.Inject

/**
 * Share repository impl
 *
 * @property megaApiGateway
 * @property nodeMapper
 * @property sortOrderIntMapper
 * @property ioDispatcher
 * @constructor Create empty Share repository impl
 */
internal class ShareRepositoryImpl @Inject constructor(
    private val megaApiGateway: MegaApiGateway,
    private val nodeMapper: NodeMapper,
    private val sortOrderIntMapper: SortOrderIntMapper,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ShareRepository {

    override suspend fun getPublicLinks(sortOrder: SortOrder) =
        withContext(ioDispatcher) {
            megaApiGateway.getPublicLinks(sortOrderIntMapper(sortOrder)).map { nodeMapper(it) }
        }
}