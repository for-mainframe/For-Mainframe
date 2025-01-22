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

package eu.ibagroup.formainframe.explorer.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.text
import eu.ibagroup.formainframe.common.ui.StatefulComponent
import eu.ibagroup.formainframe.dataops.attributes.RemoteUssAttributes
import javax.swing.JComponent
import com.intellij.ui.dsl.builder.*
import eu.ibagroup.formainframe.dataops.content.synchronizer.DEFAULT_BINARY_CHARSET
import eu.ibagroup.formainframe.utils.*
import org.zowe.kotlinsdk.*
import java.nio.charset.Charset

/** Class for USS file properties dialog */
class UssFilePropertiesDialog(project: Project?, override var state: UssFileState) : DialogWrapper(project),
  StatefulComponent<UssFileState> {

  private val sameWidthGroup = "USS_FILE_PROPERTIES_DIALOG_LABELS_WIDTH_GROUP"

  private val generalTab by lazy{
  panel {
      row {
        label("$fileTypeName name: ")
          .widthGroup(sameWidthGroup)
        textField()
          .text(state.ussAttributes.name)
          .applyToComponent { isEditable = false }
          .align(AlignX.FILL)
      }
      row {
        label("Location: ")
          .widthGroup(sameWidthGroup)
        textField()
          .text(state.ussAttributes.parentDirPath)
          .applyToComponent { isEditable = false }
          .align(AlignX.FILL)
      }
      row {
        label("Path: ")
          .widthGroup(sameWidthGroup)
        textField()
          .text(state.ussAttributes.path)
          .applyToComponent { isEditable = false }
          .align(AlignX.FILL)
      }
      row {
        label("$fileTypeName size: ")
          .widthGroup(sameWidthGroup)
        textField()
          .text("${state.ussAttributes.length} bytes")
          .applyToComponent { isEditable = false }
          .align(AlignX.FILL)
      }
      row {
        label("Last modified: ")
          .widthGroup(sameWidthGroup)
        textField()
          .text(getParamTextValueOrUnknown(state.ussAttributes.modificationTime))
          .applyToComponent { isEditable = false }
          .align(AlignX.FILL)
      }
      if (!state.ussAttributes.isDirectory && state.fileIsBeingEditingNow) {
        row {
          label("File encoding: ").widthGroup(sameWidthGroup)
          comboBox = comboBox(getSupportedEncodings())
            .bindItem(state.ussAttributes::charset.toNullableProperty())
            .align(AlignX.FILL)
        }
        row {
          button("Reset Default Encoding") {
            state.ussAttributes.charset = DEFAULT_BINARY_CHARSET
            comboBox.component.item = DEFAULT_BINARY_CHARSET
          }.widthGroup(sameWidthGroup)
        }
      }
      if (state.ussAttributes.isSymlink) {
        row {
          label("Symlink to: ")
            .widthGroup(sameWidthGroup)
          textField()
            .text(getParamTextValueOrUnknown(state.ussAttributes.symlinkTarget))
            .applyToComponent { isEditable = false }
            .align(AlignX.FILL)
        }
      }
    }
  }

  private val permissionTab by lazy{
    panel {
      row {
        label("Owner: ")
          .widthGroup(sameWidthGroup)
        textField()
          .bindText(
            { state.ussAttributes.owner ?: UNKNOWN_PARAM_VALUE },
            { state.ussAttributes.owner = it }
          )
          .validationOnApply { validateForBlank(it) ?: validateFieldWithLengthRestriction(it, 8, "Owner") }
          .align(AlignX.FILL)
      }
      row {
        label("Group: ")
          .widthGroup(sameWidthGroup)
        textField()
          .bindText(
            { state.ussAttributes.groupId ?: UNKNOWN_PARAM_VALUE },
            { state.ussAttributes.groupId = it }
          )
          .validationOnApply { validateForBlank(it) ?: validateFieldWithLengthRestriction(it, 8, "Group") }
          .align(AlignX.FILL)
      }
      row {
        label("The numeric group ID (GID): ")
          .widthGroup(sameWidthGroup)
        textField()
          .text(getParamTextValueOrUnknown(state.ussAttributes.gid))
          .applyToComponent { isEditable = false }
          .align(AlignX.FILL)
      }
      row {
        label("Owner permissions: ")
          .widthGroup(sameWidthGroup)
        comboBox(fileModeValues)
          .bindItem(
            { state.ussAttributes.fileMode?.owner?.toFileModeValue() },
            { state.ussAttributes.fileMode?.owner = it?.mode ?: 0 }
          )
          .align(AlignX.FILL)
      }
      row {
        label("Group permissions: ")
          .widthGroup(sameWidthGroup)
        comboBox(fileModeValues)
          .bindItem(
            { state.ussAttributes.fileMode?.group?.toFileModeValue() },
            { state.ussAttributes.fileMode?.group = it?.mode ?: 0 }
          )
          .align(AlignX.FILL)
      }
      row {
        label("Permissions for all users: ")
          .widthGroup(sameWidthGroup)
        comboBox(fileModeValues)
          .bindItem(
            { state.ussAttributes.fileMode?.all?.toFileModeValue() },
            { state.ussAttributes.fileMode?.all = it?.mode ?: 0 }
          )
          .align(AlignX.FILL)
      }
    }
  }

  private lateinit var comboBox: Cell<ComboBox<Charset>>

  var fileTypeName: String = "File"

  private val fileModeValues = listOf(
    FileModeValue.NONE,
    FileModeValue.EXECUTE,
    FileModeValue.WRITE,
    FileModeValue.WRITE_EXECUTE,
    FileModeValue.READ,
    FileModeValue.READ_EXECUTE,
    FileModeValue.READ_WRITE,
    FileModeValue.READ_WRITE_EXECUTE
  )

  init {
    if (state.ussAttributes.isDirectory)
      fileTypeName = "Directory"
    title = "$fileTypeName Properties"
    permissionTab.registerValidators(myDisposable) { map ->
      isOKActionEnabled = map.isEmpty()
    }
    init()
  }

  override fun createCenterPanel(): JComponent {
    val tabbedPanel = JBTabbedPane()
    tabbedPanel.add("General", generalTab)
    tabbedPanel.add("Permissions", permissionTab)

    return tabbedPanel
  }

  override fun doOKAction() {
    generalTab.apply()
    permissionTab.apply()
    super.doOKAction()
  }

  /**
   * Overloaded method to validate components in the permissionTab panel
   */
  override fun doValidate(): ValidationInfo? {
    return permissionTab.validateAll().firstOrNull() ?: super.doValidate()
  }

}

class UssFileState(var ussAttributes: RemoteUssAttributes, val fileIsBeingEditingNow: Boolean)

//class MemberState(var member: Member, override var mode: DialogMode = DialogMode.CREATE) : PropertiesState(mode)

fun Int.toFileModeValue(): FileModeValue {
  return when (this) {
    0 -> FileModeValue.NONE
    1 -> FileModeValue.EXECUTE
    2 -> FileModeValue.WRITE
    3 -> FileModeValue.WRITE_EXECUTE
    4 -> FileModeValue.READ
    5 -> FileModeValue.READ_EXECUTE
    6 -> FileModeValue.READ_WRITE
    7 -> FileModeValue.READ_WRITE_EXECUTE
    else -> FileModeValue.NONE
  }
}
