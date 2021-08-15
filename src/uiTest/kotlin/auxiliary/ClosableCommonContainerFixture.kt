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

class ClosableFixtureCollector() {
    var items = mutableListOf<ClosableFixtureItem>()
    fun add(xPath: Locator, stack: List<Locator>) {
        items.add(ClosableFixtureItem(xPath.byDescription, (stack + xPath).toMutableList()))
    }
    fun findClosable(remoteRobot: RemoteRobot, locator: Locator) = with(remoteRobot) {
        Thread.sleep(10)
        when(locator.byDescription) {
            SettingsDialog.name -> find<SettingsDialog>(locator, Duration.ofSeconds(60))
            AddConnectionDialog.name -> find<AddConnectionDialog>(locator, Duration.ofSeconds(60))
            EditConnectionDialog.name -> find<EditConnectionDialog>(locator, Duration.ofSeconds(60))
            ErrorCreatingConnectionDialog.name -> find<ErrorCreatingConnectionDialog>(locator, Duration.ofSeconds(60))
            IdeFrameImpl.xPath("untitled").byDescription -> find<IdeFrameImpl>(locator, Duration.ofSeconds(60))
            else -> throw IllegalAccessException("There is no corresponding class to ${locator.byDescription}")
        }
    }
    fun closeItem(i: Int, item: ClosableFixtureItem, remoteRobot: RemoteRobot) {
        println(item.stack[i].byDescription)
        findClosable(remoteRobot, item.stack[i]).apply {
            println("found")
            if (i == item.stack.size-1) {
                println("closed")
                close()
                items.remove(item)
            } else {
                closeItem(i+1, item, remoteRobot)
            }
        }
    }
    fun closeWantedClosables(wantToClose: List<String>, remoteRobot: RemoteRobot) {
        for (item in items.reversed()) {
            if (item.name in wantToClose) {
                println(item.name)
                closeItem(0, item, remoteRobot)
                println(items.size)
            }
        }
    }
}