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
                sh 'docker compose build'
            }
        }

        stage('4. Integration Smoke Testing in Ephemeral Stack') {
            steps {
                echo "Spinning up all 10 containers (Microservices + Postgres + Redis + Kafka)..."
                sh '''
                    # Ensure any existing stopped containers are removed cleanly
                    docker compose down -v || true

                    # Start up the full platform stack in detached mode
                    docker compose up -d

                    # Wait for Spring Boot services and Kafka broker to be fully ready
                    echo "Waiting for services to become healthy..."
                    sleep 35

                    # Execute automated end-to-end smoke test
                    if [ -f scripts/test_e2e.sh ]; then
                        chmod +x scripts/test_e2e.sh
                        ./scripts/test_e2e.sh
                    fi
                '''
            }
            post {
                always {
                    echo "Cleaning up ephemeral test containers..."
                    sh 'docker compose down -v'
                }
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
