package eu.ibagroup.formainframe.config

import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.TabbedConfigurable
import eu.ibagroup.formainframe.analytics.ui.AnalyticsConfigurable
import eu.ibagroup.formainframe.config.connect.ui.ConnectionConfigurable
import eu.ibagroup.formainframe.config.ws.ui.jobs.JobsWsConfigurable
import eu.ibagroup.formainframe.config.ws.ui.files.WSConfigurable

class MainframeConfigurable : TabbedConfigurable() {

  var preferredConfigurableClass: Class<*>? = null

  override fun getDisplayName(): String {
    return "For Mainframe"
  }

  private lateinit var connectionConfigurable: ConnectionConfigurable
  private lateinit var wsConfigurable: WSConfigurable
  private lateinit var analyticsConfigurable: AnalyticsConfigurable
  private lateinit var jobsWsConfigurable: JobsWsConfigurable

  override fun createConfigurables(): MutableList<Configurable> {
    return mutableListOf(
      WSConfigurable().also { wsConfigurable = it },
      ConnectionConfigurable().also { connectionConfigurable = it },
      JobsWsConfigurable().also { jobsWsConfigurable = it },
      AnalyticsConfigurable().also { analyticsConfigurable = it }
    )
  }

  override fun apply() {
    super.apply()
    ConfigSandbox.instance.updateState()
  }

  override fun reset() {
    ConfigSandbox.instance.fetch()
    super.reset()
  }

  override fun cancel() {
    configurables.forEach { it.cancel() }
  }

  override fun createConfigurableTabs() {
    super.createConfigurableTabs().also {
      myTabbedPane.selectedIndex = when (preferredConfigurableClass) {
        WSConfigurable::class.java -> 2
        ConnectionConfigurable::class.java -> 1
        else -> 0
      }
    }
  }

}
