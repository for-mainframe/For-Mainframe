package eu.ibagroup.formainframe.config.jobs

import com.intellij.openapi.actionSystem.ActionToolbarPosition
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.layout.panel
import eu.ibagroup.formainframe.common.message
import eu.ibagroup.formainframe.common.ui.DEFAULT_ROW_HEIGHT
import eu.ibagroup.formainframe.common.ui.DialogMode
import eu.ibagroup.formainframe.common.ui.ValidatingTableView
import eu.ibagroup.formainframe.common.ui.toolbarTable
import eu.ibagroup.formainframe.config.*
import eu.ibagroup.formainframe.config.ws.WorkingSetConfig
import eu.ibagroup.formainframe.config.ws.ui.WorkingSetDialog
import eu.ibagroup.formainframe.config.ws.ui.initEmptyUuids
import eu.ibagroup.formainframe.config.ws.ui.toDialogState
import eu.ibagroup.formainframe.utils.crudable.nextUniqueValue
import eu.ibagroup.formainframe.utils.isThe
import javax.swing.JPanel

class JobsConfigurable : BoundSearchableConfigurable("Job Filters", "mainframe") {

  private lateinit var panel: JPanel

  override fun createPanel(): DialogPanel {
    val jobsFilterTable = JobsFilterTableModel(sandboxCrudable)
    val validatingTableView = ValidatingTableView(jobsFilterTable, disposable!!).apply {
      rowHeight = DEFAULT_ROW_HEIGHT
    }
    ApplicationManager.getApplication()
      .messageBus
      .connect(disposable!!)
      .subscribe(SandboxListener.TOPIC, object : SandboxListener {
        override fun <E : Any> update(clazz: Class<out E>) {
        }
        override fun <E : Any> reload(clazz: Class<out E>) {
          if (clazz.isThe<JobsFilter>()) {
            jobsFilterTable.reinitialize()
          }
        }
      })
    return panel {
      row {
        cell(isVerticalFlow = true, isFullWidth = false) {
          toolbarTable(message("configurable.ws.tables.ws.title"), validatingTableView) {
            addNewItemProducer { JobsFilter() }
            configureDecorator {
              disableUpDownActions()
              setAddAction {
                val dialog = JobsFilterDialog(sandboxCrudable)
                if (dialog.showAndGet()) {
                  val state = dialog.state
                  state.uuid = sandboxCrudable.nextUniqueValue<JobsFilter,String>()
                  jobsFilterTable.addRow(state)
                  jobsFilterTable.reinitialize()
                }
              }
              setEditAction {
                validatingTableView.selectedObject?.let { selected ->
                  JobsFilterDialog(crudable = sandboxCrudable, state = selected).apply {
                    if (showAndGet()) {
                      val idx = validatingTableView.selectedRow
                      jobsFilterTable[idx] = state
                      jobsFilterTable.reinitialize()
                    }
                  }
                }
              }
              setToolbarPosition(ActionToolbarPosition.BOTTOM)
            }
          }
        }
      }
    }.also { panel = it }

  }

  override fun isModified(): Boolean {
    return isSandboxModified<JobsFilter>()
  }

  override fun apply() {
    val wasModified = isModified
    applySandbox<JobsFilter>()
    if (wasModified) {
      panel.updateUI()
    }
  }

  override fun reset() {
    val wasModified = isModified
    rollbackSandbox<JobsFilter>()
    if (wasModified) {
      panel.updateUI()
    }
  }

  override fun cancel() {
    reset()
  }

}