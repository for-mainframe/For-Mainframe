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

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import java.awt.Component

// TODO: doc
abstract class StatefulDialog<T : Any>(
  project: Project? = null,
  parentComponent: Component? = null,
  canBeParent: Boolean = true,
  ideModalityType: IdeModalityType = IdeModalityType.IDE,
  createSouth: Boolean = true
) : DialogWrapper(
  project,
  parentComponent,
  canBeParent,
  ideModalityType,
  createSouth
), StatefulComponent<T>

/**
 * Show dialog until the associated process is finished or cancel is clicked
 * @param initialState the state to initialize the dialog
 * @param factory the factory to create the dialog
 * @param test the associated process to be handled when the dialog is open
 */
fun <T : Any> showUntilDone(
  initialState: T,
  factory: (state: T) -> StatefulDialog<T>,
  test: (T) -> Boolean
): T? {
  var dialog: StatefulDialog<T>
  var stateToInitializeDialog = initialState
  while (true) {
    dialog = factory(stateToInitializeDialog)
    if (dialog.showAndGet()) {
      stateToInitializeDialog = dialog.state
      if (test(stateToInitializeDialog)) {
        return stateToInitializeDialog
      }
    } else {
      break
    }
  }
  return null
}