/*
 * Copyright (c) 2020-2024 IBA Group.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   IBA Group
 *   Zowe Community
 */

package eu.ibagroup.formainframe.config.connect.ui

import com.intellij.util.ui.ColumnInfo
import eu.ibagroup.formainframe.common.message

/**
 * Class represents a connection url column in connection table model.
 * It extends ColumnInfo abstract class and overloads getter and setter methods as values for this column.
 */
class ConnectionUrlColumn<ConnectionState : ConnectionDialogStateBase<*>> : ColumnInfo<ConnectionState, String>(
  message("configurable.connection.table.url")
) {

  /**
   * Overloaded getter method of ColumnInfo abstract class.
   */
  override fun valueOf(item: ConnectionState): String {
    return item.connectionUrl
  }

  /**
   * Overloaded setter method of ColumnInfo abstract class.
   */
  override fun setValue(item: ConnectionState, value: String) {
    item.connectionUrl = value
  }

  override fun getTooltipText(): String {
    return message("configurable.connection.table.url.tooltip")
  }
}
