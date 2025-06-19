# Kafka UI BSON SerDe

This project provides a BSON (Binary JSON) serializer/deserializer for Kafka UI.

## Quick Start

Use the following command to build the BSON SerDe JAR:

```bash
$ make      
Available commands:
  make build       - Build the BSON SerDe JAR
  make clean       - Clean build artifacts
  make run-custom  - Run with custom broker configuration (wl-local01-kafka01) using Podman
  make test        - Run test script to generate sample BSON document
  make help        - Show this help message
```

## Example Usage

```bash
$ make run-custom

podman run -it -p 8080:8080 \
	  -e SERDE_PACKAGES_PATH=/kafka-ui-serde/ \
	  -e spring.config.additional-location=/etc/kafkaui/config.yml \
	  -v $$(pwd)/target/kafkaui-bson-serde-1.0-SNAPSHOT.jar:/kafka-ui-serde/kafkaui-bson-serde-1.0-SNAPSHOT.jar \
	  -v $$(pwd)/config.sample.yml:/etc/kafkaui/config.yml \
	  provectuslabs/kafka-ui:latest
```

## Configuration

```yaml
kafka:
  clusters:
    - name: local-cluster
      bootstrapServers: localhost:9092
      serde:
        - name: BSON
          className: com.yeqown.kafkaui.serde.bson.BsonSerde
          filePath: /kafka-ui-serde/kafkaui-bson-serde-1.0-SNAPSHOT.jar
          topicKeysPattern: ".*"
          topicValuesPattern: ".*"
```