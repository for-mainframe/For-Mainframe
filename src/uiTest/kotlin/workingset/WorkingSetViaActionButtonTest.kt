/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package workingset

import auxiliary.*
import auxiliary.closable.ClosableFixtureCollector
import auxiliary.containers.*
import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.HeavyWeightWindowFixture
import com.intellij.remoterobot.search.locators.Locator
import com.intellij.remoterobot.search.locators.byXpath
import com.intellij.remoterobot.utils.WaitForConditionTimeoutException
import io.kotest.matchers.string.shouldContain
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration


/**
 * Tests creating working sets and masks via action button.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(RemoteRobotExtension::class)
class WorkingSetViaActionButtonTest {
    private var closableFixtureCollector = ClosableFixtureCollector()
    private var fixtureStack = mutableListOf<Locator>()
    private var wantToClose = mutableListOf("Add Working Set Dialog")

    private val projectName = "untitled"
    private val connectionName = "valid connection"

    /**
     * Opens the project and Explorer, clears test environment.
     */
    @BeforeAll
    fun setUpAll(remoteRobot: RemoteRobot) {
        startMockServer()
        setUpTestEnvironment(projectName, fixtureStack, closableFixtureCollector, remoteRobot)
    }

    /**
     * Closes the project and clears test environment.
     */
    @AfterAll
    fun tearDownAll(remoteRobot: RemoteRobot) = with(remoteRobot) {
        mockServer.shutdown()
        clearEnvironment(projectName, fixtureStack, closableFixtureCollector, remoteRobot)
        ideFrameImpl(projectName, fixtureStack) {
            close()
        }
    }

    /**
     * Closes all unclosed closable fixtures that we want to close.
     */
    @AfterEach
    fun tearDown(remoteRobot: RemoteRobot) {
        responseDispatcher.removeAllEndpoints()
        closableFixtureCollector.closeWantedClosables(wantToClose, remoteRobot)
    }

    /**
     * Tests to add new working set without connection, checks that correct message is returned.
     */
    @Test
    @Order(1)
    fun testAddWorkingSetWithoutConnectionViaActionButton(testInfo: TestInfo, remoteRobot: RemoteRobot) =
        with(remoteRobot) {
            val wsName = "first ws"
            responseDispatcher.injectEndpoint(
                "${testInfo.displayName}_info",
                { it?.requestLine?.contains("zosmf/info") ?: false },
                { MockResponse().setBody(responseDispatcher.readMockJson("infoResponse") ?: "") }
            )
            responseDispatcher.injectEndpoint(
                "${testInfo.displayName}_resttopology",
                { it?.requestLine?.contains("zosmf/resttopology/systems") ?: false },
                { MockResponse().setBody(responseDispatcher.readMockJson("infoResponse") ?: "") }
            )
            ideFrameImpl(projectName, fixtureStack) {
                createWorkingSetFromActionButton(closableFixtureCollector, fixtureStack)

                try {
                    if (dialog("Add Working Set Dialog").isShowing) {
                        Assertions.assertTrue(false)
                    }
                } catch (e: WaitForConditionTimeoutException) {
                    e.message.shouldContain("Failed to find 'Dialog' by 'title Add Working Set Dialog'")
                } finally {
                    closableFixtureCollector.closeOnceIfExists(AddWorkingSetDialog.name)
                }
                createConnectionFromActionButton(closableFixtureCollector, fixtureStack)
                addConnectionDialog(fixtureStack) {
                    addConnection(
                        connectionName,
                        "https://${mockServer.hostName}:${mockServer.port}",
                        ZOS_USERID,
                        ZOS_PWD,
                        true
                    )
                    clickButton("OK")
                    Thread.sleep(2000)
                }
                createWorkingSetFromActionButton(closableFixtureCollector, fixtureStack)
                addWorkingSetDialog(fixtureStack) {
                    addWorkingSet(wsName, connectionName)
                    clickButton("OK")
                    Thread.sleep(2000)
                    find<HeavyWeightWindowFixture>(byXpath("//div[@class='HeavyWeightWindow']")).findText(
                        EMPTY_DATASET_MESSAGE
                    )
                    clickButton("OK")
                    Thread.sleep(2000)
                }
                closableFixtureCollector.closeOnceIfExists(AddWorkingSetDialog.name)
            }
        }

    /**
     * Tests to add new empty working set with very long name, checks that correct message is returned.
     */
    @Test
    @Order(2)
    fun testAddEmptyWorkingSetWithVeryLongNameViaActionButton(remoteRobot: RemoteRobot) = with(remoteRobot) {
        val wsName: String = "B".repeat(200)
        ideFrameImpl(projectName, fixtureStack) {
            createWorkingSetFromActionButton(closableFixtureCollector, fixtureStack)
            addWorkingSetDialog(fixtureStack) {
                addWorkingSet(wsName, connectionName)
                clickButton("OK")
                Thread.sleep(2000)
                find<HeavyWeightWindowFixture>(byXpath("//div[@class='HeavyWeightWindow']")).findText(
                    EMPTY_DATASET_MESSAGE
                )
                clickButton("OK")
                Thread.sleep(2000)
            }
            closableFixtureCollector.closeOnceIfExists(AddWorkingSetDialog.name)
        }
    }

    /**
     * Tests to add new working set with one valid mask.
     */
    @Test
    @Order(3)
    fun testAddWorkingSetWithOneValidMaskViaActionButton(remoteRobot: RemoteRobot) = with(remoteRobot) {
        val wsName = "WS1"
        val mask = Pair("$ZOS_USERID.*", "z/OS")
        ideFrameImpl(projectName, fixtureStack) {
            createWorkingSetFromActionButton(closableFixtureCollector, fixtureStack)
            addWorkingSetDialog(fixtureStack) {
                addWorkingSet(wsName, connectionName, mask)
                clickButton("OK")
                Thread.sleep(2000)
            }
            closableFixtureCollector.closeOnceIfExists(AddWorkingSetDialog.name)
        }
    }

    /**
     * Tests to add new working set with several valid z/OS masks, opens masks in explorer.
     */
    @Test
    @Order(4)
    fun testAddWorkingSetWithValidZOSMasksViaActionButton(testInfo: TestInfo, remoteRobot: RemoteRobot) =
        with(remoteRobot) {
            val wsName = "WS2"
            val masks: ArrayList<Pair<String, String>> = ArrayList()
            //todo allocate dataset with 44 length when 'Allocate Dataset Dialog' implemented

            validZOSMasks.forEach {
                masks.add(Pair(it, "z/OS"))
            }
            responseDispatcher.injectEndpoint(
                "${testInfo.displayName}_restfiles",
                { it?.requestLine?.contains("zosmf/restfiles/ds?dslevel") ?: false },
                { MockResponse().setBody("{}") }
            )

            ideFrameImpl(projectName, fixtureStack) {
                createWorkingSetFromActionButton(closableFixtureCollector, fixtureStack)
                addWorkingSetDialog(fixtureStack) {
                    addWorkingSet(wsName, connectionName, masks)
                    clickButton("OK")
                    Thread.sleep(2000)
                }
                closableFixtureCollector.closeOnceIfExists(AddWorkingSetDialog.name)
            }
            validZOSMasks.forEach {
                openWSOpenMaskInExplorer(
                    wsName,
                    it.uppercase(),
                    projectName,
                    fixtureStack,
                    remoteRobot
                )
            }
        }

    /**
     * Tests to add new working set with several valid USS masks, opens masks in explorer.
     */
    @Test
    @Order(5)
    fun testAddWorkingSetWithValidUSSMasksViaActionButton(testInfo: TestInfo, remoteRobot: RemoteRobot) =
        with(remoteRobot) {
            val wsName = "WS3"
            val masks: ArrayList<Pair<String, String>> = ArrayList()

            validUSSMasks.forEach {
                masks.add(Pair(it, "USS"))
            }

            responseDispatcher.injectEndpoint(
                "${testInfo.displayName}_restfiles",
                { it?.requestLine?.contains("zosmf/restfiles/fs?path") ?: false },
                { MockResponse().setBody("{}") }
            )

            ideFrameImpl(projectName, fixtureStack) {
                createWorkingSetFromActionButton(closableFixtureCollector, fixtureStack)
                addWorkingSetDialog(fixtureStack) {
                    addWorkingSet(wsName, connectionName, masks)
                    clickButton("OK")
                    Thread.sleep(2000)
                }
                closableFixtureCollector.closeOnceIfExists(AddWorkingSetDialog.name)
            }
            validUSSMasks.forEach { openWSOpenMaskInExplorer(wsName, it, projectName, fixtureStack, remoteRobot) }
        }

    /**
     * Tests to add new working set with invalid masks, checks that correct messages are returned.
     */
    @Test
    @Order(6)
    fun testAddWorkingSetWithInvalidMasksViaActionButton(remoteRobot: RemoteRobot) = with(remoteRobot) {
        val wsName = "WS4"
        ideFrameImpl(projectName, fixtureStack) {
            createWorkingSetFromActionButton(closableFixtureCollector, fixtureStack)
            addWorkingSetDialog(fixtureStack) {
                addWorkingSet(wsName, connectionName)
                maskMessageMap.forEach {
                    addMask(Pair(it.key, "z/OS"))
                    if (button("OK").isEnabled()) {
                        clickButton("OK")
                    } else {
                        findText("OK").moveMouse()
                    }
                    if (it.key.length < 49) {
                        findText(it.key.uppercase()).moveMouse()
                    } else {
                        findText("${it.key.uppercase().substring(0, 46)}...").moveMouse()
                    }
                    Thread.sleep(2000)
                    find<HeavyWeightWindowFixture>(byXpath("//div[@class='HeavyWeightWindow'][.//div[@class='Header']]")).findText(
                        it.value
                    )
                    assertFalse(button("OK").isEnabled())
                    findText(it.key.uppercase()).click()
                    clickActionButton(byXpath("//div[contains(@myvisibleactions, 'Down')]//div[@myaction.key='button.text.remove']"))
                }
                clickButton("Cancel")
            }
            closableFixtureCollector.closeOnceIfExists(AddWorkingSetDialog.name)
        }
    }

    /**
     * Tests to add working set with the same masks, checks that correct message is returned.
     */
    @Test
    @Order(7)
    fun testAddWorkingSetWithTheSameMasksViaActionButton(remoteRobot: RemoteRobot) = with(remoteRobot) {
        val wsName = "WS4"
        ideFrameImpl(projectName, fixtureStack) {
            createWorkingSetFromActionButton(closableFixtureCollector, fixtureStack)
            addWorkingSetDialog(fixtureStack) {
                addWorkingSet(wsName, connectionName)
                addMask(Pair("$ZOS_USERID.*", "z/OS"))
                addMask(Pair("$ZOS_USERID.*", "z/OS"))
                clickButton("OK")
                find<HeavyWeightWindowFixture>(
                    byXpath("//div[@class='HeavyWeightWindow']"),
                    Duration.ofSeconds(30)
                ).findText(IDENTICAL_MASKS_MESSAGE)
                assertFalse(button("OK").isEnabled())
                clickButton("Cancel")
            }
            closableFixtureCollector.closeOnceIfExists(AddWorkingSetDialog.name)
        }
    }
}
