package auxiliary.containers

import auxiliary.clickActionButton
import auxiliary.clickButton
import auxiliary.closable.ClosableFixtureCollector
import auxiliary.components.tabLabel
import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.Locator
import com.intellij.remoterobot.search.locators.byXpath
import java.time.Duration

fun RemoteRobot.component(function: Component.() -> Unit) {
    find<Component>(Component.xPath(), Duration.ofSeconds(60)).apply(function)
}

@FixtureName("Component")
class Component(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) : CommonContainerFixture(remoteRobot, remoteComponent) {
    fun gotIt() {
        clickButton(byXpath("//div[@accessiblename='Got It' and @class='JButton' and @text='Got It']"))
    }
    companion object {
        @JvmStatic
        fun xPath() = byXpath("//div[@class='MyComponent']")
    }
}