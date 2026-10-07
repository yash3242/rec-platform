pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = "local"
        IMAGE_TAG = "${env.BUILD_NUMBER}"
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

        stage('Build & Prepare Containers') {
            steps {
                echo "Building and launching container stack for testing..."
                bat "docker compose down"
                bat "docker compose up -d --build"
                // Wait for Spring Boot and Nginx to fully initialize
                powershell "Start-Sleep -Seconds 15"
            }
        }

        stage('Selenium Quality Gate') {
            steps {
                dir('backend') {
                    echo "Executing Selenium E2E automated suite against running stack..."
                    bat 'mvn test'
                }
            }
            post {
                always {
                    junit testResults: 'backend/target/surefire-reports/*.xml', allowEmptyResults: true
                    archiveArtifacts artifacts: 'backend/target/screenshots/*.png', allowEmptyArchive: true
                }
            }
        }

        stage('Tag Release Images') {
            steps {
                echo "Tagging stable images with build number #${IMAGE_TAG} and latest..."
                bat "docker tag rec-platform-backend:latest ${BACKEND_IMAGE}:${IMAGE_TAG}"
                bat "docker tag rec-platform-frontend:latest ${FRONTEND_IMAGE}:${IMAGE_TAG}"
            }
        }

        stage('Post-Deployment Health Probe') {
            steps {
                echo "Verifying application availability..."
                powershell '''
                    $backend = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/roles" -UseBasicParsing -TimeoutSec 15
                    $frontend = Invoke-WebRequest -Uri "http://localhost:5173" -UseBasicParsing -TimeoutSec 15
                    if ($backend.StatusCode -eq 200 -and $frontend.StatusCode -eq 200) {
                        Write-Host "Deployment Health Verified: HTTP 200 on all endpoints." -ForegroundColor Green
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
            echo "Pipeline run #${env.BUILD_NUMBER} failed."
        }
        success {
            echo "REC Platform CD Pipeline completed successfully for build #${env.BUILD_NUMBER}!"
        }
    }
}