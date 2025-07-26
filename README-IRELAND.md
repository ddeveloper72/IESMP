# 🇮🇪 Ireland eHealth DomiSMP Configuration

This repository contains a secure configuration for DomiSMP (Digital Service Infrastructure Service Metadata Publisher) specifically configured for Ireland's eHealth infrastructure.

## 🔒 Security Notice

This fork has been **SECURED** for public GitHub usage:

- ✅ All hardcoded passwords removed
- ✅ Credentials moved to `.env` file
- ✅ Sensitive files added to `.gitignore`
- ✅ Environment-based configuration

## 🚀 Quick Start

### 1. Clone Repository

```bash
git clone https://github.com/YOUR_USERNAME/domismp-ireland.git
cd domismp-ireland
```

### 2. Configure Environment

```bash
# Copy environment template
cp .env.example .env

# Edit .env with your credentials
nano .env
```

### 3. Required Environment Variables

Configure these in your `.env` file:

```bash
# Database Configuration
DB_ROOT_PASSWORD=your_secure_root_password_here
DB_USER=smp
DB_PASSWORD=your_secure_smp_password_here

# Ireland eHealth Configuration
IRELAND_DOMAIN=domain1
IRELAND_GROUP=ehealth-ie
IRELAND_USER=iesmpuser
IRELAND_USER_EMAIL=iesmpuser@ehealth.ie
IRELAND_USER_PASSWORD=your_secure_ireland_password_here

# Docker Configuration
SMP_VERSION=5.2-SNAPSHOT
SMP_PORT=8290
```

### 4. Deploy Ireland eHealth Infrastructure

```bash
cd domismp-tests/domismp-docker/compose/domismp-springboot-mysql

# Start DomiSMP
docker-compose up -d

# Configure Ireland eHealth settings
chmod +x setup-ireland-config-secure.sh
./setup-ireland-config-secure.sh
```

### 5. Access DomiSMP

- **URL**: <http://localhost:8290/smp/>
- **Login**: Use credentials from your `.env` file
- **Domain**: domain1
- **Group**: ehealth-ie

## 🛡️ Security Features

### Files Protected by .gitignore

- ✅ `.env` files (environment variables)
- ✅ `ireland-config-backup/` (database backups)
- ✅ `*.sql` files (database dumps)
- ✅ Security certificates (`.jks`, `.p12`, `.pem`)
- ✅ Configuration scripts with embedded passwords

### Secure Scripts

- 🔐 `setup-ireland-config-secure.sh` - Environment-based setup
- 🔐 `backup-ireland-config-secure.sh` - Secure backup with .env
- 🔐 `restore-ireland-config-secure.sh` - Secure restore with .env

## 📊 Ireland eHealth Services Configured

The setup includes these pre-configured FHIR services:

1. **Patient Identification**: `urn:ehealth:patientidentificationandauthentication::xcpd::crossgatewaypatientdiscovery`
2. **Cross Gateway Query**: `urn:ehealth:requestofdata::xca::crossgatewayquery`
3. **Cross Gateway Retrieve**: `urn:ehealth:requestofdata::xca::crossgatewayretrieve`
4. **Document Provision**: `urn:ehealth:provisioningofdata:provide::xdr::provideandregisterdocumentset-b`
5. **FHIR Service**: `urn:ehealth:fhirservice`
6. **International Search Mask**: `urn:ehealth:ism::internationalsearchmask`

## 🔄 Backup & Restore

### Create Backup

```bash
./backup-ireland-config-secure.sh
```

### Restore from Backup

```bash
./restore-ireland-config-secure.sh 20250726_143000
```

## 🌐 Integration with OpenNCP

This DomiSMP instance is configured to work with:

- **OpenNCP** (EU National Contact Point)
- **eHealth Portal** (Patient lookup interface)
- **Network**: `openncp` Docker network
- **Hostname**: `smp-springboot-mysql.local:8084`

## 📝 Original Project

This is a security-enhanced fork of:

- **Original**: [jrihtarsic/domismp](https://github.com/jrihtarsic/domismp)
- **License**: See `License.txt`
- **Purpose**: EU eHealth interoperability infrastructure

## ⚠️ Important Notes

1. **Never commit `.env` file** - Contains sensitive credentials
2. **Backup regularly** - Use secure backup scripts
3. **Use strong passwords** - Generate secure credentials
4. **Network security** - Consider firewall rules for production
5. **Certificate management** - Replace demo certificates for production

## 🛠️ Development

For development purposes:

```bash
# View logs
docker-compose logs -f

# Access database
docker exec -it domismp-springboot-mysql-smp-springboot-1 mysql -u root -p

# Rebuild containers
docker-compose down && docker-compose up --build -d
```

## 📞 Support

For issues related to:

- **DomiSMP**: See original repository
- **Ireland configuration**: Check environment variables in `.env`
- **Security**: Ensure all sensitive files are in `.gitignore`
