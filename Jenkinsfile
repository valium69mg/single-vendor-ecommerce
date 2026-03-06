pipeline {
    agent any

    environment {
        SONARQUBE = 'SonarQube'
    }

    triggers {
        pollSCM('H/5 * * * *')
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'dev',
                    url: 'https://github.com/valium69mg/single-vendor-ecommerce'
            }
        }

        stage('Build') {
            steps {
                echo 'Building the project...'
                sh './mvnw clean compile'
            }
        }

        stage('Unit Tests') {
            steps {
                echo 'Running unit tests...'
                sh './mvnw test'
            }
            post {
                always {
                    // Publish JUnit results
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                echo 'Running SonarQube analysis...'
                withSonarQubeEnv('SonarQube') {
                    sh './mvnw sonar:sonar -Dsonar.projectKey=single-vendor-ecommerce'
                }
            }
        }
    }

    post {
        success {
            echo '✅ Build, tests, and SonarQube analysis succeeded!'
        }
        failure {
            echo '❌ Build, tests, or SonarQube analysis failed!'
        }
    }
}