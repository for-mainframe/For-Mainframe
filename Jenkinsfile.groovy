/*
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v2.0 which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v20.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Copyright IBA Group 2020
 */
def jiraSite = 'jira-iba'
def gitCredentialsId = 'e92a3d13-efc3-47d7-955f-a78ad9d7faac'
//def gitUrl = 'https://code.iby.icdc.io/ijmp/for-mainframe.git'
def gitUrl = 'git@code.ycz.icdc.io:ijmp/for-mainframe.git'
def apacheInternalUrl = 'http://jenks2.iba6d.cmp.ycz.icdc.io'
def jenkinsServerUrl = 'http://jenks2.iba6d.cmp.ycz.icdc.io:8080'
def resultFileName = ''
String jiraTicket = ''
def gitlabBranch = env.BRANCH_NAME
properties([gitLabConnection('code.ycz.icdc.io-connection')])

// @NonCPS
// def changeVersion(String xmlFile) {

//     def xml = new XmlSlurper().parseText(xmlFile)
//     println xml.'idea-version'.'@since-build'
//     xml.'idea-version'.'@since-build' =  '203.7148.72'
//     def w = new StringWriter()
//     XmlUtil.serialize(xml, w)
//     return w.toString()
// }

pipeline {
    agent any
    triggers {
        gitlab(triggerOnPush: true, triggerOnMergeRequest: true, skipWorkInProgressMergeRequest: true,
                noteRegex: "Jenkins please retry a build")
    }
    options {
        disableConcurrentBuilds()
    }
    tools {
        gradle 'Default'
        jdk 'Java 11'
    }
    stages {
        stage('Initial checkup') {
            steps {
                sh 'java -version'
            }
        }
        // stage('Get Jira Ticket') {
        //     steps {
        //         echo gitlabBranch
        //         script {
        //             if (gitlabBranch.equals("development")) {
        //                 jiraTicket = 'development'
        //             } else if (gitlabBranch.equals("zowe-development")) {
        //                 jiraTicket = 'zowe-development'
        //             } else if (gitlabBranch.contains("release")) {
        //                 jiraTicket = gitlabBranch
        //             } else {
        //                 def pattern = ~/(?i)ijmp-\d+/
        //                 def matcher = gitlabBranch =~ pattern
        //                 if (matcher.find()) {
        //                     jiraTicket = matcher[0].toUpperCase()
        //                 } else {
        //                     jiraTicket = "null"
        //                     echo "Jira ticket name wasn't found!"
        //                 }
        //             }
        //         }
        //         echo "Jira ticket: $jiraTicket"
        //     }
        // }
        // stage('Clone Branch') {
        //     steps {
        //         cleanWs()
        //         sh "ls -la"
        //         git branch: "$gitlabBranch", credentialsId: "$gitCredentialsId", url: "$gitUrl"
        //     }
        // }
        // stage('Build Plugin IDEA') {
        //     steps {
        //         // sh 'sudo chmod +x /etc/profile.d/gradle.sh'
        //         // sh 'sudo -s source /etc/profile.d/gradle.sh'
        //         withGradle {
        //             // To change Gradle version - Jenkins/Manage Jenkins/Global Tool Configuration
        //             // sh 'gradle -v'
        //             sh 'gradle wrapper'
        //             sh './gradlew -v'
        //             sh './gradlew test'
        //             sh './gradlew buildPlugin'
        //         }
        //     }
        // }
        stage('Check build with plugin verifier') {
            steps {
                // Setup plugin verifier
                script {
                    // Create plugin verifier dirs if they are not created yet
                    def hasPluginVerifierDir = sh(returnStatus: true, script: "ls /plugin-verifier") == 0
                    if (!hasPluginVerifierDir) {
                        sh(returnStdout: false, script: "mkdir -m 775 /plugin-verifier")
                    }

                    def hasPluginVerifierIDEsDir = sh(returnStatus: true, script: "ls /plugin-verifier/ides") == 0
                    if (!hasPluginVerifierIDEsDir) {
                        sh(returnStdout: false, script: "mkdir -m 775 /plugin-verifier/ides")
                    }

                    def hasPluginVerifierJarsDir = sh(returnStatus: true, script: "ls /plugin-verifier/verifiers") == 0
                    if (!hasPluginVerifierJarsDir) {
                        sh(returnStdout: false, script: "mkdir -m 775 /plugin-verifier/verifiers")
                    }

                    // Fetch info about the plugin verifier
                    def verifierMavenCurlResp = sh(
                        returnStdout: true,
                            script: 'curl -s https://search.maven.org/solrsearch/select?q=g:"org.jetbrains.intellij.plugins"+AND+a:"verifier-cli"\\&wt=json | jq ".response"'
                    )

                    // Check if there is only on e IntelliJ plugin verifier
                    def numFound = sh(returnStdout: true, script: "#!/bin/sh -e\n" + "echo '$verifierMavenCurlResp' | jq '.numFound'").trim()
                    if (numFound != "1") {
                        error "Plugin verifier is not found (search in Maven Central gave incorrect number of found packages: $numFound)"
                    }

                    // Define verifier's latest version and name to use later
                    def latestVersion = sh(returnStdout: true, script: "#!/bin/sh -e\n" + "echo '$verifierMavenCurlResp' | jq '.docs[0].latestVersion'").trim()
                    verifierCurrName = "verifier-cli-" + latestVersion + ".jar"

                    // Remove all other versions of verifiers in the folder
                    def pluginVerifiersToDelete = sh(returnStdout: true, script: "ls /plugin-verifier/verifiers").split("\n").collect { it.split(" ") - "" }.inject([]) { result, nextArray -> result + nextArray } - verifierCurrName
                    echo pluginVerifiersToDelete.join(", ")
                    for (name in pluginVerifiersToDelete) {
                        sh(returnStdout: false, script: "rm /plugin-verifier/verifiers/$name")
                    }
                    sh(returnStdout: true, script: "ls /plugin-verifier/verifiers")
                    echo verifierCurrName
                    // curl -O http://search.maven.org/remotecontent?filepath=log4j/log4j/1.2.17/log4j-1.2.17.jar
                    echo 'Success'
                    // TODO: GitHub API requests limit in action here:
                    // curl -s https://api.github.com/repos/JetBrains/intellij-plugin-verifier/releases/latest \
                    //     | jq -r '.assets[].browser_download_url' \
                    //     | xargs curl -L --output verifier-all.jar
                }


                // script {
                //     resultFileName = sh(returnStdout: true, script: "cd build/distributions/ && ls").trim()
                // }
                // sh """
                // java -jar /plugin-verifier/verifier-all.jar check-plugin build/distributions/$resultFileName [latest-release-IU] [latest-IU] -verification-reports-dir /plugin-verifier/results
                // ls -la /plugin-verifier/results/
                // """
            }
        }
        // stage('Move to the AWS - IDEA') {
        //     steps {
        //         script {
        //             resultFileName = sh(returnStdout: true, script: "cd build/distributions/ && ls").trim()
        //         }
        //         sh """
        //         if [ "$jiraTicket" = "null" ]
        //         then
        //             echo "jira ticket is not determined"
        //         else
        //             if [ -d "/var/www/ijmp-plugin/$jiraTicket" ]
        //             then
        //                 sudo rm -r /var/www/ijmp-plugin/$jiraTicket
        //             fi
        //             sudo mkdir -p /var/www/ijmp-plugin/$jiraTicket
        //             sudo mkdir /var/www/ijmp-plugin/$jiraTicket/idea
        //             sudo mkdir /var/www/ijmp-plugin/$jiraTicket/pycharm

        //             sudo mv build/distributions/$resultFileName /var/www/ijmp-plugin/$jiraTicket/idea
        //         fi
        //         """
        //     }
        //     post {
        //         success {
        //             script {
        //                 if (!jiraTicket.contains('release') && !'development'.equals(jiraTicket) && !'zowe-development'.equals(jiraTicket) && !"null".equals(jiraTicket)) {
        //                     jiraAddComment idOrKey: "$jiraTicket", comment: "Hello! It's jenkins. Your push in branch was successfully built. You can download your build from the following link $apacheInternalUrl/ijmp-plugin/$jiraTicket/idea/$resultFileName.", site: "$jiraSite"
        //                 }

        //             }
        //         }
        //         failure {
        //             script {
        //                 if (!jiraTicket.contains('release') && !'development'.equals(jiraTicket) && !'zowe-development'.equals(jiraTicket) && !"null".equals(jiraTicket)) {
        //                     jiraAddComment idOrKey: "$jiraTicket", comment: "Hello! It's jenkins. Your push in branch failed to build for Intellij IDEA. You can get console output by the following link $jenkinsServerUrl/job/BuildPluginPipeline/", site: "$jiraSite"
        //                 }
        //             }
        //         }
        //     }
        // }

        // stage('Change Plugin Version'){
        //     steps{
        //         script{
        //             def xmlFileData = readFile(file: "src/main/resources/META-INF/plugin.xml")
        //             def res = changeVersion(xmlFileData)
        //             writeFile file: "src/main/resources/META-INF/plugin.xml", text: res
        //         }
    }
}
