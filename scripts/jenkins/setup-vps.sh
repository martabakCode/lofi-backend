#!/bin/bash

# VPS Setup Script for LofiApps Deployment
# Run this script on your VPS to prepare for Jenkins deployment

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if running as root
if [ "$EUID" -ne 0 ]; then 
    log_error "Please run as root or with sudo"
    exit 1
fi

log_info "Starting VPS setup for LofiApps..."

# Update system
log_info "Updating system packages..."
apt-get update && apt-get upgrade -y

# Install required packages
log_info "Installing required packages..."
apt-get install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg \
    lsb-release \
    software-properties-common \
    git \
    jq \
    unzip

# Install Docker
log_info "Installing Docker..."
if ! command -v docker &> /dev/null; then
    curl -fsSL https://get.docker.com -o get-docker.sh
    sh get-docker.sh
    rm get-docker.sh
    usermod -aG docker $SUDO_USER || true
    log_info "Docker installed successfully"
else
    log_warn "Docker is already installed"
fi

# Install Docker Compose
log_info "Installing Docker Compose..."
if ! command -v docker-compose &> /dev/null; then
    DOCKER_COMPOSE_VERSION=$(curl -s https://api.github.com/repos/docker/compose/releases/latest | grep -oP '"tag_name": "\K(.*)(?=")')
    curl -L "https://github.com/docker/compose/releases/download/${DOCKER_COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
    log_info "Docker Compose installed successfully"
else
    log_warn "Docker Compose is already installed"
fi

# Create deployment directories
log_info "Creating deployment directories..."
mkdir -p /opt/lofiapps/{development,staging,production}
mkdir -p /opt/lofiapps/nginx/{conf.d,ssl}
mkdir -p /var/www/certbot

# Create Jenkins user for deployment
log_info "Creating jenkins-deploy user..."
if ! id "jenkins-deploy" &>/dev/null; then
    useradd -m -s /bin/bash jenkins-deploy
    usermod -aG docker jenkins-deploy
    log_info "User jenkins-deploy created"
else
    log_warn "User jenkins-deploy already exists"
fi

# Setup SSH for Jenkins
log_info "Setting up SSH for Jenkins deployment..."
mkdir -p /home/jenkins-deploy/.ssh
chmod 700 /home/jenkins-deploy/.ssh

# Note: You'll need to manually add Jenkins public key to /home/jenkins-deploy/.ssh/authorized_keys
touch /home/jenkins-deploy/.ssh/authorized_keys
chmod 600 /home/jenkins-deploy/.ssh/authorized_keys
chown -R jenkins-deploy:jenkins-deploy /home/jenkins-deploy/.ssh

# Setup firewall (UFW)
log_info "Configuring firewall..."
if command -v ufw &> /dev/null; then
    ufw default deny incoming
    ufw default allow outgoing
    ufw allow ssh
    ufw allow 80/tcp
    ufw allow 443/tcp
    ufw --force enable
    log_info "Firewall configured"
else
    log_warn "UFW not installed, skipping firewall configuration"
fi

# Install Certbot for SSL
log_info "Installing Certbot..."
if ! command -v certbot &> /dev/null; then
    snap install core
    snap refresh core
    snap install --classic certbot
    ln -s /snap/bin/certbot /usr/bin/certbot
    log_info "Certbot installed"
else
    log_warn "Certbot is already installed"
fi

# Create systemd service for application health monitoring (optional)
log_info "Creating health monitoring service..."
cat > /etc/systemd/system/lofiapps-health.service << 'EOF'
[Unit]
Description=LofiApps Health Monitor
After=docker.service

[Service]
Type=simple
ExecStart=/opt/lofiapps/scripts/health-check.sh
Restart=always
RestartSec=60

[Install]
WantedBy=multi-user.target
EOF

# Create health check script
mkdir -p /opt/lofiapps/scripts
cat > /opt/lofiapps/scripts/health-check.sh << 'EOF'
#!/bin/bash
# Simple health check script
HEALTH_URL="http://localhost:8080/actuator/health"
LOG_FILE="/var/log/lofiapps-health.log"

if ! curl -sf "$HEALTH_URL" > /dev/null 2>&1; then
    echo "$(date): Health check failed" >> "$LOG_FILE"
    # Optionally send notification or restart container
    # docker-compose -f /opt/lofiapps/production/docker-compose.prod.yml restart app
else
    echo "$(date): Health check passed" >> "$LOG_FILE"
fi
EOF

chmod +x /opt/lofiapps/scripts/health-check.sh

# Set proper permissions
chown -R jenkins-deploy:jenkins-deploy /opt/lofiapps

# Display summary
echo ""
echo "========================================"
echo "VPS Setup Complete!"
echo "========================================"
echo ""
echo "Next steps:"
echo "1. Add Jenkins SSH public key to /home/jenkins-deploy/.ssh/authorized_keys"
echo "2. Copy your application files to /opt/lofiapps/production/"
echo "3. Configure environment variables in .env.production"
echo "4. Setup SSL certificates with: certbot --nginx"
echo "5. Start the application with: docker-compose -f docker-compose.prod.yml up -d"
echo ""
echo "Directories created:"
echo "  - /opt/lofiapps/production"
echo "  - /opt/lofiapps/staging"
echo "  - /opt/lofiapps/development"
echo "  - /opt/lofiapps/nginx"
echo "========================================"
