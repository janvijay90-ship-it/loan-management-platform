@echo off
echo ========================================================
echo  Building all 5 Spring Boot Microservices
echo ========================================================

echo 1/5 Building API Gateway...
cd api-gateway && call mvn clean package -DskipTests && cd ..

echo 2/5 Building Loan Service...
cd loan-service && call mvn clean package -DskipTests && cd ..

echo 3/5 Building Risk Service...
cd risk-service && call mvn clean package -DskipTests && cd ..

echo 4/5 Building Disbursement Service...
cd disbursement-service && call mvn clean package -DskipTests && cd ..

echo 5/5 Building Notification Service...
cd notification-service && call mvn clean package -DskipTests && cd ..

echo.
echo ========================================================
echo  All 5 microservices built successfully!
echo  Run: docker compose up --build -d
echo ========================================================
