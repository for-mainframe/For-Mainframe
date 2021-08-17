package auxiliary.closable

import auxiliary.ClosableFixtureItem
import auxiliary.containers.*
import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.search.locators.Locator
import java.time.Duration

class ClosableFixtureCollector() {
    var items = mutableListOf<ClosableFixtureItem>()
    fun add(xPath: Locator, stack: List<Locator>) {
        items.add(ClosableFixtureItem(xPath.byDescription, (stack + xPath).toMutableList()))
    }
    fun findClosable(remoteRobot: RemoteRobot, locator: Locator) = with(remoteRobot) {
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
        findClosable(remoteRobot, item.stack[i]).apply {
            if (i == item.stack.size-1) {
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
                closeItem(0, item, remoteRobot)
            }
        }
    }
    fun closeOnceIfExists(name: String) {
        for (item in items) {
            if (item.name == name) {
                items.remove(item)
                return
            }
        }
    }
}