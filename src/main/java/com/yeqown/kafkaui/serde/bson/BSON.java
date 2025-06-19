package com.yeqown.kafkaui.serde.bson;

import com.provectus.kafka.ui.serde.api.DeserializeResult;
import com.provectus.kafka.ui.serde.api.PropertyResolver;
import com.provectus.kafka.ui.serde.api.RecordHeaders;
import com.provectus.kafka.ui.serde.api.SchemaDescription;
import com.provectus.kafka.ui.serde.api.Serde;

import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.DocumentCodec;

import com.mongodb.MongoClientSettings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Optional;


public class BSON implements Serde {

    private static final Logger LOG = LoggerFactory.getLogger(BSON.class);
    private static final JsonWriterSettings JSON_WRITER_SETTINGS = 
            JsonWriterSettings.builder()
                .outputMode(JsonMode.RELAXED)
                .build();

    @Override
    public void configure(PropertyResolver serdeProperties, PropertyResolver kafkaClusterProperties, PropertyResolver globalProperties) {
        // No configuration needed
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.of("BSON");
    }

    @Override
    public Optional<SchemaDescription> getSchema(String topic, Target type) {
        // BSON is schema-less, so we don't provide a schema
        return Optional.empty();
    }

    @Override
    public boolean canDeserialize(String topic, Target type) {
        // We can deserialize any BSON data
        return true;
    }

    @Override
    public boolean canSerialize(String topic, Target type) {
        // We can serialize any BSON data
        return true;
    }

    @Override
    public Serializer serializer(String topic, Target type) {
        return new BsonSerializer();
    }

    @Override
    public Deserializer deserializer(String topic, Target type) {
        return new BsonDeserializer();
    }

    private static class BsonSerializer implements Serializer {
        // 提取 CodecRegistry 和 DocumentCodec 的创建逻辑
        private static final CodecRegistry CODEC_REGISTRY = CodecRegistries.fromRegistries(
                com.mongodb.MongoClientSettings.getDefaultCodecRegistry()
        );
        private static final org.bson.codecs.DocumentCodec DOCUMENT_CODEC = new org.bson.codecs.DocumentCodec(CODEC_REGISTRY);

        @Override
        public byte[] serialize(String input) {
            if (input == null || input.trim().isEmpty()) {
                return new byte[0];
            }

            try {
                // Parse the input JSON string into a Document object
                Document document = Document.parse(input);

                // Serialize the Document object to BSON binary
                org.bson.io.BasicOutputBuffer output = new org.bson.io.BasicOutputBuffer();
                try (org.bson.BsonBinaryWriter writer = new org.bson.BsonBinaryWriter(output)) {
                    DOCUMENT_CODEC.encode(writer, document, org.bson.codecs.EncoderContext.builder().build());
                    // Flush the writer to ensure all data is written to the buffer
                    writer.flush();
                }
                return output.toByteArray();
            } catch (Exception e) {
                LOG.error("Failed to serialize to BSON", e);
                return new byte[0];
            }
        }
    }

    private static class BsonDeserializer implements Deserializer {
        // Create a BSON document codec registry
        private static final CodecRegistry CODEC_REGISTRY = CodecRegistries.fromRegistries(
                com.mongodb.MongoClientSettings.getDefaultCodecRegistry()
        );

        private static final DocumentCodec DOCUMENT_CODEC = new DocumentCodec(CODEC_REGISTRY);

        @Override
        public DeserializeResult deserialize(RecordHeaders headers, byte[] data) {
            if (data == null || data.length == 0) {
                return new DeserializeResult(new String(data, StandardCharsets.UTF_8), DeserializeResult.Type.STRING, Collections.emptyMap());
            }

            try {
                // Create a ByteBuffer from the byte array
                java.nio.ByteBuffer byteBuffer = java.nio.ByteBuffer.wrap(data);

                // Create a BsonBinaryReader
                org.bson.BsonBinaryReader reader = new org.bson.BsonBinaryReader(byteBuffer);

                // Decode the BSON binary data into a Document object
                Document document = DOCUMENT_CODEC.decode(reader, org.bson.codecs.DecoderContext.builder().build());

                // Convert the Document object to a JSON string
                String json = document.toJson(JSON_WRITER_SETTINGS);

                // Close the reader
                reader.close();

                return new DeserializeResult(json, DeserializeResult.Type.STRING, Collections.emptyMap());
            } catch (Exception e) {
                LOG.error("Failed to deserialize BSON data", e);
                return new DeserializeResult(new String(data, StandardCharsets.UTF_8), DeserializeResult.Type.STRING, Collections.emptyMap());
            }
        }
    }
}
