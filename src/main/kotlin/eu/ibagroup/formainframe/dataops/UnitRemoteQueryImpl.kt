package eu.ibagroup.formainframe.dataops

import eu.ibagroup.formainframe.config.connect.ConnectionConfig
import eu.ibagroup.formainframe.utils.UNIT_CLASS

data class UnitRemoteQueryImpl<R>(
  override val request: R,
  override val connectionConfig: ConnectionConfig
) : RemoteQuery<R, Unit> {
  override val resultClass: Class<out Unit>
    get() = UNIT_CLASS
}
