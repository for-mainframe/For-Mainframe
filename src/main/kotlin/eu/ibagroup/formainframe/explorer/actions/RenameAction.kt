package eu.ibagroup.formainframe.explorer.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import eu.ibagroup.formainframe.analytics.AnalyticsService
import eu.ibagroup.formainframe.analytics.events.FileAction
import eu.ibagroup.formainframe.analytics.events.FileEvent
import eu.ibagroup.formainframe.config.configCrudable
import eu.ibagroup.formainframe.config.ws.DSMask
import eu.ibagroup.formainframe.config.ws.WorkingSetConfig
import eu.ibagroup.formainframe.dataops.DataOpsManager
import eu.ibagroup.formainframe.dataops.attributes.RemoteDatasetAttributes
import eu.ibagroup.formainframe.dataops.attributes.RemoteMemberAttributes
import eu.ibagroup.formainframe.dataops.attributes.RemoteUssAttributes
import eu.ibagroup.formainframe.dataops.attributes.FileAttributes
import eu.ibagroup.formainframe.dataops.operations.RenameOperation
import eu.ibagroup.formainframe.explorer.WorkingSet
import eu.ibagroup.formainframe.explorer.ui.*
import eu.ibagroup.formainframe.utils.*
import eu.ibagroup.formainframe.utils.crudable.getByUniqueKey

class RenameAction : AnAction() {

  override fun actionPerformed(e: AnActionEvent) {
    val view = e.getData(FILE_EXPLORER_VIEW) ?: return
    val selectedNode = view.mySelectedNodesData[0]
    val node = selectedNode.node
    var initialState = ""
    if (node is DSMaskNode) {
      initialState = (selectedNode.node.value as DSMask).mask
      val dialog = RenameDialog(e.project, "Dataset Mask", initialState).withValidationOnInput {
        validateDatasetMask(it.text, it)
      }.withValidationForBlankOnApply()
      if (dialog.showAndGet()) {
        val parentValue = selectedNode.node.parent?.value as WorkingSet
        val wsToUpdate = configCrudable.getByUniqueKey<WorkingSetConfig>(parentValue.uuid)?.clone()
        if (wsToUpdate != null) {
          wsToUpdate.dsMasks.filter { it.mask == initialState }[0].mask = dialog.state
          configCrudable.update(wsToUpdate)
        }
      }
    } else if (node is LibraryNode || node is FileLikeDatasetNode) {
      val attributes = selectedNode.attributes ?: return
      var type = ""
      if (attributes is RemoteDatasetAttributes) {
        initialState = attributes.datasetInfo.name
        type = "Dataset"
      } else if (attributes is RemoteMemberAttributes) {
        initialState = attributes.info.name
        type = "Member"
      }
      val dialog = RenameDialog(e.project, type, initialState).withValidationOnInput {
        if (attributes is RemoteDatasetAttributes) {
          validateDatasetNameOnInput(it)
        } else {
          validateMemberName(it)
        }
      }.withValidationForBlankOnApply()
      val file = node.virtualFile
      if (dialog.showAndGet() && file != null) {
        runRenameOperation(e.project, file, attributes, dialog.state, node)
        service<AnalyticsService>().trackAnalyticsEvent(FileEvent(attributes, FileAction.RENAME))
      }
    } else if (selectedNode.node is UssDirNode && selectedNode.node.isConfigUssPath) {
      initialState = selectedNode.node.value.path
      val dialog = RenameDialog(e.project, "Directory", initialState).withValidationOnInput {
        validateUssMask(it.text, it)
      }.withValidationForBlankOnApply()
      if (dialog.showAndGet()) {
        val parentValue = selectedNode.node.parent?.value as WorkingSet
        val wsToUpdate = configCrudable.getByUniqueKey<WorkingSetConfig>(parentValue.uuid)?.clone()
        if (wsToUpdate != null) {
          wsToUpdate.ussPaths.filter { it.path == initialState }[0].path = dialog.state
          configCrudable.update(wsToUpdate)
        }
      }

    } else if (selectedNode.node is UssDirNode || selectedNode.node is UssFileNode) {
      val attributes = selectedNode.attributes as RemoteUssAttributes
      val file = selectedNode.file
      val dialog = RenameDialog(
        e.project,
        if (attributes.isDirectory) "Directory" else "File",
        attributes.name
      ).withValidationOnInput {
        validateUssFileName(it)
      }.withValidationForBlankOnApply()
      if (dialog.showAndGet() && file != null) {
        runRenameOperation(e.project, file, attributes, dialog.state, node)
        service<AnalyticsService>().trackAnalyticsEvent(FileEvent(attributes, FileAction.RENAME))
      }
    }
  }

  private fun runRenameOperation(
    project: Project?,
    file: VirtualFile,
    attributes: FileAttributes,
    newName: String,
    node: ExplorerTreeNode<*>
  ) {
    runBackgroundableTask(
      title = "Renaming file ${file.name} to $newName",
      project = project,
      cancellable = true
    ) {
      runCatching {
        node.explorer.componentManager.service<DataOpsManager>().performOperation(
          operation = RenameOperation(
            file = file,
            attributes = attributes,
            newName = newName
          ),
          progressIndicator = it
        )
      }.onSuccess {
        node.parent?.cleanCacheIfPossible()
      }.onFailure {
        node.explorer.reportThrowable(it, project)
      }
    }
  }

  override fun update(e: AnActionEvent) {
    val view = e.getData(FILE_EXPLORER_VIEW) ?: let {
      e.presentation.isEnabledAndVisible = false
      return
    }
    val selectedNodes = view.mySelectedNodesData
    e.presentation.isEnabledAndVisible = if (selectedNodes.size == 1 && selectedNodes[0].node !is WorkingSetNode) {
      val file = selectedNodes[0].file
      var isMigrated = false
      if (file != null) {
        val attributes = service<DataOpsManager>().tryToGetAttributes(file) as? RemoteDatasetAttributes
        isMigrated = attributes?.isMigrated ?: false
      }
      !isMigrated
    } else {
      false
    }
    if (e.presentation.isEnabledAndVisible) {
      val selectedNode = selectedNodes[0]
      if (selectedNode.node is DSMaskNode || (selectedNode.node is UssDirNode && selectedNode.node.isConfigUssPath)) {
        e.presentation.text = "Edit"
      } else {
        e.presentation.text = "Rename"
      }
    }
  }


  override fun isDumbAware(): Boolean {
    return true
  }
}