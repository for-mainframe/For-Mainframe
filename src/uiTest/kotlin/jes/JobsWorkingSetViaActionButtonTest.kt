/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

package jes

import auxiliary.*
import auxiliary.closable.ClosableFixtureCollector
import auxiliary.containers.*
import com.intellij.remoterobot.RemoteRobot
import com.intellij.remoterobot.fixtures.ActionButtonFixture
import com.intellij.remoterobot.fixtures.ComponentFixture
import com.intellij.remoterobot.fixtures.HeavyWeightWindowFixture
import com.intellij.remoterobot.search.locators.Locator
import com.intellij.remoterobot.search.locators.byXpath
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.extension.ExtendWith
import java.time.Duration


/**
 * Tests creating jobs working sets and filters via action button.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(RemoteRobotExtension::class)
class JobsWorkingSetViaActionButtonTest {
    private var closableFixtureCollector = ClosableFixtureCollector()
    private var fixtureStack = mutableListOf<Locator>()
    private var wantToClose = mutableListOf("Add Jobs Working Set Dialog")

    private val projectName = "untitled"
    private val connectionName = "valid connection"

    /**
     * Opens the project and Explorer, clears test environment.
     */
    @BeforeAll
    fun setUpAll(remoteRobot: RemoteRobot) {
        setUpTestEnvironment(projectName, fixtureStack, closableFixtureCollector, remoteRobot)
    }

    /**
     * Closes the project and clears test environment.
     */
    @AfterAll
    fun tearDownAll(remoteRobot: RemoteRobot) = with(remoteRobot) {
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
        closableFixtureCollector.closeWantedClosables(wantToClose, remoteRobot)
    }

    /**
     * Tests to add new jobs working set without connection, checks that correct message is returned.
     */
    @Test
    @Order(1)
    fun testAddJobsWorkingSetWithoutConnectionViaActionButton(remoteRobot: RemoteRobot) = with(remoteRobot) {
        val jwsName = "first jws"
        ideFrameImpl(projectName, fixtureStack) {
            explorer {
                jesExplorer.click()
                createJobsWorkingSet(closableFixtureCollector, fixtureStack)
            }
            addConnectionDialog(fixtureStack) {
                addConnection(connectionName, CONNECTION_URL, ZOS_USERID, ZOS_PWD, true)
                clickButton("OK")
                Thread.sleep(5000)
            }
            addJobsWorkingSetDialog(fixtureStack) {
                addJobsWorkingSet(jwsName, connectionName)
                clickButton("OK")
                Thread.sleep(3000)
                find<HeavyWeightWindowFixture>(byXpath("//div[@class='HeavyWeightWindow']")).findText(
                    EMPTY_DATASET_MESSAGE
                )
                clickButton("OK")
                Thread.sleep(3000)
            }
            closableFixtureCollector.closeOnceIfExists(AddJobsWorkingSetDialog.name)
        }
    }

    /**
     * Tests to add new empty jobs working set with very long name, checks that correct message is returned.
     */
    @Test
    @Order(2)
    fun testAddEmptyJobsWorkingSetWithVeryLongNameViaActionButton(remoteRobot: RemoteRobot) = with(remoteRobot) {
        val jwsName: String = "B".repeat(200)
        ideFrameImpl(projectName, fixtureStack) {
            explorer {
                jesExplorer.click()
                createJobsWorkingSet(closableFixtureCollector, fixtureStack)
            }
            addJobsWorkingSetDialog(fixtureStack) {
                addJobsWorkingSet(jwsName, connectionName)
                clickButton("OK")
                Thread.sleep(3000)
                find<HeavyWeightWindowFixture>(byXpath("//div[@class='HeavyWeightWindow']")).findText(
                    EMPTY_DATASET_MESSAGE
                )
                clickButton("OK")
                Thread.sleep(3000)
            }
            closableFixtureCollector.closeOnceIfExists(AddJobsWorkingSetDialog.name)
        }
    }

    /**
     * Tests to add new jobs working set with one valid filter.
     */
    @Test
    @Order(3)
    fun testAddJobsWorkingSetWithOneValidFilterViaActionButton(remoteRobot: RemoteRobot) = with(remoteRobot) {
        val jwsName = "JWS1"
        val filter = Triple("*", ZOS_USERID, "")
        ideFrameImpl(projectName, fixtureStack) {
            explorer {
                jesExplorer.click()
                createJobsWorkingSet(closableFixtureCollector, fixtureStack)
            }
            addJobsWorkingSetDialog(fixtureStack) {
                addJobsWorkingSet(jwsName, connectionName, filter)
                clickButton("OK")
                Thread.sleep(5000)
            }
            closableFixtureCollector.closeOnceIfExists(AddJobsWorkingSetDialog.name)
        }
    }

    /**
     * Tests to add new jobs working set with several valid filters, opens filters in explorer.
     */
    @Test
    @Order(4)
    fun testAddJobsWorkingSetWithValidFiltersViaActionButton(remoteRobot: RemoteRobot) = with(remoteRobot) {
        val jwsName = "JWS2"
        ideFrameImpl(projectName, fixtureStack) {
            explorer {
                jesExplorer.click()
                createJobsWorkingSet(closableFixtureCollector, fixtureStack)
            }
            addJobsWorkingSetDialog(fixtureStack) {
                addJobsWorkingSet(jwsName, connectionName, validJobsFilters)
                clickButton("OK")
                Thread.sleep(5000)
            }
            closableFixtureCollector.closeOnceIfExists(AddJobsWorkingSetDialog.name)
        }
        openOrCloseWorkingSetInExplorer(jwsName, projectName, fixtureStack, remoteRobot)
        validJobsFilters.forEach {
            openJobFilterInExplorer(it, "", projectName, fixtureStack, remoteRobot)
            closeFilterInExplorer(it, projectName, fixtureStack, remoteRobot)
        }
        openOrCloseWorkingSetInExplorer(jwsName, projectName, fixtureStack, remoteRobot)
    }


    /**
     * Tests to add new jobs working set with invalid filters, checks that correct messages are returned.
     */
    @Test
    @Order(5)
    fun testAddJobsWorkingSetWithInvalidFiltersViaActionButton(remoteRobot: RemoteRobot) = with(remoteRobot) {
        val jwsName = "JWS3"
        ideFrameImpl(projectName, fixtureStack) {
            explorer {
                jesExplorer.click()
                createJobsWorkingSet(closableFixtureCollector, fixtureStack)
            }
            addJobsWorkingSetDialog(fixtureStack) {
                addJobsWorkingSet(jwsName, connectionName)
                invalidJobsFiltersMap.forEach {
                    addFilter(it.key.first)
                    if (button("OK").isEnabled()) {
                        clickButton("OK")
                    } else {
                        findText("OK").moveMouse()
                    }
                    val textToMoveMouse = when (it.key.second) {
                        1 -> it.key.first.first
                        2 -> it.key.first.second
                        else -> it.key.first.third
                    }
                    findText(textToMoveMouse).moveMouse()
                    Thread.sleep(5000)
                    find<HeavyWeightWindowFixture>(byXpath("//div[@class='HeavyWeightWindow'][.//div[@class='Header']]")).findText(
                        it.value
                    )
                    assertFalse(button("OK").isEnabled())
                    findText("Prefix").click()
                    clickActionButton(byXpath("//div[contains(@myvisibleactions, 'Down')]//div[@myaction.key='button.text.remove']"))

                }
                clickButton("Cancel")
                Thread.sleep(5000)
            }
            closableFixtureCollector.closeOnceIfExists(AddJobsWorkingSetDialog.name)
        }
    }

    /**
     * Tests to add jobs working set with the same filters, checks that correct message is returned.
     */
    @Test
    @Order(6)
    fun testAddJobsWorkingSetWithTheSameFiltersViaActionButton(remoteRobot: RemoteRobot) = with(remoteRobot) {
        val jwsName = "JWS3"
        ideFrameImpl(projectName, fixtureStack) {
            explorer {
                jesExplorer.click()
                createJobsWorkingSet(closableFixtureCollector, fixtureStack)
            }
            addJobsWorkingSetDialog(fixtureStack) {
                addJobsWorkingSet(jwsName, connectionName, Triple("*", ZOS_USERID.lowercase(), ""))
                addJobsWorkingSet(jwsName, connectionName, Triple("*", ZOS_USERID.lowercase(), ""))
                clickButton("OK")
                find<HeavyWeightWindowFixture>(
                    byXpath("//div[@class='HeavyWeightWindow']"),
                    Duration.ofSeconds(30)
                ).findText(IDENTICAL_FILTERS_MESSAGE)
                assertFalse(button("OK").isEnabled())
                clickButton("Cancel")
            }
            closableFixtureCollector.closeOnceIfExists(AddJobsWorkingSetDialog.name)
        }
    }

    /**
     * Tests to add jobs working set with invalid connection, checks that correct message is returned.
     */
    @Test
    @Order(7)
    fun testAddJWSWithInvalidConnectionViaActionButton(remoteRobot: RemoteRobot) = with(remoteRobot) {
        createConnection(projectName, fixtureStack, closableFixtureCollector, "invalid_connection", false, remoteRobot)
        val jwsName = "JWS3"
        ideFrameImpl(projectName, fixtureStack) {
            explorer {
                jesExplorer.click()
                createJobsWorkingSet(closableFixtureCollector, fixtureStack)
            }
            addJobsWorkingSetDialog(fixtureStack) {
                addJobsWorkingSet(jwsName, "invalid_connection", Triple("*", ZOS_USERID, ""))
                clickButton("OK")
                Thread.sleep(5000)
            }
            closableFixtureCollector.closeOnceIfExists(AddJobsWorkingSetDialog.name)
        }
        openOrCloseWorkingSetInExplorer(jwsName, projectName, fixtureStack, remoteRobot)
        findAll<ComponentFixture>(byXpath("//div[@class='MyComponent'][.//div[@accessiblename='Invalid URL port: \"104431\"' and @class='JEditorPane']]")).forEach {
            it.click()
            findAll<ActionButtonFixture>(
                byXpath("//div[@class='ActionButton' and @myicon= 'close.svg']")
            ).first().click()
        }
        openOrCloseWorkingSetInExplorer(jwsName, projectName, fixtureStack, remoteRobot)
    }
}
