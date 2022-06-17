/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package eu.ibagroup.formainframe.config.ws.ui.files

import com.intellij.util.containers.toMutableSmartList
import eu.ibagroup.formainframe.common.ui.DialogMode
import eu.ibagroup.formainframe.config.sandboxCrudable
import eu.ibagroup.formainframe.config.ws.FilesWorkingSetConfig
import eu.ibagroup.formainframe.config.ws.ui.AbstractWsConfigurable
import eu.ibagroup.formainframe.config.ws.ui.WorkingSetDialogState
import eu.ibagroup.formainframe.utils.crudable.Crudable

class WSConfigurable :
  AbstractWsConfigurable<FilesWorkingSetConfig, WSTableModel, WorkingSetDialogState>("Working Sets") {
  override val wsConfigClass = FilesWorkingSetConfig::class.java
  override val wsTableModel = WSTableModel(sandboxCrudable)

  override fun emptyConfig() = FilesWorkingSetConfig()

  override fun FilesWorkingSetConfig.toDialogStateAbstract() = this.toDialogState()

  override fun createAddDialog(crudable: Crudable, state: WorkingSetDialogState) {
    WorkingSetDialog(sandboxCrudable, state)
      .apply {
        if (showAndGet()) {
          wsTableModel.addRow(state.workingSetConfig)
          wsTableModel.reinitialize()
        }
      }
  }

  override fun createEditDialog(selected: WorkingSetDialogState) {
    WorkingSetDialog(sandboxCrudable, selected.apply { mode = DialogMode.UPDATE }).apply {
      if (showAndGet()) {
        val idx = wsTable.selectedRow
        wsTableModel[idx] = state.workingSetConfig
        wsTableModel.reinitialize()
      }
    }
  }

}

fun FilesWorkingSetConfig.toDialogState(): WorkingSetDialogState {
  return WorkingSetDialogState(
    uuid = this.uuid,
    connectionUuid = this.connectionConfigUuid,
    workingSetName = this.name,
    maskRow = this.dsMasks.map { WorkingSetDialogState.TableRow(mask = it.mask) }
      .plus(this.ussPaths.map {
        WorkingSetDialogState.TableRow(
          mask = it.path,
          type = WorkingSetDialogState.TableRow.USS
        )
      }).toMutableSmartList(),
  )
}
