#!/usr/bin/env groovy

pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {

                echo "Checkout sources"
                //checkout scm // not working with "Build now"? -> he checkout step will checkout code from source control; scm is a special variable which instructs the checkout step to clone the specific revision which triggered this Pipeline run.

                git "https://github.com/edigonzales/ilivalidator-web-service.git/"
            }
        }
        
        stage('Java Build') {
            steps {
                echo "Build fatjar"
                //sh "./gradlew clean build -x test"

                archiveArtifacts artifacts: "build/libs/*.jar", onlyIfSuccessful: true, fingerprint: true
                // do not forget javadocs: e.g. archiveArtifacts(artifacts: 'target/Nadia*javadoc.jar', fingerprint: true)
            }
        }
        
        /*
        stage('Test') {
            steps {
                echo 'Perform tests.'
                sh "./gradlew clean test"

                publishHTML target: [
                    reportName : 'Gradle Tests',
                    reportDir:   'build/reports/tests/test', // copies all subfolder in this folder
                    reportFiles: 'index.html',
                    keepAll:     true,
                    alwaysLinkToLastBuild: true,
                    allowMissing: false
                ]
            }
        }
        */

        stage('Docker Build') {
            steps {
                echo "Build docker image."
                sh "./gradlew clean build buildDocker -x test"
            }
        }

        stage('Docker Publish') {
            steps {
                echo "Publish docker image to hub.docker.com."
                script {
                    // Get version of build from build.gradle.
                    // Best approach I could figure out.
                    def projectVersion = sh script: "./gradlew getVersion -q", returnStdout: true
                    println projectVersion

                    docker.withRegistry('https://registry.hub.docker.com', 'docker-hub-credentials') {
                        docker.image('sogis/ilivalidator-web-service:latest').push(projectVersion.trim())
                        docker.image('sogis/ilivalidator-web-service:latest').push('latest')
                    }
                    
                }
                
                //sh "./gradlew clean build buildDocker -x test -s"
                //sh "docker images"
                //archiveArtifacts artifacts: "build/libs/*.jar", onlyIfSuccessful: true, fingerprint: true
            }
        }

        stage('Deploy UAT') {
            steps {
                echo 'Deploying User Acceptance Testing.'
            }
        }

        stage('Deploy Production') {
            steps {
                echo 'Deploying auf Produktion.'
            }
        }

        // Perhaps we should clean the images and other stuff?
    }
}