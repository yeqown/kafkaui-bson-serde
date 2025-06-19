# Kafka UI BSON SerDe

This project provides a BSON (Binary JSON) serializer/deserializer for Kafka UI.

## Usage

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