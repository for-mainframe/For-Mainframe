package eu.ibagroup.formainframe.explorer.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.layout.PropertyBinding
import com.intellij.ui.layout.panel
import com.intellij.ui.layout.selectedValueMatches
import eu.ibagroup.formainframe.common.ui.DialogMode
import eu.ibagroup.formainframe.common.ui.DialogState
import eu.ibagroup.formainframe.common.ui.StatefulComponent
import eu.ibagroup.formainframe.utils.runIfTrue
import eu.ibagroup.r2z.AllocationUnit
import eu.ibagroup.r2z.CreateDataset
import eu.ibagroup.r2z.DatasetOrganization
import eu.ibagroup.r2z.RecordFormat
import java.awt.Dimension
import javax.swing.JComboBox
import javax.swing.JComponent

class AllocationDialog(project: Project?, override var state: AllocationDialogState) :
  DialogWrapper(project), StatefulComponent<AllocationDialogState> {

  private lateinit var recordFormatBox: JComboBox<RecordFormat>
  private lateinit var datasetOrganizationBox: JComboBox<DatasetOrganization>

  private val firstSymbol = "A-Za-z\$@#"
  private val remainingSymbol = firstSymbol + "0-9\\-"
  private val firstGroup = "([${firstSymbol}][${remainingSymbol}]{0,7})"
  private val remainingGroup = "[${remainingSymbol}]{1,8}"
  private val smallErrorMessage = "First segment must be alphabetic (A to Z) or national (# @ \$)"
  private val errorMessageForFullText =
    "Each name segment (qualifier) is 1 to 8 characters,\nthe first of which must be alphabetic (A to Z) or national (# @ \$).\nThe remaining seven characters are either alphabetic,\nnumeric (0 - 9), national, a hyphen (-).\nName segments are separated by a period (.)";


  private val datasetNameRegex = Regex("${firstGroup}(\\.${remainingGroup})*")

  private val mainPanel by lazy {
    panel {
      row {
        label("Dataset name")
        textField(state::datasetName).withValidationOnInput {
          val text = it.text.trim()
          val length = text.length
          val firstPart = text.substringBefore('.')
          return@withValidationOnInput if (text.isBlank()) {
            null
          } else if (length > 44) {
            ValidationInfo("Dataset name cannot exceed 44 characters", it)
          } else if (!firstPart.matches(Regex(firstGroup))) {
            ValidationInfo(smallErrorMessage, it)
          } else if (!text.endsWith('.') && !text.matches(datasetNameRegex)) {
            ValidationInfo(
              errorMessageForFullText, it
            )
          } else {
            null
          }
        }.withValidationOnApply {
          if (it.text.isBlank()) {
            ValidationInfo("Cannot be blank", it)
          } else if (!it.text.trim().matches(datasetNameRegex)) {
            ValidationInfo(errorMessageForFullText, it)
          } else {
            null
          }
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
          prop = state.allocationParameters::allocationUnit
        )
      }
      row {
        label("Primary allocation")
        intTextField(PropertyBinding(
          get = { state.allocationParameters.primaryAllocation },
          set = { state.allocationParameters.primaryAllocation = it }
        ))
      }
      row {
        label("Secondary allocation")
        intTextField(state.allocationParameters::secondaryAllocation)
      }
      row {
        label("Directory")
        intTextField(
          PropertyBinding(
            get = { state.allocationParameters.directoryBlocks ?: 0 },
            set = { state.allocationParameters.directoryBlocks = it }
          )
        ).withValidationOnInput {
          if (it.text.isNotBlank() && it.text.toIntOrNull() ?: Int.MAX_VALUE > state.allocationParameters.primaryAllocation) {
            ValidationInfo("Directory cannot exceed primary allocation", it)
          } else {
            null
          }
        }.withValidationOnApply {
          if (it.text.isBlank()) {
            ValidationInfo("Cannot be blank", it)
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
        label("Block size")
        intTextField(
          PropertyBinding(
            get = { state.allocationParameters.blockSize ?: 0 },
            set = { state.allocationParameters.blockSize = it }
          )).enableIf(recordFormatBox.selectedValueMatches { it == RecordFormat.FB || it == RecordFormat.VB })
      }.apply {

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


class AllocationDialogState(
  var datasetName: String = "",
  val allocationParameters: CreateDataset = CreateDataset(
    allocationUnit = AllocationUnit.TRK,
    primaryAllocation = 0,
    secondaryAllocation = 0,
    recordFormat = RecordFormat.FB,
    datasetOrganization = DatasetOrganization.PS
  )
) : DialogState {

  override var mode: DialogMode = DialogMode.CREATE

}



