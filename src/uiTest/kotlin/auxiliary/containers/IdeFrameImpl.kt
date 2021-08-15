package auxiliary.containers

import auxiliary.ClosableCommonContainerFixture
import auxiliary.components.actionMenu
import auxiliary.components.actionMenuItem
import auxiliary.components.stripeButton
import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.Locator
import com.intellij.remoterobot.search.locators.byXpath
import java.time.Duration

fun RemoteRobot.ideFrameImpl(name: String,
                             stack: MutableList<Locator>,
                             function: IdeFrameImpl.() -> Unit) {
    find<IdeFrameImpl>(IdeFrameImpl.xPath(name), Duration.ofSeconds(60)).apply {
        stack.add(IdeFrameImpl.xPath(name))
        function()
        stack.removeLast()
    }
}

@FixtureName("IdeFrameImpl")
class IdeFrameImpl(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) : ClosableCommonContainerFixture(remoteRobot, remoteComponent) {
    fun forMainframe() {
        stripeButton(byXpath("For Mainframe", "//div[@accessiblename='For Mainframe' and @class='StripeButton' and @text='For Mainframe']"))
            .click()
    }
    companion object {
        @JvmStatic
        fun xPath(name: String) = byXpath("$name", "//div[@accessiblename='$name - IntelliJ IDEA' and @class='IdeFrameImpl']")
    }
    override fun close() {
        remoteRobot.actionMenu(remoteRobot, "File").click()
        remoteRobot.actionMenuItem(remoteRobot, "Close Project").click()
    }
}