/*
 * This is property of IBA Group
 */

package eu.ibagroup.formainframe.config.jobs

import com.intellij.util.ui.ColumnInfo
import eu.ibagroup.formainframe.common.ui.CrudableTableModel
import eu.ibagroup.formainframe.utils.crudable.Crudable
import eu.ibagroup.formainframe.utils.crudable.MergedCollections
import eu.ibagroup.formainframe.utils.crudable.applyMergedCollections
import eu.ibagroup.formainframe.utils.crudable.getAll
import eu.ibagroup.formainframe.utils.toMutableList

class JobsFilterTableModel(crudable: Crudable) : CrudableTableModel<JobsFilter>(crudable) {

  init {
    columnInfos = arrayOf(
      BasicColumn
    )
  }

  override fun fetch(crudable: Crudable): MutableList<JobsFilter> = crudable.getAll<JobsFilter>().toMutableList()


  override fun onAdd(crudable: Crudable, value: JobsFilter): Boolean = crudable.add(value).isPresent

  override fun onUpdate(crudable: Crudable, value: JobsFilter): Boolean = crudable.update(value).isPresent

  override fun onDelete(crudable: Crudable, value: JobsFilter) {
    crudable.delete(value)
  }

  override fun onApplyingMergedCollection(crudable: Crudable, merged: MergedCollections<JobsFilter>) = crudable.applyMergedCollections(merged)

  override val clazz = JobsFilter::class.java

  object BasicColumn: ColumnInfo<JobsFilter, String>("Filter") {

    override fun valueOf(item: JobsFilter): String = item.toString()

  }
}