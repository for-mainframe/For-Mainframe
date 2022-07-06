/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package eu.ibagroup.formainframe.common.ui

import eu.ibagroup.formainframe.utils.castOrNull
import java.awt.Component
import javax.swing.JTable
import javax.swing.table.DefaultTableCellRenderer

// TODO: doc
class ValidatingCellRenderer<Item> : DefaultTableCellRenderer() {

  @Suppress("UNCHECKED_CAST")
  override fun getTableCellRendererComponent(
    table: JTable?,
    value: Any?,
    isSelected: Boolean,
    hasFocus: Boolean,
    row: Int,
    column: Int
  ): Component {
    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
    if (table is ValidatingTableView<*>) {
      table.getCellValidator(column)
        ?.castOrNull<ValidatingColumnInfo<Item>>()
        ?.let { validator ->
          ComponentValidatorBuilder(this, table.disposable)
            .setup {
              withValidator { ->
                table.listTableModel.getItem(row).let { item ->
                  validator.validateEntered(item as Item, this@ValidatingCellRenderer)
                }.apply {
                  icon = getIcon()
                  table.listTableModel.validationInfos[row, column] = this
                  putClientProperty(TOOL_TIP_TEXT_KEY, this?.message)
                }
              }
            }.finish {}.apply {
              revalidate()
            }
        }
    } else {
      icon = null
    }
    return this
  }

}
