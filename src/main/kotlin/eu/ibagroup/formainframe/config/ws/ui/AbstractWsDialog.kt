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

package eu.ibagroup.formainframe.config.ws.ui

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.layout.ValidationInfoBuilder
import com.intellij.util.containers.isEmpty
import eu.ibagroup.formainframe.common.ui.StatefulComponent
import eu.ibagroup.formainframe.common.ui.ValidatingTableView
import eu.ibagroup.formainframe.common.ui.tableWithToolbar
import eu.ibagroup.formainframe.config.connect.ConnectionConfig
import eu.ibagroup.formainframe.config.connect.ConnectionConfigBase
import eu.ibagroup.formainframe.config.ws.WorkingSetConfig
import eu.ibagroup.formainframe.utils.*
import eu.ibagroup.formainframe.utils.crudable.Crudable
import java.awt.Dimension
import javax.swing.JComponent

/**
 * Abstract class for displaying configuration dialog of single Working Set.
 * @param Connection The system (such as zosmf, cics etc.) connection class to work with (see [ConnectionConfigBase]).
 * @param WSConfig Implementation class of [WorkingSetConfig].
 * @param TableRow Class with data for each column of filters/masks table (for example mask, system type, ...).
 * @param WSDState Implementation of [AbstractWsDialogState].
 * @param crudable Crudable instance to change data in after dialog applied.
 * @param wsdStateClass Instance of Class for WSDState.
 * @property state Instance of WSDState.
 * @property initialState Initial state of dialog. (used only working set name from initial state ???).
 * @property isSingleConnectionOnlyAllowed is used only in Go-To-Job feature, otherwise it is always false
 * @property connectionConfigToSelect is used only in Go-To-Job feature, otherwise it is always null
 * @author Valiantsin Krus
 * @author Viktar Mushtsin
 */
abstract class AbstractWsDialog<Connection : ConnectionConfigBase, WSConfig : WorkingSetConfig, TableRow, WSDState : AbstractWsDialogState<WSConfig, TableRow>>(
  crudable: Crudable,
  wsdStateClass: Class<out WSDState>,
  override var state: WSDState,
  var initialState: WSDState = state.clone(wsdStateClass),
  open val isSingleConnectionOnlyAllowed: Boolean = false,
  open val connectionConfigToSelect: Connection? = null
) : DialogWrapper(false), StatefulComponent<WSDState> {

  companion object {

    // TODO: Remove when it becomes possible to mock class constructor with init section.
    /** Wrapper for init() method. It is necessary only for test purposes for now. */
    private fun initialize(init: () -> Unit) {
      init()
    }
  }

  abstract val wsConfigClass: Class<out WSConfig>
  abstract val connectionClass: Class<out Connection>

  private val connectionComboBoxModel by lazy { CollectionComboBoxModel(crudable.getAll(connectionClass).toList()) }

  /**
   * Name of masks table.
   */
  abstract val tableTitle: String

  /**
   * Represents table of files masks (for Files Working Sets) or job filters (for JES Working Sets)
   */
  abstract val masksTable: ValidatingTableView<TableRow>

  /**
   * Value of text label before Working Set Name input field.
   */
  abstract val wsNameLabel: String

  abstract fun emptyTableRow(): TableRow

  abstract fun validateOnApply(validationBuilder: ValidationInfoBuilder, component: JComponent): ValidationInfo?

  /**
   * Custom apply for specific WorkingSet implementation (e.g. Files Working Set, JES Working Set).
   * @param state State, modified from dialog, to apply.
   * @return applied state.
   */
  open fun onWSApplied(state: WSDState): WSDState = state

  private val panel by lazy {
    panel {
      row {
        label("$wsNameLabel: ")
        textField()
          .bindText(state::workingSetName)
          .validationOnApply {
            validateForBlank(it) ?: validateWorkingSetName(
              it,
              initialState.workingSetName.ifBlank { null },
              crudable,
              wsConfigClass
            )
          }
          .focused()
      }.layout(RowLayout.LABEL_ALIGNED)
      row {
        label("z/OSMF Connection: ")
        comboBox(connectionComboBoxModel, SimpleListCellRenderer.create("") { it?.name })
          .apply {
            if (isSingleConnectionOnlyAllowed) {
              bindItem(
                { return@bindItem connectionConfigToSelect?.also { state.connectionUuid = it.uuid } },
                { config -> state.connectionUuid = config?.uuid ?: "" }
              )
                .enabled(false)
            } else {
              bindItem(
                {
                  return@bindItem crudable.getByUniqueKey(connectionClass, state.connectionUuid).nullable
                    ?: if (!crudable.getAll(connectionClass).isEmpty()) {
                      crudable.getAll(connectionClass).findAny().nullable?.also {
                        state.connectionUuid = it.uuid
                      }
                    } else {
                      null
                    }
                },
                { config -> state.connectionUuid = config?.uuid ?: "" }
              ).applyToComponent {
                addActionListener {
                  state.connectionUuid = (selectedItem as ConnectionConfig).uuid
                }
              }
                .validationOnApply { validateConnectionSelection(it) }
            }
          }
      }.layout(RowLayout.LABEL_ALIGNED)
      group(tableTitle, false) {
        row {
          tableWithToolbar(masksTable, addDefaultActions = true) {
            addNewItemProducer { emptyTableRow() }
          }
            .validationRequestor { }
            .validationOnApply { validateOnApply(this, it) }
            .onApply {
              state.maskRow = masksTable.items
              state = onWSApplied(state)
            }
        }
          .resizableRow()
      }
        .resizableRow()
    }
      .apply {
        minimumSize = Dimension(450, 500)
      }
  }

  /** Register validator that enables OK action if validation map is empty */
  override fun init() {
    initialize { super.init() }
    panel.registerValidators(myDisposable) { map ->
      isOKActionEnabled = map.isEmpty()
    }
  }

  /**
   * Enables continuous validation of the dialog components
   */
  override fun continuousValidation(): Boolean {
    return true
  }

  override fun createCenterPanel(): JComponent {
    return panel
  }
}
