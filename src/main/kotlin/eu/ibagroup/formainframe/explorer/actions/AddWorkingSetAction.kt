/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package eu.ibagroup.formainframe.explorer.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import eu.ibagroup.formainframe.config.ws.WorkingSetConfig
import eu.ibagroup.formainframe.config.ws.ui.AbstractWsDialog
import eu.ibagroup.formainframe.config.ws.ui.AbstractWsDialogState
import eu.ibagroup.formainframe.config.ws.ui.FilesWorkingSetDialogState
import eu.ibagroup.formainframe.config.ws.ui.files.FilesWorkingSetDialog
import eu.ibagroup.formainframe.config.ws.ui.initEmptyUuids
import eu.ibagroup.formainframe.explorer.ui.FILE_EXPLORER_VIEW
import eu.ibagroup.formainframe.explorer.ui.JES_EXPLORER_VIEW
import eu.ibagroup.formainframe.utils.crudable.Crudable

/**
 * Implementation of AddWsActionBase for files working sets.
 * @see AddWsActionBase
 * @author Valiantsin Krus
 */
class AddWorkingSetAction : AddWsActionBase() {
  override val explorerView = FILE_EXPLORER_VIEW
  override val presentationTextInExplorer = "Working Set"
  override val defaultPresentationText = "Create Working Set"

  /**
   * Creates dialog for Files Working Set.
   * @see AddWsActionBase.createDialog
   */
  override fun createDialog(configCrudable: Crudable): AbstractWsDialog<*, *, out AbstractWsDialogState<out WorkingSetConfig, *>> {
    return FilesWorkingSetDialog(configCrudable, FilesWorkingSetDialogState().initEmptyUuids(configCrudable))
  }

  /**
   * Does the same as the super method but disable action for JES Explorer.
   * @see AddWsActionBase.update
   */
  override fun update(e: AnActionEvent) {
    super.update(e)
    if (e.getData(JES_EXPLORER_VIEW) != null) {
      e.presentation.isEnabledAndVisible = false
    }
  }
}
