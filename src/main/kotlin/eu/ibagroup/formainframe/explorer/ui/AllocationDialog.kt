/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package eu.ibagroup.formainframe.explorer.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.layout.selectedValueMatches
import eu.ibagroup.formainframe.common.ui.StatefulDialog
import eu.ibagroup.formainframe.dataops.operations.DatasetAllocationParams
import eu.ibagroup.formainframe.explorer.config.*
import eu.ibagroup.formainframe.utils.validateDataset
import eu.ibagroup.r2z.AllocationUnit
import eu.ibagroup.r2z.DatasetOrganization
import eu.ibagroup.r2z.RecordFormat
import java.awt.Dimension
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JTextField

class AllocationDialog(project: Project?, override var state: DatasetAllocationParams) :
  StatefulDialog<DatasetAllocationParams>(project = project) {

  private lateinit var recordFormatBox: JComboBox<RecordFormat>
  private lateinit var spaceUnitBox: ComboBox<AllocationUnit>
  private lateinit var datasetOrganizationBox: ComboBox<DatasetOrganization>
  private lateinit var datasetNameField: JTextField
  private lateinit var memberNameField: JTextField
  private lateinit var primaryAllocationField: JTextField
  private lateinit var secondaryAllocationField: JTextField
  private lateinit var directoryBlocksField: JTextField
  private lateinit var recordLengthField: JTextField
  private lateinit var blockSizeField: JTextField
  private lateinit var averageBlockLengthField: JTextField
  private lateinit var advancedParametersField: JTextField
  private lateinit var presetsBox: JComboBox<Presets>

  @Suppress("UnstableApiUsage")
  private val mainPanel by lazy {
    val sameWidthLabelsGroup = "ALLOCATION_DIALOG_LABELS_WIDTH_GROUP"
    val sameWidthComboBoxGroup = "ALLOCATION_DIALOG_COMBO_BOX_WIDTH_GROUP"

    panel {
      row {
        label("Choose preset")
          .widthGroup(sameWidthLabelsGroup)
        comboBox(listOf(Presets.CUSTOM_DATASET, Presets.SEQUENTIAL_DATASET, Presets.PDS_DATASET, Presets.PDS_WITH_EMPTY_MEMBER, Presets.PDS_WITH_SAMPLE_JCL_MEMBER))
          .bindItem(state::presets.toNullableProperty())
          .also { presetsBox = it.component }
          .widthGroup(sameWidthComboBoxGroup)
          .whenItemSelectedFromUi {
            doPresetAssignment(it)
          }
      }
      row {
        label("Dataset name: ")
          .widthGroup(sameWidthLabelsGroup)
        textField()
          .bindText(state::datasetName)
          .apply { focused() }
          .also { datasetNameField = it.component }
          .onApply { state.datasetName = state.datasetName.uppercase() }
          .horizontalAlign(HorizontalAlign.FILL)
      }
      row {
        label("Member name: ")
          .widthGroup(sameWidthLabelsGroup)
        textField()
          .bindText(state::memberName)
          .also { memberNameField = it.component }
          .onApply { state.memberName = state.memberName.uppercase() }
          .horizontalAlign(HorizontalAlign.FILL)
      }
        .visibleIf(presetsBox.selectedValueMatches { it == Presets.PDS_WITH_EMPTY_MEMBER || it == Presets.PDS_WITH_SAMPLE_JCL_MEMBER })
      row {
        label("Dataset organization: ")
          .widthGroup(sameWidthLabelsGroup)
        comboBox(
          listOf(
            DatasetOrganization.PS,
            DatasetOrganization.PO,
            DatasetOrganization.POE
          )
        )
          .bindItem(state.allocationParameters::datasetOrganization.toNullableProperty())
          .also { datasetOrganizationBox = it.component }
          .widthGroup(sameWidthComboBoxGroup)
      }
      row {
        label("Allocation unit: ")
          .widthGroup(sameWidthLabelsGroup)
        comboBox(listOf(AllocationUnit.TRK, AllocationUnit.CYL))
          .bindItem(state.allocationParameters::allocationUnit.toNullableProperty())
          .also { spaceUnitBox = it.component }
          .widthGroup(sameWidthComboBoxGroup)
      }
      row {
        label("Primary allocation: ")
          .widthGroup(sameWidthLabelsGroup)
        textField()
          .bindText(
            { state.allocationParameters.primaryAllocation.toString() },
            { state.allocationParameters.primaryAllocation = it.toIntOrNull() ?: 0 }
          )
          .also { primaryAllocationField = it.component }
          .horizontalAlign(HorizontalAlign.FILL)
      }
      row {
        label("Secondary allocation: ")
          .widthGroup(sameWidthLabelsGroup)
        textField()
          .bindText(
            { state.allocationParameters.secondaryAllocation.toString() },
            { state.allocationParameters.secondaryAllocation = it.toIntOrNull() ?: 0 }
          )
          .also { secondaryAllocationField = it.component }
          .horizontalAlign(HorizontalAlign.FILL)
      }
      row {
        label("Directory: ")
          .widthGroup(sameWidthLabelsGroup)
        textField()
          .bindText(
            {
              if (state.allocationParameters.directoryBlocks != null) {
                state.allocationParameters.directoryBlocks.toString()
              } else {
                "0"
              }
            },
            { state.allocationParameters.directoryBlocks = it.toIntOrNull() ?: 0 }
          )
          .also { directoryBlocksField = it.component }
          .horizontalAlign(HorizontalAlign.FILL)
      }
        .visibleIf(datasetOrganizationBox.selectedValueMatches { it != DatasetOrganization.PS })
      row {
        label("Record format: ")
          .widthGroup(sameWidthLabelsGroup)
        comboBox(
          listOf(
            RecordFormat.F,
            RecordFormat.FB,
            RecordFormat.V,
            RecordFormat.VA,
            RecordFormat.VB,
          )
        )
          .bindItem(state.allocationParameters::recordFormat.toNullableProperty())
          .also { recordFormatBox = it.component }
          .widthGroup(sameWidthComboBoxGroup)
      }
      row {
        label("Record Length: ")
          .widthGroup(sameWidthLabelsGroup)
        textField()
          .bindText(
            { state.allocationParameters.recordLength?.toString() ?: "0" },
            { state.allocationParameters.recordLength = it.toIntOrNull() }
          )
          .also { recordLengthField = it.component }
          .horizontalAlign(HorizontalAlign.FILL)
      }
      row {
        label("Block size: ")
          .widthGroup(sameWidthLabelsGroup)
        textField()
          .bindText(
            { state.allocationParameters.blockSize?.toString() ?: "0" },
            { state.allocationParameters.blockSize = it.toIntOrNull() }
          )
          .also { blockSizeField = it.component }
          .horizontalAlign(HorizontalAlign.FILL)
      }
      row {
        label("Average Block Length: ")
          .widthGroup(sameWidthLabelsGroup)
        textField()
          .bindText(
            { state.allocationParameters.averageBlockLength?.toString() ?: "0" },
            { state.allocationParameters.averageBlockLength = it.toIntOrNull() }
          )
          .also { averageBlockLengthField = it.component }
          .horizontalAlign(HorizontalAlign.FILL)
      }
      collapsibleGroup("Advanced Parameters", false) {
        row {
          label("Volume: ")
            .widthGroup(sameWidthLabelsGroup)
          textField()
            .bindText(
              { state.allocationParameters.volumeSerial ?: "" },
              { state.allocationParameters.volumeSerial = it }
            )
            .also { advancedParametersField = it.component }
            .horizontalAlign(HorizontalAlign.FILL)
        }
        row {
          label("Device Type: ")
            .widthGroup(sameWidthLabelsGroup)
          textField()
            .bindText(
              { state.allocationParameters.deviceType ?: "" },
              { state.allocationParameters.deviceType = it }
            )
            .horizontalAlign(HorizontalAlign.FILL)
        }
        row {
          label("Storage class: ")
            .widthGroup(sameWidthLabelsGroup)
          textField()
            .bindText(
              { state.allocationParameters.storageClass ?: "" },
              { state.allocationParameters.storageClass = it }
            )
            .horizontalAlign(HorizontalAlign.FILL)
        }
        row {
          label("Management class: ")
            .widthGroup(sameWidthLabelsGroup)
          textField()
            .bindText(
              { state.allocationParameters.managementClass ?: "" },
              { state.allocationParameters.managementClass = it }
            )
            .horizontalAlign(HorizontalAlign.FILL)
        }
        row {
          label("Data class: ")
            .widthGroup(sameWidthLabelsGroup)
          textField()
            .bindText(
              { state.allocationParameters.dataClass ?: "" },
              { state.allocationParameters.dataClass = it }
            )
            .horizontalAlign(HorizontalAlign.FILL)
        }
      }
    }
  }

  override fun createCenterPanel(): JComponent {
    return JBScrollPane(
      mainPanel,
      JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
      JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
    ).apply {
      minimumSize = Dimension(450, 300)
      if (state.errorMessage != "") {
        setErrorText(state.errorMessage)
      }
      border = null
    }
  }

  /**
   * Function which sets the proper values for a chosen preset from UI
   * @param preset - a chosen preset from UI
   * @return Void
   */
  private fun doPresetAssignment(preset : Presets) {
    val dataContainer = preset.initDataClass()
    datasetOrganizationBox.selectedItem = dataContainer.presetCustom?.datasetOrganization ?: dataContainer.presetSeq?.datasetOrganization ?: dataContainer.presetPds?.datasetOrganization
    spaceUnitBox.selectedItem = dataContainer.presetCustom?.spaceUnit ?: dataContainer.presetSeq?.spaceUnit ?: dataContainer.presetPds?.spaceUnit
    val primaryAlloc = dataContainer.presetCustom?.primaryAllocation ?: dataContainer.presetSeq?.primaryAllocation ?: dataContainer.presetPds?.primaryAllocation
    primaryAllocationField.text = primaryAlloc.toString()
    val secondaryAlloc = dataContainer.presetCustom?.secondaryAllocation ?: dataContainer.presetSeq?.secondaryAllocation ?: dataContainer.presetPds?.secondaryAllocation
    secondaryAllocationField.text = secondaryAlloc.toString()
    val dirBlocks = dataContainer.presetCustom?.directoryBlocks ?: dataContainer.presetSeq?.directoryBlocks ?: dataContainer.presetPds?.directoryBlocks
    directoryBlocksField.text = dirBlocks.toString()
    recordFormatBox.selectedItem = dataContainer.presetCustom?.recordFormat ?: dataContainer.presetSeq?.recordFormat ?: dataContainer.presetPds?.recordFormat
    val recordLen = dataContainer.presetCustom?.recordLength ?: dataContainer.presetSeq?.recordLength ?: dataContainer.presetPds?.recordLength
    recordLengthField.text = recordLen.toString()
    val blockSize = dataContainer.presetCustom?.blockSize ?: dataContainer.presetSeq?.blockSize ?: dataContainer.presetPds?.blockSize
    blockSizeField.text = blockSize.toString()
    val avgBlockLen = dataContainer.presetCustom?.averageBlockLength ?: dataContainer.presetSeq?.averageBlockLength ?: dataContainer.presetPds?.averageBlockLength
    averageBlockLengthField.text = avgBlockLen.toString()
  }

  override fun doOKAction() {
    super.doOKAction()
    mainPanel.apply()
  }

  override fun doValidate(): ValidationInfo? {
    super.doValidate()
    return validateDataset(
      datasetNameField,
      datasetOrganizationBox.selectedItem as DatasetOrganization,
      primaryAllocationField,
      secondaryAllocationField,
      directoryBlocksField,
      recordLengthField,
      blockSizeField,
      averageBlockLengthField,
      advancedParametersField
    )
  }

  init {
    title = "Allocate Dataset"
    init()
  }
}
