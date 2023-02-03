/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package eu.ibagroup.formainframe.config.ws.ui.jes

import com.intellij.util.containers.toMutableSmartList
import eu.ibagroup.formainframe.common.ui.DialogMode
import eu.ibagroup.formainframe.config.connect.ConnectionConfig
import eu.ibagroup.formainframe.config.sandboxCrudable
import eu.ibagroup.formainframe.config.ws.JesWorkingSetConfig
import eu.ibagroup.formainframe.config.ws.JobFilterState
import eu.ibagroup.formainframe.config.ws.ui.AbstractWsConfigurable
import eu.ibagroup.formainframe.config.ws.ui.JesWorkingSetDialogState
import eu.ibagroup.formainframe.utils.crudable.Crudable

/**
 * Implementation of AbstractWsConfigurable class for modifying JES Working Set configurations.
 * @see AbstractWsConfigurable
 */
class JesWsConfigurable
  : AbstractWsConfigurable<JesWorkingSetConfig, JesWsTableModel<ConnectionConfig>, JesWorkingSetDialogState>("JES Working Sets") {
  override val wsConfigClass = JesWorkingSetConfig::class.java
  override val wsTableModel = JesWsTableModel(sandboxCrudable, ConnectionConfig::class.java)

  override fun emptyConfig() = JesWorkingSetConfig()

  /**
   * Creates JesWorkingSetDialogState based on data of JesWorkingSetConfig.
   */
  override fun JesWorkingSetConfig.toDialogStateAbstract() = this.toDialogState()

  /**
   * Creates and shows dialog for adding JES Working Set.
   * @param crudable crudable to modify after applying dialog.
   * @param state state of dialog.
   */
  override fun createAddDialog(crudable: Crudable, state: JesWorkingSetDialogState) {
    JesWsDialog(sandboxCrudable, state)
      .apply {
        if (showAndGet()) {
          wsTableModel.addRow(state.workingSetConfig)
          wsTableModel.reinitialize()
        }
      }
  }

  /**
   * Creates and shows dialog for editing JES Working Set.
   * @param selected selected working set dialog state
   */
  override fun createEditDialog(selected: JesWorkingSetDialogState) {
    JesWsDialog(
      sandboxCrudable,
      selected.apply { mode = DialogMode.UPDATE }).apply {
      if (showAndGet()) {
        val idx = wsTable.selectedRow
        val res = state.workingSetConfig
        wsTableModel[idx] = res
        wsTableModel.reinitialize()
      }
    }
  }


}

/**
 * Creates JesWorkingSetDialogState based on data of JesWorkingSetConfig.
 */
fun JesWorkingSetConfig.toDialogState(): JesWorkingSetDialogState {
  return JesWorkingSetDialogState(
    uuid = this.uuid,
    connectionUuid = this.connectionConfigUuid,
    workingSetName = this.name,
    maskRow = this.jobsFilters.map { JobFilterState(it.prefix, it.owner, it.jobId) }
      .toMutableSmartList()
  )
}
