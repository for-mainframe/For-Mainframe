package auxiliary

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.waitFor
import java.time.Duration

fun RemoteRobot.configurableEditor(function: ConfigurableEditor.() -> Unit) {
    find<ConfigurableEditor>(ConfigurableEditor.xPath(), Duration.ofSeconds(60)).apply(function)
}

@FixtureName("ConfigurableEditor")
class ConfigurableEditor(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) : CommonContainerFixture(remoteRobot, remoteComponent) {
    val conTab = remoteRobot.tabLabel(remoteRobot, "z/OSMF Connections")
    fun add() {
        clickActionButton(byXpath("//div[@accessiblename='Add' and @class='ActionButton' and @myaction='Add (Add)']"))
    }
    companion object {
        @JvmStatic
        fun xPath() = byXpath("//div[@class='ConfigurableEditor']")
    }
}

fun RemoteRobot.tabLabel(remoteRobot: RemoteRobot, name: String): TabLabel {
    val xpath = byXpath("$name", "//div[@accessiblename='$name' and @class='TabLabel']")
    waitFor {
        findAll<TabLabel>(xpath).isNotEmpty()
    }
    return findAll<TabLabel>(xpath).first()
}

@FixtureName("TabLabel")
class TabLabel(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) : ComponentFixture(remoteRobot, remoteComponent)