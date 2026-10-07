pipeline {
    agent none
    options {
        skipDefaultCheckout()
    }
    stages {
        stage('Build jar') {
            agent { label 'lin' }
            steps {
                cleanWs()
                script {
                    def scmVars = checkout scm
                    // Only package files that passed 'planethour dev': its passes are recorded on lin by git tree,
                    // so a merge commit with the same files as a tested branch head also qualifies.
                    env.RELEASE_TREE = sh(returnStdout: true, script: 'git rev-parse "HEAD^{tree}"').trim()
                    def devBuild = sh(returnStdout: true, script: 'head -n 1 "$HOME/jenkins/dev-verified/planethour/$RELEASE_TREE" 2>/dev/null || true').trim()
                    if (!devBuild) {
                        currentBuild.description = "${scmVars.GIT_COMMIT.substring(0, 8)}: blocked, not dev-tested"
                        error("Commit ${scmVars.GIT_COMMIT.substring(0, 8)} has not passed 'planethour dev'. Check it out in the working copy with no uncommitted changes, build it in the IDE, wait for 'planethour dev' to succeed, then run this release again.")
                    }
                    echo sh(returnStdout: true, script: 'tail -n 1 "$HOME/jenkins/dev-verified/planethour/$RELEASE_TREE"').trim()
                    env.APP_VERSION = sh(returnStdout: true, script: 'mvn -B -q help:evaluate -Dexpression=project.version -DforceStdout').trim().replace('-SNAPSHOT', '')
                    if (!(env.APP_VERSION ==~ /\d+(\.\d+){0,2}/)) {
                        error("pom.xml version '${env.APP_VERSION}' is not a valid installer version; use e.g. 1.2 or 1.2.3")
                    }
                    currentBuild.description = "v${env.APP_VERSION} @ ${scmVars.GIT_COMMIT.substring(0, 8)}, dev #${devBuild}"
                }
                sh 'mvn -B -ntp clean verify'
                sh '''
                    mkdir -p app
                    cp target/planethour.jar app/
                    cp LICENSE app/LICENSE.txt
                '''
                stash name: 'app', includes: 'app/**'
                stash name: 'icons', includes: 'extras/PlanetHour.*'
                archiveArtifacts artifacts: 'app/planethour.jar', fingerprint: true
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }
        stage('Package') {
            parallel {
                stage('Windows') {
                    agent { label 'win' }
                    stages {
                        stage('Windows: Creating minimal JRE') {
                            steps {
                                cleanWs()
                                unstash 'app'
                                unstash 'icons'
                                script {
                                    def deps = bat(returnStdout: true, script: '@echo off && "%JAVA_HOME%\\bin\\jdeps" --print-module-deps app\\planethour.jar').trim()
                                    echo "Dependencies: '${deps}'"
                                    withEnv(["DEPENDS=${deps}"]) {
                                        bat '@echo off && "%JAVA_HOME%\\bin\\jlink" --compress=zip-6 --strip-debug --no-header-files --no-man-pages --add-modules "%DEPENDS%" --output jre'
                                    }
                                }
                            }
                        }
                        stage('Pack Windows Installer') {
                            steps {
                                script {
                                    fileOperations([
                                        fileDownloadOperation(password: '', proxyHost: '', proxyPort: '', targetFileName: 'wix314-binaries.zip', targetLocation: '', url: 'https://github.com/wixtoolset/wix3/releases/download/wix3141rtm/wix314-binaries.zip', userName: ''),
                                        fileUnZipOperation(filePath: 'wix314-binaries.zip', targetLocation: 'wix')
                                    ])
                                    def wix = pwd() + '\\wix'
                                    withEnv(["PATH+WIX=${wix}"]) {
                                        bat '@echo off && "%JAVA_HOME%\\bin\\jpackage" --input app --name PlanetHour --description "Planetary hour calculator" --vendor "Sounak Choudhury" --copyright "Copyright (C) 2013-2026 Sounak Choudhury" --app-version %APP_VERSION% --main-jar planethour.jar --runtime-image jre --type msi --license-file app\\LICENSE.txt --icon extras\\PlanetHour.ico --win-dir-chooser --win-menu --win-menu-group PlanetHour --win-shortcut'
                                    }
                                }
                            }
                        }
                        stage('Export MSI') {
                            steps {
                                archiveArtifacts artifacts: '*.msi', followSymlinks: false
                            }
                        }
                    }
                }
                stage('Ubuntu') {
                    agent { label 'lin' }
                    stages {
                        stage('Ubuntu: Creating minimal JRE') {
                            steps {
                                cleanWs()
                                unstash 'app'
                                unstash 'icons'
                                sh 'jlink --compress=zip-6 --strip-debug --no-header-files --no-man-pages --add-modules "$(jdeps --print-module-deps app/planethour.jar)" --output jre'
                            }
                        }
                        stage('Pack Debian Package') {
                            steps {
                                sh 'jpackage --input app --name PlanetHour --description "Planetary hour calculator" --vendor "Sounak Choudhury" --copyright "Copyright (C) 2013-2026 Sounak Choudhury" --app-version "$APP_VERSION" --main-jar planethour.jar --runtime-image jre --type deb --license-file app/LICENSE.txt --icon extras/PlanetHour.png --linux-app-category Utility --linux-app-release release --linux-menu-group Utility --linux-shortcut'
                            }
                        }
                        stage('Export DEB') {
                            steps {
                                archiveArtifacts artifacts: '*.deb', followSymlinks: false
                            }
                        }
                    }
                }
                stage('Mac OS') {
                    agent { label 'mac' }
                    stages {
                        stage('MacOS: Creating minimal JRE') {
                            steps {
                                cleanWs()
                                unstash 'app'
                                unstash 'icons'
                                sh 'jlink --compress=zip-6 --strip-debug --no-header-files --no-man-pages --add-modules "$(jdeps --print-module-deps app/planethour.jar)" --output jre'
                            }
                        }
                        stage('Pack DMG Image') {
                            steps {
                                sh 'jpackage --input app --name PlanetHour --description "Planetary hour calculator" --vendor "Sounak Choudhury" --copyright "Copyright (C) 2013-2026 Sounak Choudhury" --app-version "$APP_VERSION" --main-jar planethour.jar --runtime-image jre --type dmg --license-file app/LICENSE.txt --icon extras/PlanetHour.icns --mac-app-category utilities --mac-package-name PlanetHour'
                            }
                        }
                        stage('Export DMG Image') {
                            steps {
                                archiveArtifacts artifacts: '*.dmg', followSymlinks: false
                            }
                        }
                    }
                }
            }
        }
    }
}
