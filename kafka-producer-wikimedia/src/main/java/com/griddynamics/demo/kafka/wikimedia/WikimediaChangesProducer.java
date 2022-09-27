package com.griddynamics.demo.kafka.wikimedia;

import com.launchdarkly.eventsource.EventHandler;
import com.launchdarkly.eventsource.EventSource;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;


import java.net.URI;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class WikimediaChangesProducer {

    public static void main(String[] args) throws InterruptedException {
        String bootstrapServer = "127.0.0.1:9092";
        String topic = "wikimedia.recentchange";

        System.out.println("Wikimedia Changes Producer Running...");
        // create Producer Properties
        Properties props = new Properties();
        props.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapServer);
        props.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,StringSerializer.class.getName());

        props.setProperty(ProducerConfig.BATCH_SIZE_CONFIG,Integer.toString(32*1024));
        props.setProperty(ProducerConfig.LINGER_MS_CONFIG,"5");
        props.setProperty(ProducerConfig.COMPRESSION_TYPE_CONFIG,"snappy");

        // create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        EventHandler eventHandler = new WikimediaChangeHandler(producer, topic); // TODO create class to implement the event handler
        String url = "https://stream.wikimedia.org/v2/stream/recentchange";
        EventSource.Builder builder = new EventSource.Builder(eventHandler, URI.create(url));
        EventSource eventSource = builder.build();

        //start the producer in another Thread
        eventSource.start();
        TimeUnit.MINUTES.sleep(1);

    }
}
