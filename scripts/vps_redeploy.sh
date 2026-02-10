#!/bin/bash
# vps_redeploy.sh
# Run this script on your VPS to separate/restart containers after configuration changes

# Define your compose file (change to docker-compose.prod.yml if you use that instead)
COMPOSE_FILE="docker-compose.vps.yml"

echo "🔄 Redeploying containers using $COMPOSE_FILE..."

# 1. Pull the latest images (if you are pushing new code to a registry)
# echo "⬇️ Pulling latest images..."
# docker-compose -f $COMPOSE_FILE pull

# 2. Stop and remove existing containers
# This ensures that ANY changes to .env or network configurations are fully applied
echo "tj Stopping containers..."
docker-compose -f $COMPOSE_FILE down

# 3. Start containers in detached mode
echo "🚀 Starting containers..."
docker-compose -f $COMPOSE_FILE up -d

# 4. Check health
echo "aaS checking status..."
docker-compose -f $COMPOSE_FILE ps

echo "✅ Done! Application should be running with new configurations."
