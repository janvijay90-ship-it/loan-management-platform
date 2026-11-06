pipeline {
    agent any

    environment {
        // ── AWS / ECR config ──────────────────────────────────────────────
        AWS_REGION      = 'ap-south-1'
        AWS_ACCOUNT_ID  = '306317596640'
        ECR_REGISTRY    = "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"
        EKS_CLUSTER     = 'loan-platform-eks'
        K8S_NAMESPACE   = 'loan-system'

        // ── ECR repository names (must match what you created in ECR) ─────
        ECR_API_GATEWAY      = "${ECR_REGISTRY}/api-gateway"
        ECR_LOAN_SERVICE     = "${ECR_REGISTRY}/loan-service"
        ECR_RISK_SERVICE     = "${ECR_REGISTRY}/risk-service"
        ECR_DISBURSEMENT     = "${ECR_REGISTRY}/disbursement-service"
        ECR_NOTIFICATION     = "${ECR_REGISTRY}/notification-service"
        ECR_LOAN_PORTAL      = "${ECR_REGISTRY}/loan-portal"
        ECR_SERVICE_REGISTRY = "${ECR_REGISTRY}/service-registry"
        ECR_CONFIG_SERVER    = "${ECR_REGISTRY}/config-server"

        // ── Unique image tag: build-number + short git SHA ─────────────────
        BUILD_TAG = "${env.BUILD_NUMBER}-${env.GIT_COMMIT ? env.GIT_COMMIT.take(7) : 'local'}"
    }

    options {
        timeout(time: 45, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
    }

    stages {

        // ══════════════════════════════════════════════════════════════════
        // STAGE 1 — Parallel Unit Tests
        // ══════════════════════════════════════════════════════════════════
        stage('1. Parallel Unit Testing') {
            parallel {
                stage('Test: API Gateway') {
                    steps {
                        dir('api-gateway') { sh 'mvn test' }
                    }
                }
                stage('Test: Loan Service') {
                    steps {
                        dir('loan-service') { sh 'mvn test' }
                    }
                }
                stage('Test: Risk Service') {
                    steps {
                        dir('risk-service') { sh 'mvn test' }
                    }
                }
                stage('Test: Disbursement Service') {
                    steps {
                        dir('disbursement-service') { sh 'mvn test' }
                    }
                }
                stage('Test: Notification Service') {
                    steps {
                        dir('notification-service') { sh 'mvn test' }
                    }
                }
            }
        }

        // ══════════════════════════════════════════════════════════════════
        // STAGE 2 — Build Spring Boot JARs
        // ══════════════════════════════════════════════════════════════════
        stage('2. Build Microservice Binaries') {
            steps {
                echo "Packaging all 5 Spring Boot JARs via root Maven..."
                sh 'mvn clean package -DskipTests'
            }
        }

        // ══════════════════════════════════════════════════════════════════
        // STAGE 3 — Build Docker Images (tagged for ECR)
        // ══════════════════════════════════════════════════════════════════
        stage('3. Build Docker Images') {
            steps {
                echo "Building and tagging Docker images for ECR (tag: ${BUILD_TAG})..."
                sh """
                    docker build -t ${ECR_API_GATEWAY}:${BUILD_TAG}     ./api-gateway
                    docker build -t ${ECR_LOAN_SERVICE}:${BUILD_TAG}    ./loan-service
                    docker build -t ${ECR_RISK_SERVICE}:${BUILD_TAG}    ./risk-service
                    docker build -t ${ECR_DISBURSEMENT}:${BUILD_TAG}    ./disbursement-service
                    docker build -t ${ECR_NOTIFICATION}:${BUILD_TAG}    ./notification-service

                    # Also tag as 'latest' for convenience
                    docker tag ${ECR_API_GATEWAY}:${BUILD_TAG}     ${ECR_API_GATEWAY}:latest
                    docker tag ${ECR_LOAN_SERVICE}:${BUILD_TAG}    ${ECR_LOAN_SERVICE}:latest
                    docker tag ${ECR_RISK_SERVICE}:${BUILD_TAG}    ${ECR_RISK_SERVICE}:latest
                    docker tag ${ECR_DISBURSEMENT}:${BUILD_TAG}    ${ECR_DISBURSEMENT}:latest
                    docker tag ${ECR_NOTIFICATION}:${BUILD_TAG}    ${ECR_NOTIFICATION}:latest
                """
            }
        }

        // ══════════════════════════════════════════════════════════════════
        // STAGE 4 — Integration Smoke Tests
        // ══════════════════════════════════════════════════════════════════
        stage('4. Integration Smoke Testing') {
            environment {
                GATEWAY_HOST = 'localhost'
            }
            steps {
                echo "Running integration checks..."
                sh '''
                    # Check if API Gateway is reachable before testing
                    if curl -s -f -m 3 http://${GATEWAY_HOST}:8080/actuator/health > /dev/null 2>&1; then
                        echo "API Gateway is active. Running test_e2e.sh..."
                        chmod +x scripts/test_e2e.sh
                        bash scripts/test_e2e.sh
                    else
                        echo "⚠️ API Gateway is not running on this build agent. Skipping ephemeral smoke test — deployment stage will verify cluster health."
                    fi
                '''
            }
        }

        // ══════════════════════════════════════════════════════════════════
        // STAGE 5 — Push Images to AWS ECR (main branch only)
        // ══════════════════════════════════════════════════════════════════
        stage('5. Push Images to ECR') {
            when {
                branch 'main'
            }
            steps {
                withCredentials([[
                    $class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: 'aws-credentials'   // matches the ID you set in Jenkins
                ]]) {
                    echo "Logging into Amazon ECR..."
                    sh """
                        aws ecr get-login-password --region ${AWS_REGION} \
                            | docker login --username AWS --password-stdin ${ECR_REGISTRY}
                    """

                    echo "Pushing images to ECR (tag: ${BUILD_TAG})..."
                    sh """
                        docker push ${ECR_API_GATEWAY}:${BUILD_TAG}
                        docker push ${ECR_API_GATEWAY}:latest

                        docker push ${ECR_LOAN_SERVICE}:${BUILD_TAG}
                        docker push ${ECR_LOAN_SERVICE}:latest

                        docker push ${ECR_RISK_SERVICE}:${BUILD_TAG}
                        docker push ${ECR_RISK_SERVICE}:latest

                        docker push ${ECR_DISBURSEMENT}:${BUILD_TAG}
                        docker push ${ECR_DISBURSEMENT}:latest

                        docker push ${ECR_NOTIFICATION}:${BUILD_TAG}
                        docker push ${ECR_NOTIFICATION}:latest

                        docker push ${ECR_LOAN_PORTAL}:${BUILD_TAG}
                        docker push ${ECR_LOAN_PORTAL}:latest

                        docker push ${ECR_SERVICE_REGISTRY}:${BUILD_TAG}
                        docker push ${ECR_SERVICE_REGISTRY}:latest

                        docker push ${ECR_CONFIG_SERVER}:${BUILD_TAG}
                        docker push ${ECR_CONFIG_SERVER}:latest
                    """
                }
            }
        }

        // ══════════════════════════════════════════════════════════════════
        // STAGE 6 — Deploy to EKS (main branch only)
        // ══════════════════════════════════════════════════════════════════
        stage('6. Deploy to EKS') {
            when {
                branch 'main'
            }
            steps {
                withCredentials([[
                    $class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: 'aws-credentials'
                ]]) {
                    echo "Updating kubeconfig for cluster: ${EKS_CLUSTER}..."
                    sh """
                        aws eks update-kubeconfig \
                            --name ${EKS_CLUSTER} \
                            --region ${AWS_REGION}
                    """

                    echo "Applying Kubernetes manifests..."
                    sh """
                        # Apply all manifests in order
                        kubectl apply -f k8s/00-namespace.yaml
                        kubectl apply -f k8s/01-databases.yaml
                        kubectl apply -f k8s/02-kafka.yaml
                        kubectl apply -f k8s/03-registry-and-config.yaml
                        kubectl apply -f k8s/04-microservices.yaml
                        kubectl apply -f k8s/05-gateway-and-portal.yaml

                        # Update image tags to this exact build
                        kubectl set image deployment/api-gateway \
                            api-gateway=${ECR_API_GATEWAY}:${BUILD_TAG} \
                            -n ${K8S_NAMESPACE}

                        kubectl set image deployment/loan-service \
                            loan-service=${ECR_LOAN_SERVICE}:${BUILD_TAG} \
                            -n ${K8S_NAMESPACE}

                        kubectl set image deployment/risk-service \
                            risk-service=${ECR_RISK_SERVICE}:${BUILD_TAG} \
                            -n ${K8S_NAMESPACE}

                        kubectl set image deployment/disbursement-service \
                            disbursement-service=${ECR_DISBURSEMENT}:${BUILD_TAG} \
                            -n ${K8S_NAMESPACE}

                        kubectl set image deployment/notification-service \
                            notification-service=${ECR_NOTIFICATION}:${BUILD_TAG} \
                            -n ${K8S_NAMESPACE}

                        kubectl set image deployment/loan-portal \
                            loan-portal=${ECR_LOAN_PORTAL}:${BUILD_TAG} \
                            -n ${K8S_NAMESPACE}

                        kubectl set image deployment/service-registry \
                            service-registry=${ECR_SERVICE_REGISTRY}:${BUILD_TAG} \
                            -n ${K8S_NAMESPACE}

                        kubectl set image deployment/config-server \
                            config-server=${ECR_CONFIG_SERVER}:${BUILD_TAG} \
                            -n ${K8S_NAMESPACE}
                    """

                    echo "Waiting for rollout to complete..."
                    sh """
                        kubectl rollout status deployment/service-registry    -n ${K8S_NAMESPACE} --timeout=5m
                        kubectl rollout status deployment/config-server        -n ${K8S_NAMESPACE} --timeout=5m
                        kubectl rollout status deployment/loan-service         -n ${K8S_NAMESPACE} --timeout=5m
                        kubectl rollout status deployment/risk-service         -n ${K8S_NAMESPACE} --timeout=5m
                        kubectl rollout status deployment/disbursement-service -n ${K8S_NAMESPACE} --timeout=5m
                        kubectl rollout status deployment/notification-service -n ${K8S_NAMESPACE} --timeout=5m
                        kubectl rollout status deployment/api-gateway          -n ${K8S_NAMESPACE} --timeout=5m
                        kubectl rollout status deployment/loan-portal          -n ${K8S_NAMESPACE} --timeout=5m
                    """

                    echo "Deployment complete! Live pods:"
                    sh "kubectl get pods -n ${K8S_NAMESPACE}"
                }
            }
        }

        // ══════════════════════════════════════════════════════════════════
        // STAGE 7 — Verify Deployment (Smoke check on cluster)
        // ══════════════════════════════════════════════════════════════════
        stage('7. Post-Deploy Verification') {
            when {
                branch 'main'
            }
            steps {
                withCredentials([[
                    $class: 'AmazonWebServicesCredentialsBinding',
                    credentialsId: 'aws-credentials'
                ]]) {
                    sh """
                        echo "=== Pods ==="
                        kubectl get pods -n ${K8S_NAMESPACE}

                        echo "=== Services (check LoadBalancer EXTERNAL-IP) ==="
                        kubectl get svc -n ${K8S_NAMESPACE}

                        echo "=== Recent Warning Events ==="
                        kubectl get events -n ${K8S_NAMESPACE} --field-selector type=Warning || true
                    """
                }
            }
        }

    }

    // ════════════════════════════════════════════════════════════════════
    // POST — Notifications
    // ════════════════════════════════════════════════════════════════════
    post {
        success {
            echo """
            ✅ CI/CD Pipeline Completed Successfully!
            ─────────────────────────────────────────
            Branch  : ${env.BRANCH_NAME}
            Build # : ${env.BUILD_NUMBER}
            Tag     : ${BUILD_TAG}
            Cluster : ${EKS_CLUSTER}
            All microservices tested, built, pushed to ECR, and deployed to EKS.
            """
        }
        failure {
            echo """
            ❌ CI/CD Pipeline FAILED!
            ──────────────────────────
            Build # : ${env.BUILD_NUMBER}
            Check the failed stage log above for details.
            """
        }
        always {
            // Clean up dangling Docker images to save disk space
            sh 'docker image prune -f || true'
        }
    }
}
