package com.yaweb.springstreamsexample.processors;

import org.springframework.context.annotation.Configuration;

@Configuration
public class Config {
  //@Bean
  //public KafkaStreams kafkaStreams(KafkaProperties kafkaProperties,
  //    @Value("${spring.application.name}")
  //        String appName) {
  //  final Properties props = new Properties();
  //
  //  // inject SSL related properties
  //  props.putAll(kafkaProperties.getSsl().buildProperties());
  //  props.putAll(kafkaProperties.getProperties());
  //  // stream config centric ones
  //  props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
  //  props.put(StreamsConfig.APPLICATION_ID_CONFIG, appName);
  //  props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
  //  props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class);
  //  props.put(StreamsConfig.STATE_DIR_CONFIG, "data");
  //  props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
  //      "org.apache.kafka.streams.errors.LogAndContinueExceptionHandler");
  //  // others
  //  props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, JsonNode.class);
  //
  //  final KafkaStreams kafkaStreams = new KafkaStreams(kafkaStreamTopology(), props);
  //  kafkaStreams.start();
  //
  //  return kafkaStreams;
  //}
  //
  //@Bean
  //public Topology kafkaStreamTopology() {
  //  final StreamsBuilder streamsBuilder = new StreamsBuilder();
  //
  //  Serde<String> STRING_SERDE = Serdes.String();
  //  Serde<Message> MESSAGE_SERDE = Serdes.serdeFrom(new MessageSerializer(),
  //      new MessageDeserializer());
  //
  //  KStream<String, Message> messageStream = streamsBuilder
  //      .stream("testtopic", Consumed.with(STRING_SERDE, MESSAGE_SERDE));
  //
  //  messageStream.groupBy(
  //      (key, value) -> String.valueOf(value.id()), Grouped.with(STRING_SERDE, MESSAGE_SERDE))
  //      .count().toStream().foreach((key, value) -> {
  //        System.out.println(key);
  //        System.out.println(value.toString());
  //      });
  //
  //  //messageStream.foreach((key, value) -> {
  //  //  System.out.println(key);
  //  //  System.out.println(value.toString());
  //  //});
  //
  //  //KTable<String, Long> wordCounts = messageStream
  //  //    .mapValues((ValueMapper<String, String>) String::toLowerCase)
  //  //    .flatMapValues(value -> Arrays.asList(value.split("\\W+")))
  //  //    .groupBy((key, word) -> word, Grouped.with(STRING_SERDE, STRING_SERDE))
  //  //    .count();
  //
  //  //wordCounts.toStream().to("output-topic");
  //  //System.out.println("test-11");
  //
  //  return streamsBuilder.build();
  //}
}
