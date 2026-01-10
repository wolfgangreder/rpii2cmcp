# RPI I2C MCP Server

A Model Context Protocol (MCP) Server built with Quarkus that enables execution of I2C commands on Raspberry Pi 5+ microcomputers. This server provides a secure REST API for reading from and writing to I2C devices using i2c-tools.

## Features

- ✅ **Quarkus-based**: Modern, reactive Java framework optimized for cloud and containerization
- ✅ **I2C Command Support**: Execute read and write operations on I2C devices
- ✅ **Security First**: Input validation, command sanitization, and configurable access controls
- ✅ **RESTful API**: Clean REST endpoints with JSON request/response
- ✅ **OpenAPI/Swagger**: Built-in API documentation and testing interface
- ✅ **Health Checks**: Built-in health monitoring endpoints
- ✅ **High Test Coverage**: 80%+ code and branch coverage
- ✅ **Complete Documentation**: Comprehensive Javadoc for all public APIs
- ✅ **Easy Installation**: Automated installation and startup scripts
- ✅ **Systemd Integration**: Run as a system service with auto-restart

## Prerequisites

- Raspberry Pi 5+ (or compatible single-board computer with I2C support)
- Raspberry Pi OS (64-bit recommended)
- Java 17 or higher
- Maven 3.8+
- i2c-tools package
- Enabled I2C interface

## Quick Start

### Installation

1. Clone the repository:
```bash
git clone https://github.com/wolfgangreder/rpii2cmcp.git
cd rpii2cmcp
```

2. Run the installation script (requires sudo):
```bash
sudo ./scripts/install.sh
```

The installation script will:
- Update system packages
- Install i2c-tools
- Enable I2C interface
- Configure permissions
- Install Java 17 and Maven (if needed)
- Build the application
- Create a systemd service

3. Reboot if I2C was just enabled:
```bash
sudo reboot
```

### Running the Server

#### Option 1: Using the startup script (Development mode)
```bash
./scripts/start.sh dev
```

#### Option 2: Using the startup script (Production mode)
```bash
./scripts/start.sh prod
```

#### Option 3: Using systemd service
```bash
# Start the service
sudo systemctl start rpii2cmcp

# Enable auto-start on boot
sudo systemctl enable rpii2cmcp

# Check status
sudo systemctl status rpii2cmcp

# View logs
sudo journalctl -u rpii2cmcp -f
```

## API Usage

### Execute I2C Command

**Endpoint:** `POST /api/i2c/execute`

**Request Body:**
```json
{
  "bus": 1,
  "address": "0x48",
  "register": "0x00",
  "operation": "read"
}
```

**Response:**
```json
{
  "success": true,
  "data": "0x42",
  "error": null,
  "command": "i2cget -y 1 0x48 0x00"
}
```

### Read Example

Read from I2C device at address 0x48, register 0x00 on bus 1:

```bash
curl -X POST http://localhost:8080/api/i2c/execute \
  -H "Content-Type: application/json" \
  -d '{
    "bus": 1,
    "address": "0x48",
    "register": "0x00",
    "operation": "read"
  }'
```

### Write Example

Write value 0xFF to register 0x01 of device at address 0x48 on bus 1:

```bash
curl -X POST http://localhost:8080/api/i2c/execute \
  -H "Content-Type: application/json" \
  -d '{
    "bus": 1,
    "address": "0x48",
    "register": "0x01",
    "value": "0xFF",
    "operation": "write"
  }'
```

## API Documentation

Once the server is running, access the interactive API documentation:

- **Swagger UI**: http://localhost:8080/swagger-ui
- **OpenAPI Spec**: http://localhost:8080/openapi
- **Health Check**: http://localhost:8080/q/health

## Configuration

Edit `src/main/resources/application.yml` to customize settings:

```yaml
quarkus:
  http:
    port: 8080  # Change server port

i2c:
  enabled: true  # Enable/disable I2C commands
  command:
    get: /usr/sbin/i2cget  # Path to i2cget
    set: /usr/sbin/i2cset  # Path to i2cset
```

## Security

The server implements multiple security measures:

1. **Input Validation**: All parameters are validated before execution
   - Bus numbers restricted to 0-10
   - Address and register values must be valid hex format (0xNN)
   - Operation must be "read" or "write"
   
2. **Command Sanitization**: Commands are built programmatically, not via string concatenation
3. **No Shell Injection**: Direct process execution without shell interpretation
4. **Configurable Access**: Can be disabled via configuration
5. **Error Handling**: Comprehensive error messages without exposing internals

## Development

### Building

```bash
mvn clean package
```

### Running Tests

```bash
mvn test
```

### Test Coverage

```bash
mvn clean test jacoco:report
```

View coverage report: `target/site/jacoco/index.html`

### Generating Javadoc

```bash
mvn javadoc:javadoc
```

View Javadoc: `target/site/apidocs/index.html`

### Development Mode with Live Reload

```bash
mvn quarkus:dev
```

## I2C Device Detection

To detect I2C devices on your Raspberry Pi:

```bash
# Detect devices on bus 1
sudo i2cdetect -y 1

# List available I2C buses
sudo i2cdetect -l
```

## Troubleshooting

### I2C Device Not Found

1. Check if I2C is enabled:
```bash
ls /dev/i2c-*
```

2. Enable I2C interface:
```bash
sudo raspi-config
# Navigate to: Interface Options > I2C > Enable
```

3. Load I2C kernel module:
```bash
sudo modprobe i2c-dev
```

### Permission Denied

Add your user to the i2c group:
```bash
sudo usermod -a -G i2c $USER
```

Log out and log back in for changes to take effect.

### Service Won't Start

Check logs:
```bash
sudo journalctl -u rpii2cmcp -n 50
```

## Project Structure

```
rpii2cmcp/
├── src/
│   ├── main/
│   │   ├── java/at/reder/rpii2cmcp/
│   │   │   ├── model/          # Data models
│   │   │   ├── resource/       # REST endpoints
│   │   │   └── service/        # Business logic
│   │   └── resources/
│   │       └── application.yml # Configuration
│   └── test/
│       └── java/at/reder/rpii2cmcp/  # Unit tests
├── scripts/
│   ├── install.sh              # Installation script
│   └── start.sh                # Startup script
└── pom.xml                     # Maven configuration
```

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Author

Wolfgang Reder

## Support

For issues and questions, please use the GitHub issue tracker.
