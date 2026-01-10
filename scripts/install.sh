#!/bin/bash
#
# Installation script for RPI I2C MCP Server
# This script installs all dependencies and builds the application for Raspberry Pi 5
#
# Usage: sudo ./install.sh
#

set -e

echo "=========================================="
echo "RPI I2C MCP Server Installation Script"
echo "=========================================="
echo ""

# Check if running as root
if [ "$EUID" -ne 0 ]; then 
    echo "Error: This script must be run as root (use sudo)"
    exit 1
fi

# Check if running on Raspberry Pi
if ! grep -q "Raspberry Pi" /proc/cpuinfo 2>/dev/null; then
    echo "Warning: This does not appear to be a Raspberry Pi"
    read -p "Continue anyway? (y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

echo "Step 1: Updating system packages..."
apt-get update

echo ""
echo "Step 2: Installing i2c-tools..."
apt-get install -y i2c-tools

echo ""
echo "Step 3: Enabling I2C interface..."
# Enable I2C if not already enabled
if ! grep -q "^dtparam=i2c_arm=on" /boot/config.txt; then
    echo "dtparam=i2c_arm=on" >> /boot/config.txt
    echo "I2C interface enabled (reboot required)"
else
    echo "I2C interface already enabled"
fi

# Load I2C kernel modules
if ! lsmod | grep -q i2c_dev; then
    modprobe i2c-dev
    echo "i2c-dev" >> /etc/modules
    echo "I2C kernel module loaded"
fi

echo ""
echo "Step 4: Setting up permissions..."
# Add user to i2c group
if [ -n "$SUDO_USER" ]; then
    usermod -a -G i2c $SUDO_USER
    echo "Added $SUDO_USER to i2c group"
fi

# Set permissions for i2c devices
if [ -e /dev/i2c-1 ]; then
    chmod 666 /dev/i2c-1
    echo "Set permissions for /dev/i2c-1"
fi

echo ""
echo "Step 5: Installing Java 17 (if not present)..."
if ! command -v java &> /dev/null || ! java -version 2>&1 | grep -q "17"; then
    apt-get install -y openjdk-17-jdk
    echo "Java 17 installed"
else
    echo "Java 17 already installed"
fi

echo ""
echo "Step 6: Installing Maven (if not present)..."
if ! command -v mvn &> /dev/null; then
    apt-get install -y maven
    echo "Maven installed"
else
    echo "Maven already installed"
fi

echo ""
echo "Step 7: Building the application..."
cd "$(dirname "$0")/.."
sudo -u ${SUDO_USER:-$USER} mvn clean package -DskipTests

echo ""
echo "Step 8: Creating systemd service..."
cat > /etc/systemd/system/rpii2cmcp.service << EOF
[Unit]
Description=RPI I2C MCP Server
After=network.target

[Service]
Type=simple
User=${SUDO_USER:-$USER}
WorkingDirectory=$(pwd)
ExecStart=/usr/bin/java -jar $(pwd)/target/quarkus-app/quarkus-run.jar
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
echo "Systemd service created"

echo ""
echo "Step 9: Setting up firewall (if UFW is active)..."
if command -v ufw &> /dev/null && ufw status | grep -q "Status: active"; then
    ufw allow 8080/tcp
    echo "Firewall rule added for port 8080"
else
    echo "UFW not active, skipping firewall setup"
fi

echo ""
echo "=========================================="
echo "Installation completed successfully!"
echo "=========================================="
echo ""
echo "Next steps:"
echo "1. Reboot the system if I2C was just enabled: sudo reboot"
echo "2. Start the service: sudo systemctl start rpii2cmcp"
echo "3. Enable auto-start: sudo systemctl enable rpii2cmcp"
echo "4. Check status: sudo systemctl status rpii2cmcp"
echo "5. View logs: sudo journalctl -u rpii2cmcp -f"
echo ""
echo "The API will be available at: http://localhost:8080/api/i2c/execute"
echo "Swagger UI: http://localhost:8080/swagger-ui"
echo "OpenAPI spec: http://localhost:8080/openapi"
echo ""
