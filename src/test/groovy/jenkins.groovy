pipeline {
    agent any

    tools {
        // Install the Maven version configured as "M3" and add it to the path.
        maven "Maven_Home"
    }

    stages {
        stage('Compile') {
            steps {
                // Get some code from a GitHub repository
                git branch: 'main', url: 'https://github.com/dondvdlaan/JAVA-SPRING-PG-OPENAPI'


                // To run Maven on a Windows agent, use
                bat "mvn clean compile"
            }
        }
        stage('Test') {
            steps {
                // To run Maven on a Windows agent, use
                bat "mvn test"
            }
        }
        stage('Build') {
            steps {
                // To run Maven on a Windows agent, use
                bat "mvn package"
            }
        }

        stage('Integration test') {
            parallel {
                stage('DECOM') {
                    steps {
                        bat "mvn spring-boot:run"
                    }
                }
                stage('iTEST') {
                    steps {
                        git branch: 'main', url: 'https://github.com/dondvdlaan/JAVA-SPRING-PG-OPENAPI-itest'
                        bat "mvn test"
                    }
                }
            }
        }
    }
    post {
        success {
            // Start
            junit '**/target/surefire-reports/TEST-*.xml'
            archiveArtifacts 'target/*.jar'
        }
        failure {
            echo "Foutje"
        }
    }


}
