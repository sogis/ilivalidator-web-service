#!/usr/bin/env groovy

pipeline {
    agent any

    stages {
        stage('Prepare') {
            steps {
                echo "Checkout sources"
                git "https://github.com/edigonzales/ilivalidator-web-service.git/"
            }
        }
        
        stage('Java Build') {
            steps {
                sh './gradlew --no-daemon clean classes'
            }
        }

        stage('Test') {
            steps {
                sh './gradlew --no-daemon test'
                publishHTML target: [
                    reportName : 'Gradle Tests',
                    reportDir:   'build/reports/tests/test', // copies all subfolder in this folder
                    reportFiles: 'index.html',
                    keepAll: true,
                    alwaysLinkToLastBuild: true,
                    allowMissing: false
                ]                
            }
        }

        stage('Publish') {
            steps {
                sh './gradlew --no-daemon bootJar'  
                archiveArtifacts artifacts: "build/libs/ilivalidator-web-service-*.jar", onlyIfSuccessful: true, fingerprint: true                              
            }
        }               
    }
    post {
        always {
            deleteDir() 
        }
    }
}