package com.yeqown.kafkaui.serde.bson;

import com.provectus.kafka.ui.serde.api.Serde;

/**
 * This class is used to understand the Serde API interface.
 */
public class SerdeApiTest {
    public static void main(String[] args) {
        // Print the methods of the Serde interface
        System.out.println("Methods of the Serde interface:");
        for (java.lang.reflect.Method method : Serde.class.getMethods()) {
            System.out.println(method);
        }
    }
}
