package auxiliary.containers

import auxiliary.ClosableCommonContainerFixture
import auxiliary.clickButton
import auxiliary.closable.ClosableFixtureCollector
import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.data.RemoteComponent
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.fixtures.FixtureName
import com.intellij.remoterobot.search.locators.Locator
import com.intellij.remoterobot.search.locators.byXpath
import java.time.Duration

fun ContainerFixture.errorCreatingConnectionDialog(
    closableFixtureCollector: ClosableFixtureCollector,
    stack: List<Locator>,
    timeout: Duration = Duration.ofSeconds(60),
    function: ErrorCreatingConnectionDialog.() -> Unit = {}) {
    find<ErrorCreatingConnectionDialog>(ErrorCreatingConnectionDialog.xPath(), timeout).apply {
        for (item in closableFixtureCollector.items) {
            if (item.name == AddConnectionDialog.name) {
                item.name = EditConnectionDialog.name
                item.stack[item.stack.size-1] = EditConnectionDialog.xPath()
            }
        }
        closableFixtureCollector.add(ErrorCreatingConnectionDialog.xPath(), stack)
        function()
    }
}

@FixtureName("Error Creating Connection Dialog")
class ErrorCreatingConnectionDialog(
    remoteRobot: RemoteRobot,
    remoteComponent: RemoteComponent
) : ClosableCommonContainerFixture(remoteRobot, remoteComponent) {
    override fun close() {
        no()
    }
    fun no() {
        clickButton("No")
    }
    fun yes() {
        clickButton("Yes")
    }
    companion object {
        const val name = "Error Creating Connection Dialog"
        @JvmStatic
        fun xPath() = byXpath( name,"//div[@accessiblename='Error Creating Connection' and @class='MyDialog']")
    }
}