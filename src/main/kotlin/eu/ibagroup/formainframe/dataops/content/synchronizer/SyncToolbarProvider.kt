/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package eu.ibagroup.formainframe.dataops.content.synchronizer

import com.intellij.openapi.editor.toolbar.floating.AbstractFloatingToolbarProvider

private const val ACTION_GROUP = "eu.ibagroup.formainframe.dataops.content.synchronizer.SyncActionGroup"

/**
 * Class which serves as extension point for editor floating toolbar provider. Defined in plugin.xml
 */
class SyncToolbarProvider : AbstractFloatingToolbarProvider(ACTION_GROUP) {
  override val autoHideable = true
  override val priority = 1
}
