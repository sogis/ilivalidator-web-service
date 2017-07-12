#!/usr/bin/env groovy

pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {

                echo "Checkout sources"
                //checkout scm // not working with "Build now"? -> he checkout step will checkout code from source control; scm is a special variable which instructs the checkout step to clone the specific revision which triggered this Pipeline run.

                git "https://github.com/edigonzales/ilivalidator-web-service.git/"

                //echo "Construyendo el proyecto con Gradle Wrapper"
                //sh './gradlew build -x test'

                //archive 'build/libs/*.jar'
            }
        }
        stage('Build') {
            steps {
                echo "Build binary/jar."
                sh "./gradlew build -x test"

                archiveArtifacts artifacts: "build/libs/*.jar", onlyIfSuccessful: true, fingerprint: true
                // do not forget javadocs: e.g. archiveArtifacts(artifacts: 'target/Nadia*javadoc.jar', fingerprint: true)
            }
        }
        
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

        stage('Publish image') {
            steps {
                echo "Publish docker image to hub.docker.com"
                //sh "./gradlew build -x test"

                //archiveArtifacts artifacts: "build/libs/*.jar", onlyIfSuccessful: true, fingerprint: true
            }
        }

        stage('Deploy') {
            steps {
                echo 'Deploying macht Spass.'
            }
        }
    }
}
