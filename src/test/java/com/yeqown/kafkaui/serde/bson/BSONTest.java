package com.yeqown.kafkaui.serde.bson;

import com.provectus.kafka.ui.serde.api.DeserializeResult;
import com.provectus.kafka.ui.serde.api.Serde;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class BSONTest {

    private BSON serde;

    @BeforeEach
    void setUp() {
        serde = new BSON();
    }

    @Test
    void testSerializeAndDeserializeSimpleDocument() {
        // Create a simple JSON document
        String jsonInput = "{\"name\":\"test\",\"value\":123}";

        // Serialize to BSON binary
        Serde.Serializer serializer = serde.serializer("test-topic", Serde.Target.VALUE);
        byte[] bsonData = serializer.serialize(jsonInput);

        // Verify that we got some binary data
        assertThat(bsonData).isNotNull();
        assertThat(bsonData.length).isGreaterThan(0);

        // Deserialize from BSON binary back to JSON
        Serde.Deserializer deserializer = serde.deserializer("test-topic", Serde.Target.VALUE);
        DeserializeResult result = deserializer.deserialize(null, bsonData);

        // Verify
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(DeserializeResult.Type.STRING);
        assertThat(result.getResult()).isNotNull();

        // The result should be a JSON string that contains the same data
        String resultJson = (String) result.getResult();
        assertThat(resultJson).contains("\"name\"");
        assertThat(resultJson).contains("\"test\"");
        assertThat(resultJson).contains("\"value\"");
        assertThat(resultJson).contains("123");
    }

    @Test
    void testSerializeAndDeserializeComplexDocument() {
        // Create a complex document
        Document nested = new Document()
                .append("field1", "value1")
                .append("field2", 42)
                .append("array", Arrays.asList(1, 2, 3, 4, 5));

        Document original = new Document()
                .append("name", "Complex Document")
                .append("timestamp", new Date())
                .append("nested", nested);

        // Convert to JSON
        String jsonInput = original.toJson();

        // Serialize to BSON binary
        Serde.Serializer serializer = serde.serializer("test-topic", Serde.Target.VALUE);
        byte[] bsonData = serializer.serialize(jsonInput);

        // Verify that we got some binary data
        assertThat(bsonData).isNotNull();
        assertThat(bsonData.length).isGreaterThan(0);

        // Deserialize from BSON binary back to JSON
        Serde.Deserializer deserializer = serde.deserializer("test-topic", Serde.Target.VALUE);
        DeserializeResult result = deserializer.deserialize(null, bsonData);

        // Verify
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(DeserializeResult.Type.STRING);
        assertThat(result.getResult()).isNotNull();

        // The result should be a JSON string that contains the same data
        String resultJson = (String) result.getResult();
        assertThat(resultJson).contains("\"name\"");
        assertThat(resultJson).contains("\"Complex Document\"");
        assertThat(resultJson).contains("\"field1\"");
        assertThat(resultJson).contains("\"value1\"");
        assertThat(resultJson).contains("\"field2\"");
        assertThat(resultJson).contains("42");
    }

    @Test
    void testDeserializeInvalidData() {
        // Create some invalid BSON data
        byte[] invalidData = "This is not valid BSON".getBytes(StandardCharsets.UTF_8);

        // Deserialize
        Serde.Deserializer deserializer = serde.deserializer("test-topic", Serde.Target.VALUE);
        DeserializeResult result = deserializer.deserialize(null, invalidData);

        // Verify that we get a result even with invalid data
        assertThat(result).isNotNull();
        assertThat(result.getType()).isEqualTo(DeserializeResult.Type.STRING);
        assertThat(result.getResult()).isNotNull();

        // The result should contain the original text
        String resultJson = (String) result.getResult();
        assertThat(resultJson).contains("This is not valid BSON");
    }

    @Test
    void testSerializeInvalidJson() {
        // Create invalid JSON
        String invalidJson = "{name:test, invalid json}";

        // Serialize
        Serde.Serializer serializer = serde.serializer("test-topic", Serde.Target.VALUE);
        byte[] serialized = serializer.serialize(invalidJson);

        // Verify that we get an empty byte array for invalid JSON
        assertThat(serialized).isNotNull();
        assertThat(serialized.length).isEqualTo(0);
    }
}
