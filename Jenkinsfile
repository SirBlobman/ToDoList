pipeline {
    agent any

    options {
        githubProjectProperty(projectUrlStr: "https://github.com/SirBlobman/ToDoList")
    }

    environment {
        DISCORD_URL = credentials('PUBLIC_DISCORD_WEBHOOK')
    }

    triggers {
        githubPush()
    }

    tools {
        jdk "JDK 17"
        maven "Default"
    }

    stages {
        stage ("Gradle: Build") {
            steps {
                withGradle {
                    sh("./gradlew clean build --refresh-dependencies --no-daemon")
                }
            }
        }
    }

    post {
        success {
            archiveArtifacts artifacts: 'build/libs/ToDoList-*.jar', fingerprint: true
        }

        always {
            script {
                discordSend webhookURL: DISCORD_URL,
                        title: "${env.JOB_NAME}",
                        link: "${env.BUILD_URL}",
                        result: currentBuild.currentResult,
                        description: "**Build:** ${env.BUILD_NUMBER}\n**Status:** ${currentBuild.currentResult}",
                        enableArtifactsList: false,
                        showChangeset: true
            }
        }
    }
}