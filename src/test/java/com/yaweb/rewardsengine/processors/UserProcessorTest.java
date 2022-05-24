package com.yaweb.rewardsengine.processors;

import com.yaweb.rewardsengine.common.DefaultStreamProperties;
import com.yaweb.rewardsengine.interfaces.Actor;
import com.yaweb.rewardsengine.models.TableChange;
import com.yaweb.rewardsengine.models.actors.User;
import com.yaweb.rewardsengine.serialization.GenericObjectDeserializer;
import com.yaweb.rewardsengine.serialization.TableChangeDeserializer;
import com.yaweb.rewardsengine.serialization.TableChangeSerializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KeyValue;
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
 * Created by ya-ds on 23 May 2022
 */

@ExtendWith(SpringExtension.class)
class UserProcessorTest {

  @Mock
  KafkaProperties kafkaProperties;
  @Mock
  KafkaProperties.Streams streams;

  private final TableChangeSerializer<User> serializer = new TableChangeSerializer<>();
  private final TableChangeDeserializer<User> deserializer = new TableChangeDeserializer<>(User.class);
  private final GenericObjectDeserializer<Actor> actorDeserializer = new GenericObjectDeserializer<>(User.class);
  private final Serde<TableChange<User>> userChangesSerde = Serdes.serdeFrom(serializer, deserializer);

  @Test
  void getStream() {
    when(kafkaProperties.getStreams()).thenReturn(streams);
    when(streams.getProperties()).thenReturn(Collections.singletonMap("users-table-changes-topic", "input-topic-users"));
    StreamsBuilder streamsBuilder = new StreamsBuilder();
    KStream<String, TableChange<User>> changes = streamsBuilder.stream("input-topic-users",
        Consumed.with(Serdes.String(), userChangesSerde));
    var processor = new UserProcessor(kafkaProperties, streamsBuilder);
    KStream stream = processor.getStream();
    stream.to("user-processing-output");
    var topology = streamsBuilder.build();
    try (TopologyTestDriver topologyTestDriver = new TopologyTestDriver(topology, DefaultStreamProperties.getDefaultProps())) {
      TestInputTopic<String, TableChange<User>> inputTopic = topologyTestDriver
          .createInputTopic("input-topic-users", new StringSerializer(), serializer);
      inputTopic.pipeInput(
          new TableChange<>(
              "users",
              "U",
              new User("123", 9L, true, "test@test.test"),
              new User("123", 9L, true, "test@test.mail")
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
      TestOutputTopic<String, Actor> outputTopic = topologyTestDriver.createOutputTopic("user-processing-output",
          new StringDeserializer(), actorDeserializer);
      assertThat(outputTopic.readKeyValue(), equalTo(new KeyValue<>("123", new User("123", 9L, true, "test@test.mail"))));
    }
  }
}