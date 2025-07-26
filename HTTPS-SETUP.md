# DomiSMP HTTPS Setup Guide

This guide will help you set up the DomiSMP server with HTTPS support for NCP integration.

## Prerequisites

✅ Java JDK 8 or 11
✅ Maven 3.6+
✅ MySQL 8.0 (installed)
✅ Project built successfully (`mvn clean install`)

## Database Setup

The database schema `smpdb` already exists with all required tables. You need to create the SMP user:

### Option 1: Using MySQL Command Line (as root)

```bash
mysql -u root -p < smp-config/create-smp-user.sql
```

### Option 2: Using MySQL Workbench

1. Open MySQL Workbench
2. Connect as root user
3. Run the SQL commands in `smp-config/create-smp-user.sql`

### Verify Database Connection

```bash
./test-db-connection.sh
```

## HTTPS Configuration

The server is configured with:

- **HTTPS Port**: 8443
- **HTTP Port**: 8080 (redirects to HTTPS)
- **Keystore**: `smp-config/keystores/smp-keystore.p12`
- **Truststore**: `smp-config/keystores/smp-truststore.p12`
- **Certificate Password**: `changeit`

### Certificate Setup

A self-signed certificate has been created for development/testing. For production:

1. Replace the self-signed certificate with a proper SSL certificate
2. Update the keystore with your certificate:

```bash
keytool -importkeystore -srckeystore your-cert.p12 -destkeystore smp-config/keystores/smp-keystore.p12
```

### Adding NCP Client Certificates

To allow NCP client certificate authentication:

```bash
keytool -import -alias ncp-client -keystore smp-config/keystores/smp-truststore.p12 -file ncp-client-cert.crt -storepass changeit
```

## Starting the Server

### Windows

```cmd
start-smp-https.bat
```

### Linux/Bash

```bash
./start-smp-https.sh
```

## Access Points

Once started, the SMP server will be available at:

- **HTTP** (Development): <http://localhost:8080/smp/>
- **HTTP** (Current restart): <http://localhost:8081/smp/>
- **HTTPS**: <https://localhost:8443/>
- **Admin UI**: <http://localhost:8081/ui/>
- **API**: <http://localhost:8081/api/>

### Default Credentials

- **Username**: `system`
- **Password**: `test123` (default password hash in database)

### Server Restart Status

✅ **Server restarted successfully on port 8081** - Property changes have been applied!

## Configuration Files

- **Main Config**: `smp-config/smp.config.properties`
- **Database**: MySQL connection to `smpdb` on localhost:3306
- **Certificates**: `smp-config/keystores/`
- **Logs**: `smp-config/logs/`

## Troubleshooting

### Database Connection Issues

1. Verify MySQL is running: `mysql -u root -p -e "SHOW DATABASES;"`
2. Check if smp user exists: `mysql -u root -p -e "SELECT User FROM mysql.user WHERE User='smp';"`
3. Run the user creation script: `mysql -u root -p < smp-config/create-smp-user.sql`

### HTTPS Issues

1. Check certificate: `keytool -list -keystore smp-config/keystores/smp-keystore.p12 -storepass changeit`
2. Verify port 8443 is not in use: `netstat -an | grep 8443`

### NCP Integration

1. Import NCP client certificates into the truststore
2. Configure NCP to connect to: `https://localhost:8443/`
3. Set client certificate authentication in NCP configuration

## Development Mode

For development, you can disable HTTPS by editing `smp-config/smp.config.properties`:

```properties
server.ssl.enabled=false
server.port=8080
```

## Production Deployment

For production deployment:

1. Use proper SSL certificates (not self-signed)
2. Set `smp.mode.development=false`
3. Configure proper database connection pooling
4. Set up proper logging configuration
5. Configure firewall rules for port 8443

## Next Steps

1. ✅ Database setup complete
2. ✅ HTTPS configuration ready
3. ✅ Self-signed certificate created
4. 🔄 Create SMP database user
5. 🔄 Start the SMP server
6. 🔄 Test HTTPS connectivity
7. 🔄 Configure NCP to connect to SMP
