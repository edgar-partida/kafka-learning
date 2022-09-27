package com.griddynamics.opensearch;

import com.google.gson.JsonParser;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.opensearch.action.index.IndexRequest;
import org.opensearch.action.index.IndexResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestClient;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.client.indices.CreateIndexRequest;
import org.opensearch.client.indices.GetIndexRequest;
import org.opensearch.common.xcontent.XContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class OpenSearchConsumer {

    public static RestHighLevelClient createOpenSearchClient() {
        String connString = "http://localhost:9200";
//        String connString = "https://c9p5mwld41:45zeygn9hy@kafka-course-2322630105.eu-west-1.bonsaisearch.net:443";

        // we build a URI from the connection string
        RestHighLevelClient restHighLevelClient;
        URI connUri = URI.create(connString);
        // extract login information if it exists
        String userInfo = connUri.getUserInfo();

        if (userInfo == null) {
            // REST client without security
            restHighLevelClient = new RestHighLevelClient(RestClient.builder(new HttpHost(connUri.getHost(), connUri.getPort(), "http")));

        } else {
            // REST client with security
            String[] auth = userInfo.split(":");

            CredentialsProvider cp = new BasicCredentialsProvider();
            cp.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(auth[0], auth[1]));

            restHighLevelClient = new RestHighLevelClient(
                    RestClient.builder(new HttpHost(connUri.getHost(), connUri.getPort(), connUri.getScheme()))
                            .setHttpClientConfigCallback(
                                    httpAsyncClientBuilder -> httpAsyncClientBuilder.setDefaultCredentialsProvider(cp)
                                            .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())));


        }

        return restHighLevelClient;
    }

    private static KafkaConsumer<String, String> createKafkaConsumer() {
        String bootstrapServer = "127.0.0.1:9092";
        String groupId = "consumer-opensearch-demo";

        Properties props = new Properties();
        props.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrapServer);
        props.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.setProperty(ConsumerConfig.GROUP_ID_CONFIG,groupId);
        props.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,"latest");

        return new KafkaConsumer<>(props);
    }

    public static void main(String[] args) {
        final String INDEX_NAME = "wikimedia";

        Logger log = LoggerFactory.getLogger(OpenSearchConsumer.class);

        RestHighLevelClient openSearchClient = createOpenSearchClient();

        KafkaConsumer<String, String> consumer = createKafkaConsumer();

        try(openSearchClient; consumer) {
            boolean indexExists = openSearchClient.indices().exists(new GetIndexRequest(INDEX_NAME),RequestOptions.DEFAULT);
            if(indexExists) {
                log.info("The wikimedia index already exists");
            } else {
                CreateIndexRequest indexRequest = new CreateIndexRequest("wikimedia");
                openSearchClient.indices().create(indexRequest,RequestOptions.DEFAULT);
                log.info("The wikimedia index was created correctly");
            }

            consumer.subscribe(Collections.singleton("wikimedia.recentchange"));

            while(true) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(3000));
                log.info("Received " + records.count() + " record(s)" );

                for(ConsumerRecord<String, String> record : records) {

                    // Strategy 1
                    // Define an ID using kafka records coordinates
                    String id = record.topic() + "_" + record.partition() + "_" + record.offset();

                    // Strategy 2
                    // Extract the id from the JSON value
                    String generatedId = extractId(record.value());

                    IndexRequest indexRequest = new IndexRequest(INDEX_NAME)
                            .source(record.value(),XContentType.JSON)
                            .id(generatedId);

                    IndexResponse response = openSearchClient.index(indexRequest, RequestOptions.DEFAULT);
                    log.info("Record added to openSearch with ID: "+response.getId());
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String extractId(String json) {
          return JsonParser.parseString(json)
                  .getAsJsonObject()
                  .get("meta")
                  .getAsJsonObject()
                  .get("id").getAsString();
    }
}
