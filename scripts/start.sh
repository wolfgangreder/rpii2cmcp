#!/bin/bash
#
# Startup script for RPI I2C MCP Server
# This script starts the MCP server in development or production mode
#
# Usage: ./start.sh [dev|prod]
#

set -e

# Get the directory of this script
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

# Parse command line arguments
MODE="${1:-dev}"

echo "=========================================="
echo "RPI I2C MCP Server Startup Script"
echo "=========================================="
echo "Mode: $MODE"
echo ""

cd "$PROJECT_DIR"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed"
    echo "Please run the installation script first: sudo ./scripts/install.sh"
    exit 1
fi

# Check if Maven is installed (for dev mode)
if [ "$MODE" = "dev" ] && ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed"
    echo "Please run the installation script first: sudo ./scripts/install.sh"
    exit 1
fi

if [ "$MODE" = "dev" ]; then
    echo "Starting in DEVELOPMENT mode..."
    echo "The application will auto-reload on code changes"
    echo "Press Ctrl+C to stop"
    echo ""
    
    mvn quarkus:dev
    
elif [ "$MODE" = "prod" ]; then
    echo "Starting in PRODUCTION mode..."
    echo ""
    
    # Check if the application is built
    if [ ! -f "$PROJECT_DIR/target/quarkus-app/quarkus-run.jar" ]; then
        echo "Application not built. Building now..."
        mvn clean package -DskipTests
    fi
    
    echo "Starting server..."
    echo "Press Ctrl+C to stop"
    echo ""
    
    java -jar "$PROJECT_DIR/target/quarkus-app/quarkus-run.jar"
    
else
    echo "Error: Invalid mode '$MODE'"
    echo "Usage: $0 [dev|prod]"
    exit 1
fi
