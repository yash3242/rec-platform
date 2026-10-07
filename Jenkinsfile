pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'JDK17'
    }

    parameters {
        string(name: 'DEPLOY_ENV', defaultValue: 'local-staging', description: 'Target deployment environment')
        string(name: 'SERVER_PORT', defaultValue: '8080', description: 'Spring Boot embedded Tomcat port')
    }

    environment {
        DEPLOY_ENV = "${params.DEPLOY_ENV}"
        SERVER_PORT = "${params.SERVER_PORT}"
        BACKEND_DIR = "${WORKSPACE}\\backend"
        FRONTEND_DIR = "${WORKSPACE}\\frontend"
    }

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    bat 'npm ci'
                    bat 'npm run build'
                }
            }
        }

        stage('Build Backend') {
            steps {
                dir('backend') {
                    bat 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Test (E2E / Selenium)') {
            steps {
                script {
                    echo "Starting Vite dev server for E2E tests"
                    bat '''
                        cd frontend
                        start "vite-dev" /b cmd /c "npm run dev -- --port 5173 > vite-e2e.log 2>&1"
                        ping -n 20 127.0.0.1 > nul
                    '''
                    dir('backend') {
                        bat 'mvn test'
                    }
                    bat '''
                        for /f "tokens=5" %%a in ('netstat -aon ^| findstr :5173 ^| findstr LISTENING') do taskkill /pid %%a /f
                    '''
                }
            }
        }

        stage('Run Selenium Tests') {
            steps {
                dir('backend') {
                    bat 'mvn test'
                }
            }
            post {
                always {
                    junit 'backend/target/surefire-reports/*.xml'
                    archiveArtifacts artifacts: 'backend/target/screenshots/**/*', allowEmptyArchive: true
                }
            }
        }

        stage('Archive Artifacts') {
            steps {
                archiveArtifacts artifacts: 'backend/target/*.jar,frontend/dist/**/*', fingerprint: true, allowEmptyArchive: false
            }
        }

        stage('Deploy') {
            steps {
                script {
                    echo "Deploying to ${DEPLOY_ENV} on port ${SERVER_PORT}"
                    powershell '''
                        $port = [int]$env:SERVER_PORT
                        $connections = Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue
                        if ($connections) {
                            $connections | Select-Object -ExpandProperty OwningProcess | ForEach-Object {
                                Write-Host "Stopping process $_ on port $port"
                                Stop-Process -Id $_ -Force
                            }
                        }
                    '''
                    bat '''
                        set JAVA_HOME=C:\\Program Files\\Eclipse Adoptium\\jdk-17.0.20.101-hotspot
                        set "PATH=%JAVA_HOME%\\bin;%PATH%"
                        for %%f in (backend\\target\\*.jar) do (
                            start "" /b java -jar %%f --server.port=%SERVER_PORT%
                            goto :startDone
                        )
                        :startDone
                    '''
                }
            }
        }
    }

    post {
        always {
            junit '**/target/surefire-reports/*.xml'
            archiveArtifacts artifacts: '**/target/screenshots/*.png', allowEmptyArchive: true
            cleanWs()
        }
        success {
            echo "SUCCESS: Built and deployed REC Platform to ${DEPLOY_ENV} on port ${SERVER_PORT}"
        }
        failure {
            echo "FAILURE: Build, test, or deployment failed. Check Surefire reports and screenshots."
        }
    }
}
