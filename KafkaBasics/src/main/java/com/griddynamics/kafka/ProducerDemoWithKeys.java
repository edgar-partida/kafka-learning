package com.griddynamics.kafka;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class ProducerDemoWithKeys {

    private static final Logger log = LoggerFactory.getLogger(ProducerDemoWithKeys.class.getName());


    public static void main(String[] args) {   
        System.out.println("I am a Kafka producer with callback!!");
        // create Producer Properties
        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"127.0.0.1:9092");
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());

        // create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);


        String topic = "demo-java";
        String value = "Hello Kafka with not null key";
        for(int i = 0; i<10; i++) {
            // create a producer record
            String key = "id_"+i;
            ProducerRecord<String, String> producerRecord = new ProducerRecord<>(topic,key,value);

            producer.send(producerRecord, new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    // executes every time a record is successfully sent or an exception is thrown
                    if(exception == null) {
                        log.info("Received new metadata/ \n" +
                                "Topic: "+metadata.topic() + "\n" +
                                "Key: " + producerRecord.key() +"n" +
                                "Partition: "+metadata.partition() + "\n" +
                                "Offset: " + metadata.offset() + "\n" +
                                "Timestamp: " + metadata.timestamp());
                    }
                }
            });
        }
        // send data - asynchronous operation

        // flush data - asynchronous
        producer.flush();

        // close producer
        producer.close();
    }

}
