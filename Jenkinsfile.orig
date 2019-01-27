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
        
        stage('Java Test') {
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
            }
        }

        // Generally: How (technically) do I deploy?
        // Depends on different boundary conditions?!

        stage('Deploy UAT') {
            steps {
                echo 'Deploying User Acceptance Testing.'
                echo 'Das darf ich alleine.'
            }
        }

        stage("Deploy Production") {
            steps {
                echo 'Deploying auf Produktion.'
                echo 'Das darf ich NICHT alleine.'

                script {
                    // do not forget to set a time!
                    // timeout(time: 1, unit: 'MINUTES') {...}
                    def result = input(id: 'Proceed1', message: 'Deploy to production?', parameters: [[$class: 'BooleanParameterDefinition', defaultValue: false, description: '', name: 'Please confirm you agree with this']])
                    echo 'result: ' + result
                }    
            }  
        }  
        // Perhaps we should clean up the images and other stuff?
    }
}