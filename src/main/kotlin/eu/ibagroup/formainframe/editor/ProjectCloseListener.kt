package eu.ibagroup.formainframe.editor

import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.ProjectManagerListener
import com.intellij.openapi.project.VetoableProjectManagerListener

//private val log = log<FileEditorEventsListener>()

// TODO: implement as soon as the syncronizer will be rewritten
class ProjectCloseListener : ProjectManagerListener {
  init {
    val projListener = object : VetoableProjectManagerListener {
      override fun canClose(project: Project): Boolean {
//        val configService = service<ConfigService>()
//        if (!configService.isAutoSyncEnabled.get() && ApplicationManager.getApplication().isActive) {
//          val openFiles = project.component<FileEditorManager>().openFiles
//          if (openFiles.isNotEmpty()) {
//            openFiles.forEach { file ->
//              val document = FileDocumentManager.getInstance().getDocument(file) ?: let {
//                log.info("Document cannot be used here")
//                return@forEach
//              }
//              if (showSyncOnCloseDialog(file.name, project)) {
//                runModalTask(
//                  title = "Syncing ${file.name}",
//                  project = project,
//                  cancellable = true
//                ) {
//                  runInEdt {
//                    FileDocumentManager.getInstance().saveDocument(document)
//                    service<DataOpsManager>().getContentSynchronizer(file)?.userSync(file)
//                  }
//                }
//              }
//            }
//          }
//        }
        return true
      }
    }
    ProjectManager.getInstance().addProjectManagerListener(projListener)
  }
}
