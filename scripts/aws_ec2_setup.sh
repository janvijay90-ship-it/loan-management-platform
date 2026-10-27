#!/usr/bin/env bash
# ==============================================================================
# AWS EC2 Bootstrap & Deployment Script for Loan Management Platform
# OS Target: Ubuntu 22.04 / 24.04 LTS
# ==============================================================================
set -e

echo "=== 1. Updating System Packages ==="
sudo apt-get update -y
sudo apt-get upgrade -y

echo "=== 2. Installing Prerequisites & Docker ==="
sudo apt-get install -y ca-certificates curl gnupg lsb-release git htop

# Install official Docker
sudo install -m 0755 -d /etc/apt/keyrings
sudo curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
sudo chmod a+r /etc/apt/keyrings/docker.asc

echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

sudo apt-get update -y
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Enable and start Docker service
sudo systemctl enable docker
sudo systemctl start docker

# Add ubuntu user to docker group (avoids needing sudo for docker commands)
sudo usermod -aG docker ubuntu

echo "=== 3. Configuring 4GB Swap Space (Recommended for JVM & Kafka stability) ==="
if [ ! -f /swapfile ]; then
    sudo fallocate -l 4G /swapfile
    sudo chmod 600 /swapfile
    sudo mkswap /swapfile
    sudo swapon /swapfile
    echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab
    echo "Swap created successfully."
fi

echo "=== 4. Docker & Compose Versions ==="
docker --version
docker compose version

echo "=============================================================================="
echo "Setup Complete! Next steps:"
echo "1. Run: git clone <YOUR_GIT_REPO_URL>"
echo "2. cd loan-management-platform"
echo "3. docker compose up --build -d"
echo "=============================================================================="
