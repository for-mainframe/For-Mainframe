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

import eu.ibagroup.formainframe.config.ws.FilesWorkingSetConfig
import eu.ibagroup.formainframe.config.ws.ui.AbstractWsTableModel
import eu.ibagroup.formainframe.utils.crudable.Crudable

/**
 * Table model for Files Working Set configuration table.
 * @see AbstractWsTableModel
 * @author Valiantsin Krus
 * @author Viktar Mushtsin
 */
class WSTableModel(crudable: Crudable) : AbstractWsTableModel<FilesWorkingSetConfig>(crudable) {

  override val clazz = FilesWorkingSetConfig::class.java

  override operator fun set(row: Int, item: FilesWorkingSetConfig) {
    get(row).dsMasks = item.dsMasks
    get(row).ussPaths = item.ussPaths
    super.set(row, item)
  }

  init {
    initialize()
  }
}
