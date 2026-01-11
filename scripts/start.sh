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
DEBUG="${2:-false}"

echo "=========================================="
echo "RPI I2C MCP Server Startup Script"
echo "=========================================="
echo "Mode: $MODE"
if [ "$DEBUG" = "debug" ]; then
    echo "Debug: enabled on port 5005"
fi
echo ""

cd "$PROJECT_DIR"

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed"
    echo "Please run the installation script first: sudo ./scripts/install.sh"
    exit 1
fi

# Check if Gradle wrapper exists (for dev mode)
if [ "$MODE" = "dev" ] && [ ! -f "$PROJECT_DIR/gradlew" ]; then
    echo "Error: Gradle wrapper not found"
    echo "Please ensure gradlew exists in the project directory"
    exit 1
fi

if [ "$MODE" = "dev" ]; then
    echo "Starting in DEVELOPMENT mode..."
    echo "The application will auto-reload on code changes"
    echo "Press Ctrl+C to stop"
    echo ""
    
    if [ "$DEBUG" = "debug" ]; then
        echo "Remote debugging enabled on port 5005"
        ./gradlew quarkusDev -Ddebug=5005
    else
        ./gradlew quarkusDev
    fi
    
elif [ "$MODE" = "prod" ]; then
    echo "Starting in PRODUCTION mode..."
    echo ""
    
    # Check if the application is built
    if [ ! -f "$PROJECT_DIR/build/quarkus-app/quarkus-run.jar" ]; then
        echo "Application not built. Building now..."
        ./gradlew clean build -x test
    fi
    
    echo "Starting server..."
    echo "Press Ctrl+C to stop"
    echo ""
    
    if [ "$DEBUG" = "debug" ]; then
        echo "Remote debugging enabled on port 5005"
        java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005 \
             -jar "$PROJECT_DIR/build/quarkus-app/quarkus-run.jar"
    else
        java -jar "$PROJECT_DIR/build/quarkus-app/quarkus-run.jar"
    fi
    
else
    echo "Error: Invalid mode '$MODE'"
    echo "Usage: $0 [dev|prod] [debug]"
    echo "  dev   - Development mode with hot reload"
    echo "  prod  - Production mode"
    echo "  debug - Enable remote debugging on port 5005"
    exit 1
fi
