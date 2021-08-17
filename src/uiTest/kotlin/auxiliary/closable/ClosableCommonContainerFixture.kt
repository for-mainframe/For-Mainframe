package auxiliary

import auxiliary.containers.*
import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.CommonContainerFixture
import com.intellij.remoterobot.search.locators.Locator
import com.intellij.remoterobot.search.locators.byXpath
import java.time.Duration

abstract class ClosableCommonContainerFixture(
    remoteRobot: RemoteRobot,
    remoteComponent: RemoteComponent
): CommonContainerFixture(remoteRobot, remoteComponent) {
    abstract fun close()
}

data class ClosableFixtureItem(
    var name: String,
    var stack: MutableList<Locator>
)