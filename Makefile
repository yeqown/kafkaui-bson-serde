.PHONY: build clean help test test-serde run-custom

# Default target
help:
	@echo "Available commands:"
	@echo "  make build       - Build the BSON SerDe JAR"
	@echo "  make clean       - Clean build artifacts"
	@echo "  make run-custom  - Run with custom broker configuration (wl-local01-kafka01) using Podman"
	@echo "  make test        - Run test script to generate sample BSON document"
	@echo "  make test-serde  - Run test script to test BSON serialization and deserialization"
	@echo "  make help        - Show this help message"

# Build the JAR file
build:
	@echo "Building BSON SerDe for Kafka UI..."
	mvn clean package
	@echo "Build completed successfully!"
	@echo "JAR file is available at: target/kafkaui-bson-serde-1.0-SNAPSHOT.jar"

# Clean build artifacts
clean:
	@echo "Cleaning build artifacts..."
	mvn clean
	@echo "Clean completed successfully!"

# Run test script
test:
	@echo "Running test script to generate sample BSON document..."
	./test-bson.sh
	
# Run test script for BSON serialization and deserialization
test-serde:
	@echo "Running test script to test BSON serialization and deserialization..."
	./test-bson-serde.sh
	
# Run with custom configuration
run-custom: build
	@echo "Running Kafka UI with custom broker configuration using Podman..."
	podman run -it -p 8080:8080 \
	  -e SERDE_PACKAGES_PATH=/kafka-ui-serde/ \
	  -e spring.config.additional-location=/etc/kafkaui/config.yml \
	  -v $$(pwd)/target/kafkaui-bson-serde-1.0-SNAPSHOT.jar:/kafka-ui-serde/kafkaui-bson-serde-1.0-SNAPSHOT.jar \
	  -v $$(pwd)/config.sample.yml:/etc/kafkaui/config.yml \
	  provectuslabs/kafka-ui:latest
