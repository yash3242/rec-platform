pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = "local"
        IMAGE_TAG = ""
        BACKEND_IMAGE = "rec-backend"
        FRONTEND_IMAGE = "rec-frontend"
    }

    stages {
        stage('Checkout') {
            steps {
                echo "Checking out REC Platform source code..."
                checkout scm
            }
        }

        stage('Backend Build & Selenium Quality Gate') {
            steps {
                dir('backend') {
                    echo "Running Maven build and Selenium E2E automated suite..."
                    bat 'mvn clean test'
                }
            }
            post {
                always {
                    junit testResults: 'backend/target/surefire-reports/*.xml', allowEmptyResults: true
                    archiveArtifacts artifacts: 'backend/target/screenshots/*.png', allowEmptyArchive: true
                }
            }
        }

        stage('Build & Tag Docker Images') {
            steps {
                echo "Building versioned Docker images: Tag # and latest..."
                bat "docker build -t : -t :latest -f backend/Dockerfile backend"
                bat "docker build -t : -t :latest -f frontend/Dockerfile frontend"
            }
        }

        stage('Continuous Deployment (Docker Stack)') {
            steps {
                echo "Deploying updated containerized stack via docker compose..."
                bat 'docker compose down'
                bat 'docker compose up -d'
            }
        }

        stage('Post-Deployment Health Probe') {
            steps {
                echo "Probing container stack health..."
                powershell '''
                    Start-Sleep -Seconds 12
                     = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/roles" -UseBasicParsing -TimeoutSec 15
                     = Invoke-WebRequest -Uri "http://localhost:5173" -UseBasicParsing -TimeoutSec 15
                    if (.StatusCode -eq 200 -and .StatusCode -eq 200) {
                        Write-Host "Deployment Verified! Backend and Frontend both returned HTTP 200." -ForegroundColor Green
                    } else {
                        Write-Error "Health check failed."
                        exit 1
                    }
                '''
            }
        }
    }

    post {
        failure {
            echo "Pipeline failed! Retaining previous deployment state."
        }
        success {
            echo "REC Platform CD Pipeline executed successfully for build #!"
        }
    }
}
