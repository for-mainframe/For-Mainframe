package settings.connection

import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.search.locators.byXpath
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import auxiliary.*

@ExtendWith(RemoteRobotExtension::class)
class ConnectionManager {

    @BeforeEach
    fun setUp(remoteRobot: RemoteRobot) = with(remoteRobot) {
//        welcomeFrame {
//            openProject.click()
//            dialog("Open File or Project") {
//                textField(byXpath("","//div[@class='BorderlessTextField']")).text =
//                    System.getProperty("user.dir") + "/src/uiTest/resources/untitled"
//                button("OK").click()
//            }
//        }
//        Thread.sleep(30000)
    }

    @AfterEach
    fun tearDown(remoteRobot: RemoteRobot) = with(remoteRobot) {
//        actionMenu(remoteRobot, "File").click()
//        actionMenuItem(remoteRobot, "Close Project").click()
    }

    @Test
    fun test(remoteRobot: RemoteRobot) = with(remoteRobot) {
        ideFrameImpl("untitled") {
            forMainframe.click()
            explorer {
                settings.click()
            }
            dialog("Settings") {
                configurableEditor {
                    conTab.click()
                    actionButton(byXpath("//div[@accessiblename='Add' and @class='ActionButton' and @myaction='Add (Add)']")).click()
                }
                addConnectionDialog("a","https://a.com","a","a",true) {
                    assertTrue(button("OK").isEnabled())
                    button("OK").click()
                }
                dialog("Error Creating Connection") {
                    button("Yes").click()
                }
                configurableEditor {
                    addButton.click()
                }
                addConnectionDialog("a","https://b.com","b","b",true) {
                    assertFalse(button("OK").isEnabled())
                    button("Cancel").click()
                }
                button("Cancel").click()
            }
        }
    }
}













