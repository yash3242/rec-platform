pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Quality Gate / Test execution') {
            steps {
                dir('backend') {
                    bat 'mvn clean test'
                }
            }
            post {
                always {
                    junit testResults: 'backend/target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Docker Build & Verification') {
            steps {
                powershell '''
                    Write-Host "Building and tagging local Docker images..."
                    docker compose -f docker-compose.yml up -d --build

                    docker images --filter "reference=rec-backend*" --filter "reference=rec-frontend*" --filter "reference=postgres*"
                '''
            }
        }

        stage('Health Check verification') {
            steps {
                powershell '''
                    $backend = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/roles" -UseBasicParsing
                    $frontend = Invoke-WebRequest -Uri "http://localhost:5173" -UseBasicParsing

                    if ($backend.StatusCode -eq 200 -and $frontend.StatusCode -eq 200) {
                        Write-Host "Health checks passed."
                    } else {
                        throw "Health check failed: backend=${backend.StatusCode}, frontend=${frontend.StatusCode}"
                    }
                '''
            }
        }
    }
}
