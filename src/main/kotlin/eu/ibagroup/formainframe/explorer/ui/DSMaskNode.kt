package eu.ibagroup.formainframe.explorer.ui

import com.intellij.icons.AllIcons
import com.intellij.ide.projectView.PresentationData
import com.intellij.ide.util.treeView.AbstractTreeNode
import com.intellij.ui.SimpleTextAttributes
import com.intellij.util.containers.toMutableSmartList
import eu.ibagroup.formainframe.config.ws.DSMask
import eu.ibagroup.formainframe.dataops.fetch.RemoteQuery
import eu.ibagroup.formainframe.dataops.fetch.RemoteQueryImpl
import eu.ibagroup.formainframe.explorer.ExplorerViewSettings
import eu.ibagroup.formainframe.explorer.WorkingSet
import eu.ibagroup.formainframe.vfs.MFVirtualFile

class DSMaskNode(
  private val dsMask: DSMask,
  workingSet: WorkingSet,
  viewSettings: ExplorerViewSettings
) : RemoteMFFileCacheNode<DSMask, DSMask, WorkingSet>(dsMask, workingSet, viewSettings) {

  override fun update(presentation: PresentationData) {
    presentation.addText(dsMask.mask, SimpleTextAttributes.REGULAR_ATTRIBUTES)
    presentation.addText(" ${dsMask.volser}", SimpleTextAttributes.GRAYED_ATTRIBUTES)
    presentation.setIcon(AllIcons.Nodes.Module)
  }

  override val query: RemoteQuery<DSMask>?
    get() {
      val connectionConfig = unit.connectionConfig
      val urlConnection = unit.urlConnection
      return if (connectionConfig != null && urlConnection != null) {
        RemoteQueryImpl(dsMask, connectionConfig, urlConnection)
      } else null
    }

  override fun Collection<MFVirtualFile>.toChildrenNodes(): MutableList<AbstractTreeNode<*>> {
    return map {
      if (it.isDirectory) {
        LibraryNode(it, unit, viewSettings)
      } else {
        FileLikeDatasetFileNode(it, unit, viewSettings)
      }
    }.toMutableSmartList()
  }

  override val requestClass = DSMask::class.java



}