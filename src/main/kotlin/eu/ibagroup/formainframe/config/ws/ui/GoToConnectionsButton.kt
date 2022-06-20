/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package eu.ibagroup.formainframe.config.ws.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.ui.AnActionButton
import com.intellij.ui.table.TableView
import eu.ibagroup.formainframe.common.message
import eu.ibagroup.formainframe.common.ui.ValidatingTableView
import eu.ibagroup.formainframe.common.ui.getColumnIndexByName
import eu.ibagroup.formainframe.config.ws.FilesWorkingSetConfig
import eu.ibagroup.formainframe.config.ws.ui.files.WSTableModel
import java.util.function.Supplier

fun goToConnectionsButton(
  table: TableView<FilesWorkingSetConfig>?,
  wsTableModel: WSTableModel
): AnActionButton {

  val getTitle = Supplier {
    if (table != null) {
      val connectionName = table.selectedObject?.name
      if (!connectionName.isNullOrBlank()) {
        val row = table.selectedRow
        val usernameIndex =
          wsTableModel.getColumnIndexByName(message("configurable.ws.tables.ws.username.name"))
        val urlIndex = wsTableModel.getColumnIndexByName(message("configurable.ws.tables.ws.url.name"))
        return@Supplier when {
          wsTableModel.validationInfos[row, urlIndex] != null -> {
            message(
              "configurable.ws.tables.actions.connection.selected.fix.url",
              connectionName
            )
          }
          wsTableModel.validationInfos[row, usernameIndex] != null -> {
            message(
              "configurable.ws.tables.actions.connection.selected.fix.credentials",
              connectionName
            )
          }
          else -> {
            message("configurable.ws.tables.actions.connection.selected", connectionName)
          }
        }
      }
    }
    message("configurable.ws.tables.actions.connection.default")
  }


  return object : AnActionButton(getTitle, getTitle, AllIcons.General.Web) {

    val table by lazy { this.contextComponent as ValidatingTableView<*> }

    override fun isEnabled(): Boolean {
      return this.table.selectedObject != null
    }

    override fun displayTextInToolbar(): Boolean {
      return true
    }

    override fun actionPerformed(e: AnActionEvent) {
    }

  }.apply {
    addCustomUpdater {
      this.table.selectedObject != null
    }
  }

}
