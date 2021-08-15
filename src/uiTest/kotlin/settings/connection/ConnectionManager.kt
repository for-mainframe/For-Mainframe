package settings.connection

import com.intellij.remoterobot.RemoteRobot
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import auxiliary.*
import auxiliary.containers.*
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.search.locators.Locator
import com.intellij.remoterobot.search.locators.byXpath
import org.junit.jupiter.api.Assertions.assertFalse

@ExtendWith(RemoteRobotExtension::class)
class ConnectionManager {
    private var closableFixtureCollector = ClosableFixtureCollector()
    private var stack = mutableListOf<Locator>()
    private val wantToClose = listOf("Settings Dialog", "Add Connection Dialog", "Error Creating Connection Dialog", "Edit Connection Dialog")
    private val projectName = "untitled"

    @BeforeEach
    fun setUp(remoteRobot: RemoteRobot) = with(remoteRobot) {
//        welcomeFrame {
//            open(projectName)
//        }
//        Thread.sleep(30000)
//        ideFrameImpl(projectName) {
//            forMainframe()
//        }
    }

    @AfterEach
    fun tearDown(remoteRobot: RemoteRobot) {
        closableFixtureCollector.closeWantedClosables(wantToClose, remoteRobot)
    }

    @Test
    fun testA(remoteRobot: RemoteRobot) = with(remoteRobot) {
        ideFrameImpl("untitled", stack) {
            explorer {
                settings(closableFixtureCollector, stack)
            }
            settingsDialog(stack) {
                configurableEditor {
                    conTab.click()
                    add(closableFixtureCollector, stack)
                }
                addConnectionDialog(stack) {
                    addConnection("a","https://a.com","a","a",true)
                    ok()
                }
                errorCreatingConnectionDialog(closableFixtureCollector, stack)
            }
        }
    }

//    //@Test
//    fun testB(remoteRobot: RemoteRobot) = with(remoteRobot) {
//        ideFrameImpl("untitled") {
//            explorer {
//                settings(closableFixtureCollector)
//            }
//            settingsDialog {
//                configurableEditor {
//                    conTab.click()
//                    add(closableFixtureCollector)
//                }
//                addConnectionDialog {
//                    addConnection("a","https://a.com","a","a",true)
//                    ok(closableFixtureCollector)
//                }
//            }
//        }
//    }
//
//    //@Test
//    fun testC(remoteRobot: RemoteRobot) = with(remoteRobot) {
//        ideFrameImpl("untitled") {
//            explorer {
//                settings(closableFixtureCollector)
//            }
//            settingsDialog {
//                configurableEditor {
//                    conTab.click()
//                    add(closableFixtureCollector)
//                }
//                addConnectionDialog {
//                    addConnection("a","https://a.com","a","a",true)
//                    ok(closableFixtureCollector)
//                }
//                errorCreatingConnectionDialog(closableFixtureCollector) {}
//            }
//        }
//    }
//
//    //@Test
//    fun testD(remoteRobot: RemoteRobot) = with(remoteRobot) {
//        ideFrameImpl("untitled") {
//            explorer {
//                settings(closableFixtureCollector)
//            }
//            settingsDialog {
//                configurableEditor {
//                    conTab.click()
//                    add(closableFixtureCollector)
//                }
//                addConnectionDialog {
//                    addConnection("a","https://a.com","a","a",true)
//                    ok(closableFixtureCollector)
//                }
//                errorCreatingConnectionDialog(closableFixtureCollector) {
//                    yes(closableFixtureCollector)
//                }
//                configurableEditor {
//                    add(closableFixtureCollector)
//                }
//                addConnectionDialog {
//                    addConnection("a","https://b.com","b","b",true)
//                    assertFalse(button("OK").isEnabled())
//                    clickButton("Cancel")
//                }
//                clickButton("Cancel")
//            }
//        }
//    }
}














