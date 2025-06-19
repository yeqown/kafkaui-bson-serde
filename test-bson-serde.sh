#!/bin/bash
set -e

echo "Testing BSON serialization and deserialization"
echo "=============================================="
echo

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "Error: Java is required but not found"
    exit 1
fi

# Build the project if the JAR doesn't exist
if [ ! -f target/kafkaui-bson-serde-1.0-SNAPSHOT.jar ]; then
    echo "Building the project..."
    mvn clean package
fi

# Create a simple Java program to test the BSON serialization and deserialization
cat > BsonTest.java << 'EOF'
import com.yeqown.kafkaui.serde.bson.BSON;
import com.yeqown.kafkaui.serde.bson.BsonUtil;
import com.provectus.kafka.ui.serde.api.DeserializeResult;
import com.provectus.kafka.ui.serde.api.Serde;
import org.bson.Document;

import java.util.Arrays;
import java.util.Date;

public class BsonTest {
    public static void main(String[] args) {
        System.out.println("Creating test document...");
        Document document = new Document()
                .append("_id", "12345")
                .append("name", "Test Document")
                .append("timestamp", new Date())
                .append("nested", new Document()
                        .append("field1", "value1")
                        .append("field2", 42)
                        .append("array", Arrays.asList(1, 2, 3, 4, 5))
                );
        
        System.out.println("Original document as JSON:");
        String jsonStr = document.toJson();
        System.out.println(jsonStr);
        System.out.println();
        
        // Create BSON serializer
        BSON bson = new BSON();
        Serde.Serializer serializer = bson.serializer("test-topic", Serde.Target.VALUE);
        
        // Serialize to BSON binary
        System.out.println("Serializing to BSON binary...");
        byte[] bsonData = serializer.serialize(jsonStr);
        System.out.println("BSON binary size: " + bsonData.length + " bytes");
        System.out.println();
        
        // Deserialize from BSON binary
        System.out.println("Deserializing from BSON binary...");
        Serde.Deserializer deserializer = bson.deserializer("test-topic", Serde.Target.VALUE);
        DeserializeResult result = deserializer.deserialize(null, bsonData);
        
        System.out.println("Deserialized document as JSON:");
        System.out.println(result.getResult());
    }
}
EOF

# Compile and run the test program
echo "Compiling and running the test program..."
javac -cp target/kafkaui-bson-serde-1.0-SNAPSHOT.jar BsonTest.java
java -cp .:target/kafkaui-bson-serde-1.0-SNAPSHOT.jar BsonTest

# Clean up
rm -f BsonTest.java BsonTest.class
