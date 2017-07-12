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
        
        stage('Build') {
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

        stage('Publish image') {
            def image = docker.build("ilivalidator-web-service:0.0.8-62aad82")
            //steps {
                //echo "Publish docker image to hub.docker.com"
                //sh "./gradlew clean build buildDocker -x test -s"
                //sh "docker images"
                //archiveArtifacts artifacts: "build/libs/*.jar", onlyIfSuccessful: true, fingerprint: true
            //}
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
    }
}