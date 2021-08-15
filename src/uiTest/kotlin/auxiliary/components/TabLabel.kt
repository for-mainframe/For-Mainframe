package auxiliary.components

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.waitFor

fun RemoteRobot.tabLabel(remoteRobot: RemoteRobot, name: String): TabLabel {
    val xpath = byXpath("$name", "//div[@accessiblename='$name' and @class='TabLabel']")
    waitFor {
        findAll<TabLabel>(xpath).isNotEmpty()
    }
    return findAll<TabLabel>(xpath).first()
}

@FixtureName("TabLabel")
class TabLabel(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) : ComponentFixture(remoteRobot, remoteComponent)