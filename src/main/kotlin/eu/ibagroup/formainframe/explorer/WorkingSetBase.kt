/*
 * This is a property of IBA Group
 */

package eu.ibagroup.formainframe.explorer

import com.intellij.openapi.Disposable
import eu.ibagroup.formainframe.config.configCrudable
import eu.ibagroup.formainframe.config.connect.ConnectionConfig
import eu.ibagroup.formainframe.config.ws.WorkingSetConfig
import eu.ibagroup.formainframe.utils.clone
import eu.ibagroup.formainframe.utils.runIfTrue
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

abstract class WorkingSetBase<MaskType, WS : WorkingSet<*>, WSConfig : WorkingSetConfig>(
  override val uuid: String,
  globalExplorer: AbstractExplorerBase<out WorkingSet<*>, WSConfig>,
  private val workingSetConfigProvider: (String) -> WSConfig?,
  parentDisposable: Disposable
) : WorkingSet<MaskType> {

  abstract val wsConfigClass: Class<out WSConfig>

  override val explorer = globalExplorer

  private val isDisposed = AtomicBoolean(false)

  protected val lock = ReentrantLock()

  protected val workingSetConfig: WSConfig?
    get() = lock.withLock {
      (isDisposed.compareAndSet(false, false)).runIfTrue { workingSetConfigProvider(uuid) }
    }

  abstract fun WSConfig.masks(): MutableCollection<MaskType>

  override val masks: Collection<MaskType>
    get() = lock.withLock { workingSetConfig?.masks() ?: listOf() }

  override val name
    get() = workingSetConfig?.name ?: ""

  override val connectionConfig: ConnectionConfig?
    get() = lock.withLock {
      workingSetConfig
        ?.let {
          return@withLock configCrudable.getByForeignKey(it, ConnectionConfig::class.java).get()
        }
    }

  override fun addMask(mask: MaskType) {
    val newWsConfig = workingSetConfig?.clone(wsConfigClass) ?: return
    if (newWsConfig.masks().add(mask)) {
      configCrudable.update(newWsConfig)
    }
  }

  override fun removeMask(mask: MaskType) {
    val newWsConfig = workingSetConfig?.clone(wsConfigClass) ?: return
    if (newWsConfig.masks().remove(mask)) {
      configCrudable.update(newWsConfig)
    }
  }

  override fun dispose() {
    isDisposed.set(true)
  }
}