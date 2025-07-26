#!/bin/bash
# Test database connection script

echo "Testing MySQL database connection..."
echo

# Test with smp user
echo "Testing connection with smp user..."
mysql -u smp -p -h localhost -P 3306 -D smpdb -e "SELECT COUNT(*) as table_count FROM information_schema.tables WHERE table_schema = 'smpdb';"

if [ $? -eq 0 ]; then
    echo "✅ Database connection successful!"
    echo "✅ SMP user can access smpdb database"
else
    echo "❌ Database connection failed!"
    echo "Please run the create-smp-user.sql script as MySQL root user:"
    echo "mysql -u root -p < smp-config/create-smp-user.sql"
fi
