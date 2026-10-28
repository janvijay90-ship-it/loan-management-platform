# Loan Management Platform — Setup & Operations Guide

## Quick Start (One Command)

```powershell
cd d:\loan-management-system\loan-management-platform
docker compose up --build -d
```

This starts **19 containers** covering the full platform:

| Container | Port | Purpose |
|---|---|---|
| loan-portal | 80 | React-like Web UI |
| api-gateway | 8080 | JWT security + routing |
| loan-service | 8081 | Loan CRUD + Outbox |
| risk-service | 8082 | CIBIL evaluation |
| disbursement-service | 8083 | Bank payout simulation |
| notification-service | 8084 | Kafka event notifications |
| jenkins-ci | 8085 | CI/CD pipeline |
| service-registry | 8761 | Eureka service discovery |
| config-server | 8888 | Centralized config |
| postgres-loan | 5432 | Loan DB |
| postgres-risk | 5433 | Risk DB |
| postgres-disbursement | 5434 | Disbursement DB |
| redis-cache | 6379 | Rate limiting + CIBIL cache |
| kafka-broker | 9092 | Event streaming |
| zipkin-tracing | 9411 | Distributed tracing |
| prometheus-metrics | 9090 | Metrics scraping |
| grafana-dashboard | 3000 | Metrics + Logs dashboards |
| loki-logs | 3100 | Log aggregation |
| promtail-collector | — | Docker log shipper |

---

## GitHub Push (Choice A)

### Step 1 — Create the repo on GitHub
1. Go to https://github.com/new
2. Name: `loan-management-platform`
3. **Leave it empty** (no README, no .gitignore)
4. Click **Create repository**

### Step 2 — Push from your machine
```powershell
cd d:\loan-management-system\loan-management-platform
git remote add origin https://github.com/YOUR_USERNAME/loan-management-platform.git
git push -u origin main
```

### Step 3 — Update Jenkins job with your repo URL
Edit `jenkins/jenkins.yaml` and `jenkins/jobs/loan-platform-pipeline.xml`,
replacing `YOUR_USERNAME` with your actual GitHub username.

---

## Jenkins CI/CD (Choice C)

Jenkins runs at **http://localhost:8085/jenkins**

### Default Credentials
- **Username:** `admin`
- **Password:** `admin123`  ← change after first login!

### What's pre-configured
- JDK 17 (`/opt/java/openjdk`)
- Maven 3.9.6 (`/opt/maven`)
- All required plugins (Pipeline, Git, Docker, Blue Ocean, JCasC)
- Pipeline job `loan-management-platform` pointing to your GitHub repo

### Triggering a build
- Automatic: SCM polling every 5 minutes (set in JCasC)
- Manual: Open http://localhost:8085/jenkins → click **Build Now**
- Webhook (recommended for production): Add `http://YOUR_SERVER:8085/jenkins/github-webhook/`
  in your GitHub repo Settings → Webhooks

---

## Observability Stack

| Tool | URL | Credentials |
|---|---|---|
| Grafana | http://localhost:3000 | admin / admin |
| Prometheus | http://localhost:9090 | — |
| Zipkin | http://localhost:9411 | — |
| Eureka | http://localhost:8761 | — |

---

## Eclipse Import

1. **File → Import → Maven → Existing Maven Projects**
2. Root directory: `D:\loan-management-system\loan-management-platform`
3. Eclipse will detect all 7 modules (api-gateway, loan-service, risk-service,
   disbursement-service, notification-service, service-registry, config-server)
4. Click **Finish**, then `Alt+F5` → **Update Maven Projects**

---

## CI/CD Pipeline Stages

```
1. Parallel Unit Testing   → tests all 5 services simultaneously
2. Build Microservice JARs → mvn clean package -DskipTests
3. Build Docker Images     → docker compose build
4. Integration Smoke Test  → spin up full stack, run test_e2e.sh
5. Deliver / Publish       → (main branch only) ready for ECR/DockerHub push
```

---

## AWS Deployment

```bash
# Run the setup script on your EC2 instance (Ubuntu 22.04)
chmod +x scripts/aws_ec2_setup.sh
./scripts/aws_ec2_setup.sh
```

Installs: Docker, Docker Compose, clones repo, starts the full platform.
