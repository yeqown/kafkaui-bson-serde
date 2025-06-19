#!/bin/bash
set -e

echo "Building BSON SerDe for Kafka UI..."
mvn clean package

echo "Build completed successfully!"
echo "JAR file is available at: target/kafkaui-bson-serde-1.0-SNAPSHOT.jar"
