package auxiliary

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.byXpath
import java.time.Duration

fun RemoteRobot.ideFrameImpl(name: String, function: IdeFrameImpl.() -> Unit) {
    find<IdeFrameImpl>(IdeFrameImpl.xPath(name), Duration.ofSeconds(60)).apply(function)
}

@FixtureName("IdeFrameImpl")
class IdeFrameImpl(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) : CommonContainerFixture(remoteRobot, remoteComponent) {
    val forMainframe = button(byXpath("For Mainframe", "//div[@accessiblename='For Mainframe' and @class='StripeButton' and @text='For Mainframe']"))
    companion object {
        @JvmStatic
        fun xPath(name: String) = byXpath("$name", "//div[@accessiblename='$name - IntelliJ IDEA' and @class='IdeFrameImpl']")
    }
}