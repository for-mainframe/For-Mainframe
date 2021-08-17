package auxiliary.containers

import auxiliary.ClosableCommonContainerFixture
import auxiliary.clickButton
import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.Locator
import com.intellij.remoterobot.search.locators.byXpath
import java.time.Duration

fun ContainerFixture.settingsDialog(
    stack: MutableList<Locator>,
    timeout: Duration = Duration.ofSeconds(60),
    function: SettingsDialog.() -> Unit = {}) {
    find<SettingsDialog>(SettingsDialog.xPath(), timeout).apply {
        stack.add(SettingsDialog.xPath())
        function()
        stack.removeLast()
    }
}

@FixtureName("Settings Dialog")
class SettingsDialog(
    remoteRobot: RemoteRobot,
    remoteComponent: RemoteComponent
) : ClosableCommonContainerFixture(remoteRobot, remoteComponent) {
    override fun close() {
        cancel()
    }
    fun cancel() {
        clickButton("Cancel")
    }
    companion object {
        const val name = "Settings Dialog"
        @JvmStatic
        fun xPath() = byXpath( name,"//div[@accessiblename='Settings' and @class='MyDialog']")
    }
}