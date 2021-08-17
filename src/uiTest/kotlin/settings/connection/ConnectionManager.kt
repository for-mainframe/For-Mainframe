package settings.connection

import com.intellij.remoterobot.RemoteRobot
import org.junit.jupiter.api.extension.ExtendWith
import auxiliary.*
import auxiliary.closable.ClosableFixtureCollector
import auxiliary.containers.*
import com.intellij.remoterobot.fixtures.ContainerFixture
import com.intellij.remoterobot.search.locators.Locator
import com.intellij.remoterobot.search.locators.byXpath
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertFalse

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(RemoteRobotExtension::class)
class ConnectionManager {
    private var closableFixtureCollector = ClosableFixtureCollector()
    private var stack = mutableListOf<Locator>()
    private val wantToClose = listOf("Settings Dialog", "Add Connection Dialog", "Error Creating Connection Dialog", "Edit Connection Dialog")
    private val projectName = "untitled"

    @BeforeAll
    fun setUpAll(remoteRobot: RemoteRobot) = with(remoteRobot) {
        welcomeFrame {
            open(projectName)
        }
        Thread.sleep(30000)
        ideFrameImpl(projectName, stack) {
            forMainframe()
        }
    }

    @AfterAll
    fun tearDownAll(remoteRobot: RemoteRobot) = with(remoteRobot) {
        ideFrameImpl(projectName, stack) {
            close()
        }
    }

    @AfterEach
    fun tearDown(remoteRobot: RemoteRobot) {
        closableFixtureCollector.closeWantedClosables(wantToClose, remoteRobot)
    }

    @Test
    fun testA(remoteRobot: RemoteRobot) = with(remoteRobot) {
        ideFrameImpl(projectName, stack) {
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
                assertFalse(true)
            }
        }
    }

    @Test
    fun testB(remoteRobot: RemoteRobot) = with(remoteRobot) {
        ideFrameImpl(projectName, stack) {
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
                closableFixtureCollector.closeOnceIfExists(AddConnectionDialog.name)
                errorCreatingConnectionDialog(closableFixtureCollector, stack) {
                    yes()
                }
                closableFixtureCollector.closeOnceIfExists(ErrorCreatingConnectionDialog.name)
                configurableEditor {
                    add(closableFixtureCollector, stack)
                }
                addConnectionDialog(stack) {
                    addConnection("a","https://b.com","b","b",true)
                    assertFalse(button("OK").isEnabled())
                    clickButton("Cancel")
                }
                closableFixtureCollector.closeOnceIfExists(AddConnectionDialog.name)
                clickButton("Cancel")
            }
            closableFixtureCollector.closeOnceIfExists(SettingsDialog.name)
        }
    }
}














