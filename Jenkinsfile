pipeline {
    agent any

    environment {
        DOCKER_REGISTRY = "local" // Change to your Docker Hub username if pushing remotely
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

        stage('Registry Publish (Optional / Local)') {
            steps {
                script {
                    echo "Tagging complete for version: "
                    // If DOCKER_REGISTRY != 'local', tag and push to remote registry
                    if (env.DOCKER_REGISTRY != "local") {
                        withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', usernameVariable: 'DH_USER', passwordVariable: 'DH_PASS')]) {
                            bat "docker login -u %DH_USER% -p %DH_PASS%"
                            bat "docker tag : %DH_USER%/:"
                            bat "docker tag : %DH_USER%/:"
                            bat "docker push %DH_USER%/:"
                            bat "docker push %DH_USER%/:"
                        }
                    } else {
                        echo "Registry mode set to 'local'. Images stored in local Docker daemon."
                    }
                }
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
                    Start-Sleep -Seconds 10
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
