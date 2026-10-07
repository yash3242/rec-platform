pipeline {
    agent any

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Selenium Quality Gate') {
            steps {
                dir('backend') {
                    bat 'mvn test'
                }
            }
            post {
                always {
                    junit testResults: 'backend/target/surefire-reports/*.xml', allowEmptyResults: true
                }
            }
        }

        stage('Verify Running Stack') {
            steps {
                powershell '''
                    $res1 = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/roles" -UseBasicParsing
                    $res2 = Invoke-WebRequest -Uri "http://localhost:5173" -UseBasicParsing
                    if ($res1.StatusCode -eq 200 -and $res2.StatusCode -eq 200) {
                        Write-Host "Services are up and healthy!"
                    }
                '''
            }
        }
    }
}