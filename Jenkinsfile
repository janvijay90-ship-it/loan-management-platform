pipeline {
    agent any

    environment {
        // Tag build uniquely using Jenkins build number and short Git commit hash
        BUILD_TAG = "${env.BUILD_NUMBER}-${env.GIT_COMMIT ? env.GIT_COMMIT.take(7) : 'local'}"
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {
        stage('1. Parallel Unit Testing') {
            parallel {
                stage('Test: API Gateway') {
                    steps {
                        dir('api-gateway') {
                            sh 'mvn test'
                        }
                    }
                }
                stage('Test: Loan Service') {
                    steps {
                        dir('loan-service') {
                            sh 'mvn test'
                        }
                    }
                }
                stage('Test: Risk Service') {
                    steps {
                        dir('risk-service') {
                            sh 'mvn test'
                        }
                    }
                }
                stage('Test: Disbursement Service') {
                    steps {
                        dir('disbursement-service') {
                            sh 'mvn test'
                        }
                    }
                }
                stage('Test: Notification Service') {
                    steps {
                        dir('notification-service') {
                            sh 'mvn test'
                        }
                    }
                }
            }
        }

        stage('2. Build Microservice Binaries') {
            steps {
                echo "Packaging all 5 Spring Boot JARs via root Maven..."
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('3. Build Docker Images') {
            steps {
                echo "Building Docker container images via Docker Compose..."
                sh 'docker compose build loan-service risk-service disbursement-service notification-service api-gateway'
            }
        }

        stage('4. Integration Smoke Testing against Platform Stack') {
            environment {
                GATEWAY_HOST = 'api-gateway'
            }
            steps {
                echo "Executing automated end-to-end smoke test against the platform..."
                sh '''
                    if [ -f scripts/test_e2e.sh ]; then
                        chmod +x scripts/test_e2e.sh || true
                        bash scripts/test_e2e.sh
                    fi
                '''
            }
        }

        stage('5. Deliver / Publish Images') {
            when {
                branch 'main'
            }
            steps {
                echo "Images successfully verified. Ready for staging/production deployment."
                // Optional: Push to Docker Registry / AWS ECR:
                // withCredentials([usernamePassword(credentialsId: 'dockerhub-credentials', usernameVariable: 'USER', passwordVariable: 'PASS')]) {
                //     sh 'echo $PASS | docker login -u $USER --password-stdin'
                //     sh 'docker compose push'
                // }
            }
        }
    }

    post {
        success {
            echo "CI/CD Pipeline Completed Successfully! All microservices tested, built, and verified."
        }
        failure {
            echo "CI/CD Pipeline Failed! Please check the failed stage log above."
        }
    }
}
