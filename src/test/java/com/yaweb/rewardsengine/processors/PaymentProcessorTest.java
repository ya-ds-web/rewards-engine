package com.yaweb.rewardsengine.processors;

import com.yaweb.rewardsengine.common.DefaultStreamProperties;
import com.yaweb.rewardsengine.interfaces.Action;
import com.yaweb.rewardsengine.models.TableChange;
import com.yaweb.rewardsengine.models.actions.Bill;
import com.yaweb.rewardsengine.models.actions.Payment;
import com.yaweb.rewardsengine.serialization.GenericObjectDeserializer;
import com.yaweb.rewardsengine.serialization.TableChangeDeserializer;
import com.yaweb.rewardsengine.serialization.TableChangeSerializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

/**
 * Created by ya-ds on 24 May 2022
 */

@ExtendWith(SpringExtension.class)
class PaymentProcessorTest {

  @Mock
  KafkaProperties kafkaProperties;
  @Mock
  KafkaProperties.Streams streams;

  private final TableChangeSerializer<Payment> serializer = new TableChangeSerializer<>();
  private final TableChangeDeserializer<Payment> deserializer = new TableChangeDeserializer<>(Payment.class);
  private final GenericObjectDeserializer<Action> paymentDeserializer = new GenericObjectDeserializer<>(Bill.class);
  private final Serde<TableChange<Payment>> paymentChangesSerde = Serdes.serdeFrom(serializer, deserializer);

  @Test
  void getStream() {
    Payment payment = new Payment(
        1243L,
        123L,
        124L,
        10L,
        1235L,
        Double.valueOf("10.0"),
        Double.valueOf("15.0"),
        "payment",
        "pass",
        "BGN",
        "BGN",
        true
    );
    when(kafkaProperties.getStreams()).thenReturn(streams);
    when(streams.getProperties()).thenReturn(Collections.singletonMap("payments-table-changes-topic", "input-topic-payments"));
    StreamsBuilder streamsBuilder = new StreamsBuilder();
    KStream<String, TableChange<Payment>> changes = streamsBuilder.stream("input-topic-payments",
        Consumed.with(Serdes.String(), paymentChangesSerde));
    var processor = new PaymentProcessor(kafkaProperties, streamsBuilder);
    KStream stream = processor.getStream();
    stream.to("payments-processing-output");
    var topology = streamsBuilder.build();
    try (TopologyTestDriver topologyTestDriver = new TopologyTestDriver(topology, DefaultStreamProperties.getDefaultProps())) {
      TestInputTopic<String, TableChange<Payment>> inputTopic = topologyTestDriver
          .createInputTopic("input-topic-payments", new StringSerializer(), serializer);

      inputTopic.pipeInput(
          new TableChange<>(
              "Payments",
              "U",
              payment,
              payment
          )
      );
      inputTopic.pipeInput(
          new TableChange<>(
              "users",
              "U",
              null,
              null
          )
      );

      TestOutputTopic<String, Action> outputTopic = topologyTestDriver.createOutputTopic("payments-processing-output",
          new StringDeserializer(), paymentDeserializer);
      var keyValue = outputTopic.readKeyValue();
      assertThat(keyValue.key, equalTo("124"));
    }
  }

}