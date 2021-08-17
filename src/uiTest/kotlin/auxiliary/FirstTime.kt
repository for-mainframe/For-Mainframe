package auxiliary

import com.intellij.remoterobot.RemoteRobot
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import auxiliary.*
import auxiliary.closable.ClosableFixtureCollector
import auxiliary.containers.*
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.search.locators.Locator
import com.intellij.remoterobot.search.locators.byXpath
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Tag

@Tag("FirstTime")
@ExtendWith(RemoteRobotExtension::class)
class ConnectionManager {
    private var stack = mutableListOf<Locator>()
    private val projectName = "untitled"

    @Test
    fun firstTime(remoteRobot: RemoteRobot) = with(remoteRobot) {
        welcomeFrame {
            open(projectName)
        }
        Thread.sleep(180000)
        ideFrameImpl(projectName, stack) {
            dialog("For Mainframe Plugin Privacy Policy and Terms and Conditions") {
                clickButton("I Agree")
            }
            dialog("Tip of the Day") {
                checkBox("Don't show tips").select()
                clickButton("Close")
            }
            component {
                gotIt()
            }
            close()
        }
    }
}

