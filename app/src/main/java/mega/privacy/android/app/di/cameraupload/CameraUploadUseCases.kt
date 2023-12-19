package mega.privacy.android.app.di.cameraupload

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.components.SingletonComponent
import mega.privacy.android.app.di.GetNodeModule
import mega.privacy.android.app.domain.usecase.DefaultGetCameraUploadFolderName
import mega.privacy.android.app.domain.usecase.GetParentMegaNode
import mega.privacy.android.data.repository.MegaNodeRepository
import mega.privacy.android.domain.repository.CameraUploadRepository
import mega.privacy.android.domain.repository.FileSystemRepository
import mega.privacy.android.domain.repository.NodeRepository
import mega.privacy.android.domain.usecase.BroadcastCameraUploadProgress
import mega.privacy.android.domain.usecase.CheckEnableCameraUploadsStatus
import mega.privacy.android.domain.usecase.ClearCacheDirectory
import mega.privacy.android.domain.usecase.CreateCameraUploadFolder
import mega.privacy.android.domain.usecase.DefaultCheckEnableCameraUploadsStatus
import mega.privacy.android.domain.usecase.DefaultDisableMediaUploadSettings
import mega.privacy.android.domain.usecase.DefaultIsChargingRequired
import mega.privacy.android.domain.usecase.DefaultIsNotEnoughQuota
import mega.privacy.android.domain.usecase.DefaultRenamePrimaryFolder
import mega.privacy.android.domain.usecase.DefaultRenameSecondaryFolder
import mega.privacy.android.domain.usecase.DefaultSetPrimarySyncHandle
import mega.privacy.android.domain.usecase.DefaultSetSecondarySyncHandle
import mega.privacy.android.domain.usecase.DisableMediaUploadSettings
import mega.privacy.android.domain.usecase.GetCameraUploadFolderName
import mega.privacy.android.domain.usecase.HasCredentials
import mega.privacy.android.domain.usecase.IsChargingRequired
import mega.privacy.android.domain.usecase.IsNodeInRubbish
import mega.privacy.android.domain.usecase.IsNotEnoughQuota
import mega.privacy.android.domain.usecase.MonitorBatteryInfo
import mega.privacy.android.domain.usecase.MonitorCameraUploadProgress
import mega.privacy.android.domain.usecase.MonitorChargingStoppedState
import mega.privacy.android.domain.usecase.RenamePrimaryFolder
import mega.privacy.android.domain.usecase.RenameSecondaryFolder
import mega.privacy.android.domain.usecase.SetPrimarySyncHandle
import mega.privacy.android.domain.usecase.SetSecondarySyncHandle

/**
 * Provides the use case implementation for camera upload
 */
@Module(includes = [GetNodeModule::class])
@InstallIn(SingletonComponent::class, ViewModelComponent::class, ServiceComponent::class)
abstract class CameraUploadUseCases {

    companion object {
        /**
         * Provide the [HasCredentials] implementation
         */
        @Provides
        fun provideHasCredentials(cameraUploadRepository: CameraUploadRepository): HasCredentials =
            HasCredentials(cameraUploadRepository::doCredentialsExist)

        /**
         * Provide the [GetParentMegaNode] implementation
         */
        @Provides
        fun provideGetParentMegaNode(megaNodeRepository: MegaNodeRepository): GetParentMegaNode =
            GetParentMegaNode(megaNodeRepository::getParentNode)

        /**
         * Provide the [IsNodeInRubbish] implementation
         */
        @Provides
        fun provideIsNodeInRubbish(nodeRepository: NodeRepository): IsNodeInRubbish =
            IsNodeInRubbish(nodeRepository::isNodeInRubbish)

        /**
         * Provide the [ClearCacheDirectory] implementation
         */
        @Provides
        fun provideClearCacheDirectory(cameraUploadRepository: CameraUploadRepository): ClearCacheDirectory =
            ClearCacheDirectory(cameraUploadRepository::clearCacheDirectory)

        /**
         * Provide the [MonitorCameraUploadProgress] implementation
         */
        @Provides
        fun provideMonitorCameraUploadProgress(cameraUploadRepository: CameraUploadRepository): MonitorCameraUploadProgress =
            MonitorCameraUploadProgress(cameraUploadRepository::monitorCameraUploadProgress)

        /**
         * Provide the [BroadcastCameraUploadProgress] implementation
         */
        @Provides
        fun provideBroadcastCameraUploadProgress(cameraUploadRepository: CameraUploadRepository): BroadcastCameraUploadProgress =
            BroadcastCameraUploadProgress(cameraUploadRepository::broadcastCameraUploadProgress)

        /**
         * Provide the [MonitorBatteryInfo] implementation
         */
        @Provides
        fun provideMonitorBatteryInfo(cameraUploadRepository: CameraUploadRepository): MonitorBatteryInfo =
            MonitorBatteryInfo(cameraUploadRepository::monitorBatteryInfo)

        /**
         * Provide the [MonitorChargingStoppedState] implementation
         */
        @Provides
        fun provideMonitorChargingStoppedState(cameraUploadRepository: CameraUploadRepository): MonitorChargingStoppedState =
            MonitorChargingStoppedState(cameraUploadRepository::monitorChargingStoppedInfo)

        /**
         * Provide the [CreateCameraUploadFolder] implementation
         */
        @Provides
        fun provideCreateCameraUploadFolder(fileSystemRepository: FileSystemRepository): CreateCameraUploadFolder =
            CreateCameraUploadFolder(fileSystemRepository::createFolder)
    }

    /**
     * Provides the [CheckEnableCameraUploadsStatus] implementation
     */
    @Binds
    abstract fun bindCheckEnableCameraUploadsStatus(useCase: DefaultCheckEnableCameraUploadsStatus): CheckEnableCameraUploadsStatus

    /**
     * Provide the [SetPrimarySyncHandle] implementation
     */
    @Binds
    abstract fun bindSetPrimarySyncHandle(setPrimarySyncHandle: DefaultSetPrimarySyncHandle): SetPrimarySyncHandle

    /**
     * Provide the [SetSecondarySyncHandle] implementation
     */
    @Binds
    abstract fun bindSetSecondarySyncHandle(setSecondarySyncHandle: DefaultSetSecondarySyncHandle): SetSecondarySyncHandle

    /**
     * Provide the [GetCameraUploadFolderName] implementation
     */
    @Binds
    abstract fun bindGetCameraUploadFolderName(getCameraUploadFolderName: DefaultGetCameraUploadFolderName): GetCameraUploadFolderName

    /**
     * Provide the [RenamePrimaryFolder] implementation
     */
    @Binds
    abstract fun bindRenamePrimaryFolder(renamePrimaryFolder: DefaultRenamePrimaryFolder): RenamePrimaryFolder

    /**
     * Provide the [RenameSecondaryFolder] implementation
     */
    @Binds
    abstract fun bindRenameSecondaryFolder(renameSecondaryFolder: DefaultRenameSecondaryFolder): RenameSecondaryFolder

    /**
     * Provide the [IsChargingRequired] implementation
     */
    @Binds
    abstract fun bindIsChargingRequired(isChargingRequired: DefaultIsChargingRequired): IsChargingRequired

    /**
     * Provide the [IsNotEnoughQuota] implementation
     */
    @Binds
    abstract fun bindIsNotEnoughQuota(isNotEnoughQuota: DefaultIsNotEnoughQuota): IsNotEnoughQuota

    /**
     * Provide the [DisableMediaUploadSettings] implementation
     */
    @Binds
    abstract fun bindDisableMediaUploadSettings(disableMediaUploadSettings: DefaultDisableMediaUploadSettings): DisableMediaUploadSettings
}
