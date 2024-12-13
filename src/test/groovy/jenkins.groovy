pipeline {
    agent any

    tools {
        // Maven_Home is configured at Jenkins
        maven "Maven_Home"
    }
    stages {
        stage('Compile') {
            steps {
                // Get Decom application from a GitHub repository
                git branch: 'main', url: 'https://github.com/dondvdlaan/JAVA-SPRING-PG-OPENAPI'
                bat "mvn clean compile"
            }
        }
        stage('Unit Tests') {
            steps {
                // Run unit tests
                bat "mvn test"
            }
        }
        stage('Build') {
            steps {
                bat "mvn package"
            }
        }
        // TODO: alternative is to start / stop Docker container with Decom application to run test with iTest
        stage('Integration test') {
            parallel {
                stage('DECOM') {
                    steps {
                        git branch: 'main', url: 'https://github.com/dondvdlaan/JAVA-SPRING-PG-OPENAPI'
                        bat "mvn spring-boot:run -DskipTests"
                    }
                }
                stage('iTEST') {
                    steps {
                        dir('./itest') {
                            sleep(time: 15, unit: "SECONDS")
                            git branch: 'main', url: 'https://github.com/dondvdlaan/JAVA-SPRING-PG-OPENAPI-itest'
                            bat "mvn clean test"
                            echo "iTest finished"
                        }
                    }
                }
            }
        }
    }
    post {
        success {
            junit '**/target/surefire-reports/TEST-*.xml'
            archiveArtifacts 'target/*.jar'
        }
        failure {
            echo "Failure occured"
        }
    }
}
