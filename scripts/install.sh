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
echo "Step 5: Detecting i2c-tools paths..."
I2CGET_PATH=$(command -v i2cget 2>/dev/null || echo "/usr/sbin/i2cget")
I2CSET_PATH=$(command -v i2cset 2>/dev/null || echo "/usr/sbin/i2cset")
echo "Detected i2cget at: $I2CGET_PATH"
echo "Detected i2cset at: $I2CSET_PATH"

# Update application.yml with detected paths
YAML_FILE="$(dirname "$0")/../src/main/resources/application.yml"
if [ -f "$YAML_FILE" ]; then
    echo "Updating application.yml with detected i2c-tools paths..."
    sed -i "s|get:.*|get: $I2CGET_PATH|" "$YAML_FILE"
    sed -i "s|set:.*|set: $I2CSET_PATH|" "$YAML_FILE"
    echo "Configuration updated"
else
    echo "Warning: application.yml not found at $YAML_FILE"
fi

echo ""
echo "Step 6: Installing Java 17 or higher (if not present)..."
if ! command -v java &> /dev/null; then
    # Try Java 21 first, fallback to Java 17
    if apt-cache show openjdk-21-jdk &> /dev/null; then
        apt-get install -y openjdk-21-jdk
        echo "Java 21 installed"
    else
        apt-get install -y openjdk-17-jdk
        echo "Java 17 installed"
    fi
else
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -ge 17 ]; then
        echo "Java $JAVA_VERSION already installed (compatible)"
    else
        echo "Java version too old, upgrading..."
        if apt-cache show openjdk-21-jdk &> /dev/null; then
            apt-get install -y openjdk-21-jdk
            echo "Java 21 installed"
        else
            apt-get install -y openjdk-17-jdk
            echo "Java 17 installed"
        fi
    fi
fi

echo ""
echo "Step 7: Building the application..."
cd "$(dirname "$0")/.."
sudo -u ${SUDO_USER:-$USER} ./gradlew clean build -x test

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
ExecStart=/usr/bin/java -jar $(pwd)/build/quarkus-app/quarkus-run.jar
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

# Uncomment the following line to enable remote debugging on port 5005
# Environment="JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
# ExecStart=/usr/bin/java \$JAVA_OPTS -jar $(pwd)/build/quarkus-app/quarkus-run.jar

[Install]
WantedBy=multi-user.target
EOF

systemctl daemon-reload
echo "Systemd service created"

echo ""
echo "Step 9: Setting up firewall (if UFW is active)..."
if command -v ufw &> /dev/null && ufw status | grep -q "Status: active"; then
    ufw allow 8080/tcp
    ufw allow 5005/tcp comment 'Java remote debugging'
    echo "Firewall rules added for port 8080 (HTTP) and 5005 (debug)"
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
