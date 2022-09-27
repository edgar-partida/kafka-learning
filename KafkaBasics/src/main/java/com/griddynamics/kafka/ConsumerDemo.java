package com.griddynamics.kafka;

import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerDemo {
    private static Logger log = LoggerFactory.getLogger(ConsumerDemo.class.getName());

    public static void main(String[] args) {
        log.info("This is a Kafka consumer");
        String bootstrapServer = "127.0.0.1:9092";
        String groupId = "my-second-application";
        String topic = "demo-java";

        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapServer);
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG,groupId);
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"earliest");

        Consumer<String, String> consumer = new KafkaConsumer<String, String>(props);

        // Getting the reference to the current Thread
        final Thread mainThread = Thread.currentThread();

        // Adding the Shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                log.info("Shutdown detected!!!");
                consumer.wakeup();
                // join the main Thread to allow the execution of the code in the main thread
                try {
                    mainThread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        try {
            consumer.subscribe(Arrays.asList(topic));

            while(true) {
                log.info("Polling...");
                ConsumerRecords<String,String> records =
                        consumer.poll(Duration.ofMillis(1000));
                for(ConsumerRecord<String, String> record : records) {
                    log.info("Key: "+ record.key()+" Value: "+record.value());
                    log.info("Partition: "+ record.partition()+" Offset: "+record.offset());
                }
            }
        } catch(WakeupException e) {
            log.info("Wake up exception!");
            // we ignore this as this is an expected exception when closing a consumer
        } catch( Exception e) {
            log.error("Unexpected exception");
        } finally {
            consumer.close();
        }
    }
}
