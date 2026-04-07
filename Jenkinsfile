pipeline {
    agent any
  
    environment {
        SONARQUBE = 'SonarQube'
        DOCKER_IMAGE = 'carlostranquilinocr98/single-vendor-ecommerce'
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
                sh './mvnw clean package -DskipTests'
            }
        }

        stage('Unit Tests') {
            steps {
                echo 'Running unit tests...'
                sh './mvnw test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        /*
        stage('SonarQube Analysis') {
            steps {
                echo 'Running SonarQube analysis...'
                withSonarQubeEnv('SonarQube') {
                    sh './mvnw sonar:sonar -Dsonar.projectKey=single-vendor-ecommerce'
                }
            }
        }

        stage('Wait for SonarQube Quality Gate') {
            steps {
                echo 'Waiting for SonarQube Quality Gate...'
                timeout(time: 10, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        */
        stage('Build & Push Docker Image') {
            steps {
                echo 'Building Docker image...'
                sh "docker build -t carlostranquilinocr98/single-vendor-ecommerce:latest ."

                echo 'Pushing Docker image to Docker Hub...'
                withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', 
                                                  usernameVariable: 'DOCKER_USER', 
                                                  passwordVariable: 'DOCKER_PASS')]) {
                    sh "echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin"
                    sh "docker push carlostranquilinocr98/single-vendor-ecommerce:latest"
                }
            }
        }
        
        stage('Deploy') {
            steps {
                sshagent(['ubuntu-server-ssh']) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no ubuntu@3.80.104.36 << 'EOF'
                        cd /home/ubuntu/single-vendor-ecommerce
                        docker compose pull
                        docker compose up -d postgres redis backend frontend thumbnail-worker --remove-orphans
                        EOF
                    '''
                }
            }
        }


    }
    
    post {
        success {
            echo '✅ Deploy completed'
        }
        failure {
            echo '❌ Deploy failed failed!'
        }
    }
}
