package auxiliary

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.byXpath
import java.time.Duration

fun RemoteRobot.explorer(function: Explorer.() -> Unit) {
    find<Explorer>(Explorer.xPath(), Duration.ofSeconds(60)).apply(function)
}

@FixtureName("Explorer")
class Explorer(remoteRobot: RemoteRobot, remoteComponent: RemoteComponent) : CommonContainerFixture(remoteRobot, remoteComponent) {
    fun settings() {
        clickActionButton(byXpath("//div[@class='ActionButton' and @myaction=' ()']"))
    }
    companion object {
        @JvmStatic
        fun xPath() = byXpath( "//div[@accessiblename='File Explorer Tool Window' and @class='InternalDecoratorImpl']")
    }
}