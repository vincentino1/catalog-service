#!/bin/bash

# Catalog Service Run Script
# This script helps to run the Catalog Service with proper environment setup

set -e

echo "🚀 Starting Catalog Service..."
echo ""

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 17 or higher."
    exit 1
fi

# Check Java version
JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1}')
if [ "$JAVA_VERSION" -lt 17 ]; then
    echo "❌ Java version 17 or higher is required. Current version: $JAVA_VERSION"
    exit 1
fi

echo "✅ Java version: $(java -version 2>&1 | head -n 1)"
echo ""

# Check if PostgreSQL is running
if ! nc -z localhost 5432 2>/dev/null; then
    echo "⚠️  PostgreSQL is not running on localhost:5432"
    echo "   You can start it with Docker:"
    echo "   docker run -d --name postgres-catalog -e POSTGRES_DB=vogueThreads -e POSTGRES_USER=devEccomerce -e POSTGRES_PASSWORD='devEccomerce$' -p 5432:5432 postgres:14"
    echo ""
    read -p "Do you want to continue anyway? (y/n) " -n 1 -r
    echo ""
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Set default JWT secret if not provided
if [ -z "$JWT_SECRET" ]; then
    echo "⚠️  JWT_SECRET not set. Using default (not for production!)"
    export JWT_SECRET="your-secret-key-change-this-in-production-must-be-at-least-256-bits"
fi

echo "🔧 Configuration:"
echo "   Database: jdbc:postgresql://localhost:5432/vogueThreads"
echo "   Server Port: 8081"
echo "   Context Path: /api/catalog"
echo ""

# Build the project if jar doesn't exist
if [ ! -f "target/catalog-service-1.0.0.jar" ]; then
    echo "📦 Building the project..."
    mvn clean package -DskipTests
    echo ""
fi

echo "🎯 Starting application..."
echo "   Health: http://localhost:8081/api/catalog/health"
echo "   Products: http://localhost:8081/api/catalog/products"
echo ""

# Run the application
mvn spring-boot:run

