package eu.ibagroup.formainframe.explorer.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.layout.PropertyBinding
import com.intellij.ui.layout.panel
import com.intellij.ui.layout.selectedValueMatches
import eu.ibagroup.formainframe.common.ui.StatefulDialog
import eu.ibagroup.formainframe.dataops.operations.DatasetAllocationParams
import eu.ibagroup.formainframe.utils.validateDatasetNameOnInput
import eu.ibagroup.formainframe.utils.validateForBlank
import eu.ibagroup.formainframe.utils.validateForPositiveInteger
import eu.ibagroup.formainframe.utils.validateVolser
import eu.ibagroup.r2z.AllocationUnit
import eu.ibagroup.r2z.DatasetOrganization
import eu.ibagroup.r2z.RecordFormat
import java.awt.Dimension
import javax.swing.JComboBox
import javax.swing.JComponent
import javax.swing.JTextField
import javax.swing.border.EmptyBorder

class AllocationDialog(project: Project?, override var state: DatasetAllocationParams) :
  StatefulDialog<DatasetAllocationParams>(project = project) {

  private lateinit var recordFormatBox: JComboBox<RecordFormat>
  private lateinit var spaceUnitBox: ComboBox<AllocationUnit?>
  private lateinit var datasetOrganizationBox: JComboBox<DatasetOrganization>
  private lateinit var primaryAllocationField: JTextField

  private val mainPanel by lazy {
    panel {
      row {
        label("Dataset name")
        textField(state::datasetName).withValidationOnInput {
          validateDatasetNameOnInput(it)
        }.withValidationOnApply {
          validateForBlank(it)
        }
      }
      row {
        label("Dataset organization")
        comboBox(
          model = CollectionComboBoxModel(
            listOf(
              DatasetOrganization.PS,
              DatasetOrganization.PO,
              DatasetOrganization.POE
            )
          ),
          prop = state.allocationParameters::datasetOrganization
        ).also {
          datasetOrganizationBox = it.component
        }

      }
      row {
        label("Allocation unit")
        comboBox(
          model = CollectionComboBoxModel(listOf(AllocationUnit.TRK, AllocationUnit.BLK, AllocationUnit.CYL)),
          modelBinding = PropertyBinding(
            get = { state.allocationParameters.allocationUnit },
            set = { state.allocationParameters.allocationUnit = it }
          )
        ).also { spaceUnitBox = it.component }
      }
      row {
        label("Primary allocation")
        intTextField(PropertyBinding(
          get = { state.allocationParameters.primaryAllocation },
          set = { state.allocationParameters.primaryAllocation = it }
        )).withValidationOnInput {
          validateForPositiveInteger(it)
        }.also { primaryAllocationField = it.component }
      }
      row {
        label("Secondary allocation")
        intTextField(state.allocationParameters::secondaryAllocation).withValidationOnInput {
          validateForPositiveInteger(it)
        }
      }
      row {
        label("Directory")
        intTextField(
          PropertyBinding(
            get = { state.allocationParameters.directoryBlocks ?: 0 },
            set = { state.allocationParameters.directoryBlocks = it }
          )
        ).withValidationOnInput {
          validateForPositiveInteger(it)
            ?: if (it.text.toIntOrNull() ?: 0 > primaryAllocationField.text.toIntOrNull() ?: 0) {
              ValidationInfo("Directory cannot exceed primary allocation", it)
            } else {
              null
            }
        }.withValidationOnApply {
          validateForBlank(it)
            ?: if (it.text == "0" && it.isEnabled) {
              ValidationInfo("Directory cannot be equal to zero", it)
            } else {
              null
            }
        }.enableIf(datasetOrganizationBox.selectedValueMatches { it == DatasetOrganization.PO })
      }
      row {
        label("Record format")
        comboBox(
          model = CollectionComboBoxModel(
            listOf(
              RecordFormat.F,
              RecordFormat.FB,
              RecordFormat.V,
              RecordFormat.VA,
              RecordFormat.VB,
              RecordFormat.U
            )
          ),
          prop = state.allocationParameters::recordFormat
        ).also {
          recordFormatBox = it.component
        }
      }
      row {
        label("Record Length")
        intTextField(
          PropertyBinding(
            get = { state.allocationParameters.recordLength ?: 0 },
            set = { state.allocationParameters.recordLength = it }
          )
        ).withValidationOnInput {
          validateForPositiveInteger(it)
        }
      }
      row {
        label("Block size")
        intTextField(
          PropertyBinding(
            get = { state.allocationParameters.blockSize ?: 0 },
            set = { state.allocationParameters.blockSize = it }
          )).withValidationOnInput {
          validateForPositiveInteger(it)
        }
      }
      row {
        label("Average Block Length")
        intTextField(
          PropertyBinding(
            get = { state.allocationParameters.averageBlockLength ?: 0 },
            set = { state.allocationParameters.averageBlockLength = it }
          )
        ).withValidationOnInput {
          validateForPositiveInteger(it)
        }.enableIf(spaceUnitBox.selectedValueMatches { it == AllocationUnit.BLK })

      }
      hideableRow("Advanced Parameters") {
        row {
          label("Volume")
          textField(PropertyBinding(
            get = { state.allocationParameters.volumeSerial ?: "" },
            set = { state.allocationParameters.volumeSerial = it }
          )).withValidationOnInput {
            validateVolser(it)
          }
        }
        row {
          label("Device Type")
          textField(PropertyBinding(
            get = { state.allocationParameters.deviceType ?: "" },
            set = { state.allocationParameters.deviceType = it }
          ))
        }
        row {
          label("Storage class")
          textField(PropertyBinding(
            get = { state.allocationParameters.storageClass ?: "" },
            set = { state.allocationParameters.storageClass = it }
          ))
        }
        row {
          label("Management class")
          textField(PropertyBinding(
            get = { state.allocationParameters.managementClass ?: "" },
            set = { state.allocationParameters.managementClass = it }
          ))
        }
        row {
          label("Data class")
          textField(PropertyBinding(
            get = { state.allocationParameters.dataClass ?: "" },
            set = { state.allocationParameters.dataClass = it }
          ))
        }
      }
    }.apply {
      minimumSize = Dimension(450, 300)
    }

  }

  override fun createCenterPanel(): JComponent {
    return mainPanel
  }


  init {
    title = "Allocate Dataset"
    init()
  }


}



