package eu.ibagroup.formainframe.dataops.content.synchronizer

import com.intellij.openapi.components.service
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import eu.ibagroup.formainframe.config.ConfigService
import eu.ibagroup.formainframe.dataops.DataOpsManager
import eu.ibagroup.formainframe.utils.castOrNull
import eu.ibagroup.formainframe.vfs.MFVirtualFile

/**
 * Listener that reacts on document saving to local file system and synchronizes it with mainframe.
 * Needed to fully implement auto-save.
 * @author Valentine Krus
 */
class MFAutoSaveDocumentListener: BulkFileListener {
  /**
   * Filters file events for saving events and synchronizes file from these events.
   */
  override fun after(events: MutableList<out VFileEvent>) {
    if (!service<ConfigService>().isAutoSyncEnabled.get()){
      return
    }
    events.forEach {
      if (!it.isFromSave) {
        return
      }
      val vFile = it.file
      val mfFile = vFile.castOrNull<MFVirtualFile>() ?: return
      val dataOpsManager = service<DataOpsManager>()

      if (dataOpsManager.isSyncSupported(mfFile)) {
        val contentSynchronizer = dataOpsManager.getContentSynchronizer(mfFile) ?: return
        runBackgroundableTask("Synchronizing file ${mfFile.name} with mainframe") { indicator ->
          contentSynchronizer.synchronizeWithRemote(DocumentedSyncProvider(mfFile), indicator)
        }
      }
    }
  }
}