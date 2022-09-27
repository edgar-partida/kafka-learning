package com.griddynamics.kafka;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemoWithCallback {

    private static final Logger log = LoggerFactory.getLogger(ProducerDemoWithCallback.class.getName());


    public static void main(String[] args) {   
        System.out.println("I am a Kafka producer with callback!!");
        // create Producer Properties
        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"127.0.0.1:9092");
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());

        // create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        // create a producer record
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>("demo-java","hello world with callback!!");

        // send data - asynchronous operation
        producer.send(producerRecord, new Callback() {
            @Override
            public void onCompletion(RecordMetadata metadata, Exception exception) {
                // executes every time a record is successfully sent or an exception is thrown
                if(exception == null) {
                    log.info("Received new metadata/ \n" +
                            "Topic: "+metadata.topic() + "\n" +
                            "Partition: "+metadata.partition() + "\n" +
                            "Offset: " + metadata.offset() + "\n" +
                            "Timestamp: " + metadata.timestamp());
                }
            }
        });

        // flush data - asynchronous
        producer.flush();

        // close producer
        producer.close();
    }

}
