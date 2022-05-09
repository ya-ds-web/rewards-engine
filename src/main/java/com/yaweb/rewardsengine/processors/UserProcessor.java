package com.yaweb.rewardsengine.processors;

import com.yaweb.rewardsengine.models.TableChange;
import com.yaweb.rewardsengine.models.user.User;
import com.yaweb.rewardsengine.serialization.GenericObjectDeserializer;
import com.yaweb.rewardsengine.serialization.GenericObjectSerializer;
import com.yaweb.rewardsengine.serialization.TableChangeDeserializer;
import com.yaweb.rewardsengine.serialization.TableChangeSerializer;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.stereotype.Component;

/**
 * Created by ya-ds on 07 May 2022
 */

@Component
public class UserProcessor {

  private final KafkaProperties kafkaProperties;
  private final StreamsBuilder streamsBuilder;
  private final TableChangeSerializer<User> serializer = new TableChangeSerializer<>();
  private final GenericObjectSerializer<User> userSerializer = new GenericObjectSerializer<>();
  private final TableChangeDeserializer<User> deserializer = new TableChangeDeserializer<>(User.class);
  private final GenericObjectDeserializer<User> userDeserializer = new GenericObjectDeserializer<>(User.class);
  private final Serde<TableChange<User>> userChangesSerde;
  private final Serde<User> userSerde;

  public UserProcessor(KafkaProperties kafkaProperties, StreamsBuilder streamsBuilder) {
    this.kafkaProperties = kafkaProperties;
    this.streamsBuilder = streamsBuilder;
    this.userSerde = Serdes.serdeFrom(userSerializer, userDeserializer);
    this.userChangesSerde = Serdes.serdeFrom(serializer, deserializer);
  }

  KTable<String, User> getUsers() {
    KStream<String, TableChange<User>> stream = streamsBuilder.stream(
        kafkaProperties.getStreams().getProperties().get("users-table-changes-topic"),
        Consumed.with(Serdes.String(), userChangesSerde));
    return stream.filter((String key, TableChange<User> value) ->
            value != null && value.after() != null)
        .mapValues(TableChange::after)
        .groupBy((key, value) -> String.valueOf(value.id()), Grouped.with(Serdes.String(), userSerde))
        .reduce((value1, value2) -> value1.inserted() > value2.inserted() ? value1 : value2);
  }
}
