/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */

import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.jetbrains.intellij") version "1.6.0"
  kotlin("jvm") version "1.6.21"
  java
  jacoco
}

apply(plugin = "kotlin")
apply(plugin = "org.jetbrains.intellij")

group = "eu.ibagroup"
version = "0.6.1"
val remoteRobotVersion = "0.11.14"

repositories {
  mavenCentral()
  maven {
    url = uri("http://10.221.23.186:8082/repository/internal/")
    isAllowInsecureProtocol = true
    credentials {
      username = "admin"
      password = "password123"
    }
    maven("https://packages.jetbrains.team/maven/p/ij/intellij-dependencies")
    flatDir {
      dir("libs")
    }
    metadataSources {
      mavenPom()
      artifact()
      ignoreGradleMetadataRedirection()
    }
  }
}

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
  implementation(group = "com.squareup.retrofit2", name = "retrofit", version = "2.9.0")
  implementation("com.squareup.retrofit2:converter-gson:2.9.0")
  implementation("com.squareup.retrofit2:converter-scalars:2.9.0")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.20")
  implementation("org.jetbrains.kotlin:kotlin-reflect:1.6.20")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.1")
  implementation("org.jgrapht:jgrapht-core:1.5.1")
  implementation("eu.ibagroup:r2z:1.0.27")
  implementation("com.segment.analytics.java:analytics:+")
  testImplementation("io.mockk:mockk:1.10.2")
  testImplementation("org.mock-server:mockserver-netty:5.11.1")
  testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.1")
  testImplementation("io.kotest:kotest-assertions-core:5.3.0")
  testImplementation("io.kotest:kotest-runner-junit5:5.3.0")
  testImplementation("com.intellij.remoterobot:remote-robot:$remoteRobotVersion")
  testImplementation("com.intellij.remoterobot:remote-fixtures:$remoteRobotVersion")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
  testRuntimeOnly("org.junit.vintage:junit-vintage-engine:5.8.2")
}

intellij {
  version.set("2022.1")
}

tasks {
  withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = JavaVersion.VERSION_11.toString()
      languageVersion = org.jetbrains.kotlin.config.LanguageVersion.LATEST_STABLE.versionString
    }
  }

  patchPluginXml {
    sinceBuild.set("203.5981")
    untilBuild.set("221.*")
    changeNotes.set(
      """
      <b>New features:</b>
      <ul>
      </ul>
      <b>Detailed changes list:</b>
      <ul>
      </ul>
      <b>Fixed bugs:</b>
      <ul>
      </ul>"""
    )
  }

  test {
    useJUnitPlatform()
    testLogging {
      events("passed", "skipped", "failed")
    }

    configure<JacocoTaskExtension> {
      isIncludeNoLocationClasses = true
      excludes = listOf("jdk.internal.*")
    }

    finalizedBy("jacocoTestReport")
  }

  jacocoTestReport {
    classDirectories.setFrom(
      files(classDirectories.files.map {
        fileTree(it) {
          exclude("${buildDir}/instrumented/**")
        }
      })
    )
  }
}

/**
 * Adds uiTest source sets
 */
sourceSets {
  create("uiTest") {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
    java.srcDirs("src/uiTest/java", "src/uiTest/kotlin")
    resources.srcDirs("src/uiTest/resources")
  }
}

/**
 * configures the UI Tests to inherit the testImplementation in dependencies
 */
val uiTestImplementation by configurations.getting {
  extendsFrom(configurations.testImplementation.get())
}

/**
 * configures the UI Tests to inherit the testRuntimeOnly in dependencies
 */
configurations["uiTestRuntimeOnly"].extendsFrom(configurations.testRuntimeOnly.get())

/**
 * runs UI tests
 */
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

/**
 * Runs the first UI test, which agrees to the license agreement
 */
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
  version.set(remoteRobotVersion)
}

tasks.runIdeForUiTests {
  systemProperty("idea.trust.all.projects", "true")
  systemProperty("ide.show.tips.on.startup.default.value","false")
}

tasks {
  withType<Copy> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
  }
}
