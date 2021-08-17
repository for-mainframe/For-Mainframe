import org.jetbrains.intellij.tasks.PatchPluginXmlTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.jetbrains.intellij") version "0.6.5"
  kotlin("jvm") version "1.4.32"
  java
}

apply(plugin = "kotlin")
apply(plugin = "org.jetbrains.intellij")

group = "eu.ibagroup"
version = "0.4.1"
val remoteRobotVersion = "0.10.0"

repositories {
  mavenCentral()
  maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
  flatDir {
    dir("libs")
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType(KotlinCompile::class).all {
  kotlinOptions {
    jvmTarget = "1.8"
    languageVersion = "1.4"
  }
}

dependencies {
  implementation(group = "com.squareup.retrofit2", name = "retrofit", version = "2.9.0")
  implementation("com.squareup.retrofit2:converter-gson:2.5.0")
  implementation("com.squareup.retrofit2:converter-scalars:2.1.0")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.30")
  implementation("org.jetbrains.kotlin:kotlin-reflect:1.4.30")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
  implementation("org.jgrapht:jgrapht-core:1.5.0")
  implementation("eu.ibagroup:r2z:1.0.14")
  implementation("com.segment.analytics.java:analytics:+")
  testImplementation("io.mockk:mockk:1.10.2")
  testImplementation("org.mock-server:mockserver-netty:5.11.1")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
  testImplementation("com.intellij.remoterobot:remote-robot:$remoteRobotVersion")
  testImplementation("com.intellij.remoterobot:remote-fixtures:1.1.18")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.1")
  testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.7.1")
}

intellij {
  version = "2021.1.3"
}

tasks.getByName<PatchPluginXmlTask>("patchPluginXml") {
  sinceBuild("203.5981")
  untilBuild("211.*")
  changeNotes(
    """
      In version 0.4.1 we added:<br/>
      <ul>
        <li>Pride logo to support LGBTQIA+ community. Peace, love, pride</li>
        <li>Job submission by the right click on files in the File Explorer</li>
        <li>Move and Copy operations are available for USS files and directories</li>
        <li>Editing Working Sets is now accessible by the right click on the Working Set in the File Explorer</li>
        <li>Tracking analytics events is now enabled with corresponding Privacy Policy</li>
        <li>Small UI fixes.</li>
      </ul>"""
  )
}

tasks.test {
  useJUnitPlatform()
  testLogging {
    events("passed", "skipped", "failed")
  }
}

sourceSets {
  create("apiTest") {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
    java.srcDirs("src/apiTest/java", "src/apiTest/kotlin")
    resources.srcDirs("src/apiTest/resources")
  }
  create("uiTest") {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
    java.srcDirs("src/uiTest/java", "src/uiTest/kotlin")
    resources.srcDirs("src/uiTest/resources")
  }
}

val apiTestImplementation by configurations.getting {
  extendsFrom(configurations.testImplementation.get())
}
val uiTestImplementation by configurations.getting {
  extendsFrom(configurations.testImplementation.get())
}

configurations["apiTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())
configurations["uiTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())

val apiTest = task<Test>("apiTest") {
  description = "Runs the integration tests for API."
  group = "verification"
  testClassesDirs = sourceSets["apiTest"].output.classesDirs
  classpath = sourceSets["apiTest"].runtimeClasspath
  testLogging {
    events("passed", "skipped", "failed")
  }
}
val uiTest = task<Test>("uiTest") {
  description = "Runs the integration tests for UI."
  group = "verification"
  testClassesDirs = sourceSets["uiTest"].output.classesDirs
  classpath = sourceSets["uiTest"].runtimeClasspath
  useJUnitPlatform() {
    excludeTags("FirstTime")
  }
  testLogging {
    events("passed", "skipped", "failed")
  }
}
val firstTimeUiTest = task<Test>("firstTimeUiTest") {
  description = "Gets rid of license agreement, etc."
  group = "verification"
  testClassesDirs = sourceSets["uiTest"].output.classesDirs
  classpath = sourceSets["uiTest"].runtimeClasspath
  useJUnitPlatform() {
    includeTags("FirstTime")
  }
  testLogging {
    events("passed", "skipped", "failed")
  }
}

tasks.downloadRobotServerPlugin {
  version = remoteRobotVersion
}

/**
 * TODO ("change port for server IDEA")
 */
tasks.runIdeForUiTests {
//    In case your Idea is launched on remote machine you can enable public port and enable encryption of JS calls
//    systemProperty "robot-server.host.public", "true"
//    systemProperty "robot.encryption.enabled", "true"
//    systemProperty "robot.encryption.password", "my super secret"

  //this does not work
//    System.setProperty("robot-server.port", "8082")
//    System.setProperty("ide.mac.message.dialogs.as.sheets", "false")
//    System.setProperty("jb.privacy.policy.text", "<!--999.999-->")
//    System.setProperty("jb.consents.confirmation.enabled", "false")
}

tasks.register<Test>("dev"){
  group = "verification"
  useJUnitPlatform {
    includeTags("dev")
  }
}