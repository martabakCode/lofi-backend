#!/bin/bash

# Jenkins Deployment Script for VPS
# Usage: ./deploy.sh [environment] [version]

set -e

# Configuration
ENVIRONMENT=${1:-production}
VERSION=${2:-latest}
APP_NAME="lofiapps"
DEPLOY_DIR="/opt/lofiapps/${ENVIRONMENT}"
DOCKER_REGISTRY="${DOCKER_REGISTRY:-your-registry.com}"
DOCKER_IMAGE="${DOCKER_REGISTRY}/${APP_NAME}"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Logging functions
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

# Validate environment
if [[ ! "$ENVIRONMENT" =~ ^(development|staging|production)$ ]]; then
    log_error "Invalid environment. Must be: development, staging, or production"
    exit 1
fi

log_info "Starting deployment for ${ENVIRONMENT} with version ${VERSION}"

# Create deployment directory
log_info "Creating deployment directory..."
mkdir -p ${DEPLOY_DIR}
cd ${DEPLOY_DIR}

# Pull latest Docker image
log_info "Pulling Docker image ${DOCKER_IMAGE}:${VERSION}..."
docker pull ${DOCKER_IMAGE}:${VERSION}

# Determine docker-compose file
if [ "$ENVIRONMENT" == "production" ]; then
    COMPOSE_FILE="docker-compose.prod.yml"
else
    COMPOSE_FILE="docker-compose.yml"
fi

# Stop existing containers
log_info "Stopping existing containers..."
docker-compose -f ${COMPOSE_FILE} down --remove-orphans || true

# Update environment file
log_info "Updating environment configuration..."
cat > .env.${ENVIRONMENT} << EOF
VERSION=${VERSION}
DOCKER_IMAGE=${DOCKER_IMAGE}
ENVIRONMENT=${ENVIRONMENT}
EOF

# Start new containers
log_info "Starting new containers..."
VERSION=${VERSION} docker-compose -f ${COMPOSE_FILE} up -d

# Wait for application to be ready
log_info "Waiting for application to be ready..."
sleep 10

# Health check
log_info "Performing health check..."
MAX_RETRIES=30
RETRY_COUNT=0

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if curl -sf http://localhost:8080/actuator/health > /dev/null 2>&1; then
        log_info "Health check passed!"
        break
    fi
    
    RETRY_COUNT=$((RETRY_COUNT + 1))
    log_warn "Health check attempt ${RETRY_COUNT}/${MAX_RETRIES} failed. Retrying..."
    sleep 5
done

if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    log_error "Health check failed after ${MAX_RETRIES} attempts"
    log_error "Rolling back..."
    docker-compose -f ${COMPOSE_FILE} down
    exit 1
fi

# Clean up old images
log_info "Cleaning up old Docker images..."
docker image prune -f

# Display deployment status
log_info "Deployment completed successfully!"
echo ""
echo "========================================"
echo "Deployment Summary"
echo "========================================"
echo "Environment: ${ENVIRONMENT}"
echo "Version: ${VERSION}"
echo "Status: $(curl -s http://localhost:8080/actuator/health | grep -o '"status":"[^"]*"' | cut -d'"' -f4)"
echo "========================================"
