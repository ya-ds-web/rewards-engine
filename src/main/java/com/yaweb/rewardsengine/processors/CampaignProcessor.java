package com.yaweb.rewardsengine.processors;

import com.yaweb.rewardsengine.models.TableChange;
import com.yaweb.rewardsengine.models.rewardable.Payment;
import com.yaweb.rewardsengine.serialization.TableChangeDeserializer;
import com.yaweb.rewardsengine.serialization.TableChangeSerializer;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CampaignProcessor {

  private static final Serde<String> STRING_SERDE = Serdes.String();
  //private static final Serde<Message> MESSAGE_SERDE = Serdes.serdeFrom(new MessageSerializer(),
  //    new MessageDeserializer());

  @Autowired
  void buildPipeline(StreamsBuilder streamsBuilder) {

    var paymentsTableChanges = new TableChangeDeserializer();
    paymentsTableChanges.configure(Payment.class);

    Serde<TableChange<Payment>> paymentsSerde = Serdes.serdeFrom(new TableChangeSerializer(),
        paymentsTableChanges);

    KStream<String, TableChange<Payment>> messageStream = streamsBuilder
        .stream("test", Consumed.with(STRING_SERDE, paymentsSerde));
    messageStream.peek((key, value) -> System.out.println(value.before().type() + " ======"));
    //messageStream.print(Printed.<String, TableChange<Payment>>toSysOut().withLabel("test"));

    //KStream<String, Message> messageStream = streamsBuilder
    //    .stream("test", Consumed.with(STRING_SERDE, MESSAGE_SERDE));
    //
    //messageStream.groupBy(
    //    (key, value) ->String.valueOf(key),
    //    Grouped.with(
    //        STRING_SERDE,
    //        MESSAGE_SERDE)
    //).aggregate(ArrayList::new, (key, value, aggregate) -> {
    //          aggregate.add(value);
    //          return aggregate;
    //        }, Materialized.<String, ArrayList, KeyValueStore<Bytes, byte[]>>as("yani-test11")
    //        .withKeySerde(STRING_SERDE).withValueSerde(Serdes.ListSerde(ArrayList.class, MESSAGE_SERDE)))
    //.toStream().peek((key, value) -> {
    //  System.out.println("-----------------" + key);
    //  System.out.println("-----------------" + value.toString());
    //});

    //;
    //.toStream()
    //.print(Printed.<String, ArrayList<Object>>toSysOut().withLabel("test"));
    //

    //.count().toStream().print(Printed.<String, Long>toSysOut().withLabel("test"));
    //     .count().toStream()
    //.foreach((key, value) -> {
    //  System.out.println("-----------------" + key);
    //  System.out.println("-----------------" + value.toString());
    //})

    //
    //messageStream.foreach((key, value) -> {
    //  System.out.println(key);
    //  System.out.println(value.toString());
    //});
    //KTable<String, Long> wordCounts = messageStream
    //    .mapValues((ValueMapper<String, String>) String::toLowerCase)
    //    .flatMapValues(value -> Arrays.asList(value.split("\\W+")))
    //    .groupBy((key, word) -> word, Grouped.with(STRING_SERDE, STRING_SERDE))
    //    .count();

    //wordCounts.toStream().to("output-topic");
    //System.out.println("test-11");
  }

}
