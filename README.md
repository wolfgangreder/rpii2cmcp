# rpii2cmcp

MCP (Model Context Protocol) Server for Raspberry Pi 5+ to execute I2C commands.

## Overview

rpii2cmcp is a Quarkus-based MCP server designed to run on Raspberry Pi 5+ microcomputers. It provides a secure and efficient interface for executing I2C (Inter-Integrated Circuit) commands through the i2c-tools suite, enabling remote control and interaction with I2C devices connected to your Raspberry Pi.

## Features

- **MCP Server Implementation**: Built with Quarkus for high performance and low resource consumption
- **I2C Command Execution**: Execute I2C commands using the standard i2c-tools suite
- **Secure Design**: Implements security best practices to protect against unauthorized access
- **Well-Tested**: 80% code and branch coverage to ensure reliability
- **Comprehensive Documentation**: Full Javadoc documentation for all public APIs
- **Easy Installation**: Automated installation and startup scripts for Raspberry Pi 5+
- **Monitoring & Logging**: Built-in logging and monitoring capabilities

## Requirements

### Hardware
- Raspberry Pi 5 or newer
- I2C devices connected to the GPIO pins

### Software
- Java 17 or newer
- i2c-tools package
- Maven or Gradle (for building from source)

## Installation

### Prerequisites

First, ensure i2c-tools is installed on your Raspberry Pi:

```bash
sudo apt-get update
sudo apt-get install -y i2c-tools
```

Enable I2C on your Raspberry Pi:

```bash
sudo raspi-config
# Navigate to: Interface Options -> I2C -> Enable
```

### Using the Installation Script

1. Clone the repository:
```bash
git clone https://github.com/wolfgangreder/rpii2cmcp.git
cd rpii2cmcp
```

2. Run the installation script:
```bash
chmod +x install.sh
sudo ./install.sh
```

This will:
- Install all necessary dependencies
- Build the application
- Configure the service
- Set up automatic startup

### Manual Installation

1. Clone the repository and build:
```bash
git clone https://github.com/wolfgangreder/rpii2cmcp.git
cd rpii2cmcp
./mvnw clean package
```

2. Install the compiled application:
```bash
sudo cp target/rpii2cmcp-runner /usr/local/bin/
sudo chmod +x /usr/local/bin/rpii2cmcp-runner
```

3. Configure as a systemd service (optional):
```bash
sudo cp scripts/rpii2cmcp.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable rpii2cmcp
sudo systemctl start rpii2cmcp
```

## Configuration

Configuration is managed through `application.properties` or environment variables.

### Basic Configuration

```properties
# Server configuration
quarkus.http.port=8080
quarkus.http.host=0.0.0.0

# I2C configuration
i2c.default-bus=1
i2c.timeout=1000

# Security configuration
quarkus.security.enabled=true
```

### Environment Variables

- `RPII2CMCP_PORT`: HTTP server port (default: 8080)
- `I2C_BUS`: Default I2C bus number (default: 1)
- `I2C_TIMEOUT`: Command timeout in milliseconds (default: 1000)

## Usage

### Starting the Server

#### Using the startup script:
```bash
./startup.sh
```

#### Using systemd:
```bash
sudo systemctl start rpii2cmcp
```

#### Running directly:
```bash
java -jar target/rpii2cmcp-runner.jar
```

### API Examples

#### Detecting I2C Devices

```bash
curl -X GET http://localhost:8080/api/i2c/detect?bus=1
```

#### Reading from an I2C Device

```bash
curl -X POST http://localhost:8080/api/i2c/read \
  -H "Content-Type: application/json" \
  -d '{
    "bus": 1,
    "address": "0x48",
    "register": "0x00",
    "length": 2
  }'
```

#### Writing to an I2C Device

```bash
curl -X POST http://localhost:8080/api/i2c/write \
  -H "Content-Type: application/json" \
  -d '{
    "bus": 1,
    "address": "0x48",
    "register": "0x00",
    "data": [0x01, 0x02]
  }'
```

### MCP Protocol

The server implements the Model Context Protocol, allowing AI models and assistants to interact with I2C devices through a standardized interface.

Example MCP request:
```json
{
  "jsonrpc": "2.0",
  "method": "i2c/execute",
  "params": {
    "command": "i2cget",
    "args": ["-y", "1", "0x48", "0x00"]
  },
  "id": 1
}
```

## Development

### Building from Source

```bash
# Using Maven
./mvnw clean package

# Using Gradle
./gradlew build
```

### Running Tests

```bash
# Run all tests
./mvnw test

# Run with coverage report
./mvnw verify

# View coverage report
open target/site/jacoco/index.html
```

### Development Mode

Run in development mode with hot reload:

```bash
./mvnw quarkus:dev
```

### Generating Javadoc

```bash
./mvnw javadoc:javadoc
# Documentation will be in target/site/apidocs/
```

## Architecture

### Components

- **MCP Server**: Quarkus-based REST API implementing the MCP protocol
- **I2C Service**: Wrapper around i2c-tools for executing I2C commands
- **Security Layer**: Authentication and authorization for API access
- **Command Validator**: Validates I2C commands before execution
- **Error Handler**: Comprehensive error handling and logging

### Security

- Input validation for all I2C commands
- Rate limiting to prevent abuse
- Authentication required for all API endpoints
- Audit logging of all I2C operations
- Restricted execution permissions

## Troubleshooting

### Common Issues

#### I2C Device Not Detected

1. Verify I2C is enabled:
```bash
sudo raspi-config
```

2. Check for connected devices:
```bash
sudo i2cdetect -y 1
```

3. Verify permissions:
```bash
sudo usermod -aG i2c $USER
# Log out and back in for changes to take effect
```

#### Permission Denied Errors

Ensure the user running the service has I2C permissions:
```bash
sudo usermod -aG i2c rpii2cmcp
```

#### Server Won't Start

Check the logs:
```bash
sudo journalctl -u rpii2cmcp -n 50
```

Verify Java version:
```bash
java -version
```

### Logs

Application logs are located at:
- Systemd: `journalctl -u rpii2cmcp`
- Direct run: Console output or configured log file

## Testing

The project maintains 80% test coverage across:
- Unit tests for all service components
- Integration tests for I2C command execution
- Security tests for authentication and authorization
- End-to-end API tests

Run tests with coverage:
```bash
./mvnw clean verify
```

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Code Standards

- Follow Java coding conventions
- Maintain 80% test coverage
- Add Javadoc for all public APIs
- Update documentation for new features

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## API Reference

For detailed API documentation, see the Javadoc documentation:
```bash
./mvnw javadoc:javadoc
open target/site/apidocs/index.html
```

## Support

For issues, questions, or contributions, please:
- Open an issue on GitHub
- Check existing documentation
- Review the troubleshooting section

## Acknowledgments

- Built with [Quarkus](https://quarkus.io/)
- Uses [i2c-tools](https://www.kernel.org/doc/Documentation/i2c/tools/i2c-tools)
- Implements the [Model Context Protocol](https://modelcontextprotocol.io/)

## Roadmap

- [ ] Support for additional I2C protocols
- [ ] Web-based management interface
- [ ] Docker container support
- [ ] Multiple device profiles
- [ ] Enhanced monitoring and metrics
