package com.yaweb.rewardsengine.processors;

import com.yaweb.rewardsengine.interfaces.Action;
import com.yaweb.rewardsengine.interfaces.ActionsProcessor;
import com.yaweb.rewardsengine.models.TableChange;
import com.yaweb.rewardsengine.models.actions.Bill;
import com.yaweb.rewardsengine.models.actions.Payment;
import com.yaweb.rewardsengine.serialization.GenericObjectDeserializer;
import com.yaweb.rewardsengine.serialization.GenericObjectSerializer;
import com.yaweb.rewardsengine.serialization.TableChangeDeserializer;
import com.yaweb.rewardsengine.serialization.TableChangeSerializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Repartitioned;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Created by ya-ds on 04 May 2022
 */

@Component
public class PaymentProcessor implements ActionsProcessor {

  private final StreamsBuilder streamsBuilder;
  private final KafkaProperties kafkaProperties;
  private final TableChangeSerializer<Payment> serializer = new TableChangeSerializer<>();
  private final GenericObjectSerializer<Bill> billSerializer = new GenericObjectSerializer<>();
  private final GenericObjectSerializer<Action> actionSerializer = new GenericObjectSerializer<>();
  private final TableChangeDeserializer<Payment> deserializer = new TableChangeDeserializer<>(Payment.class);
  private final GenericObjectDeserializer<Bill> billDeserializer = new GenericObjectDeserializer<>(Bill.class);
  private final GenericObjectDeserializer<Action> actionDeserializer = new GenericObjectDeserializer<>(Bill.class);
  private final Serde<TableChange<Payment>> paymentChangesSerde;
  private final Serde<Bill> billSerde;
  private final Serde<Action> actionSerde;

  public PaymentProcessor(KafkaProperties kafkaProperties, StreamsBuilder streamsBuilder) {
    this.streamsBuilder = streamsBuilder;
    this.kafkaProperties = kafkaProperties;
    this.paymentChangesSerde = Serdes.serdeFrom(serializer, deserializer);
    this.billSerde = Serdes.serdeFrom(billSerializer, billDeserializer);
    this.actionSerde = Serdes.serdeFrom(actionSerializer, actionDeserializer);
  }

  @Override
  public KStream<String, Action> getStream() {
    KStream<String, TableChange<Payment>> stream = streamsBuilder.stream(
        kafkaProperties.getStreams().getProperties().get("payments-table-changes-topic"),
        Consumed.with(Serdes.String(), paymentChangesSerde));
    return stream.filter((String key, TableChange<Payment> value) ->
            value != null && value.after() != null)
        .groupBy((key, value) -> {
          var before = value.before();
          var after = value.after();
          var newKey = after != null ? after.billId() : before.billId();
          return String.valueOf(newKey);
        }, Grouped.with(Serdes.String(), paymentChangesSerde))
        .aggregate(Bill::new,
            (key, value, aggregate) -> populateBill(value, aggregate),
            Materialized.<String, Bill, KeyValueStore<Bytes, byte[]>>as("bills-store")
                .withKeySerde(Serdes.String()).withValueSerde(billSerde)
        )
        .toStream()
        .flatMap((String key, Bill value) -> {
          var newKey = String.valueOf(value.getActorMappingKey());
          var newValue = ((Action) value);
          return Collections.singleton(KeyValue.pair(newKey, newValue));
        })
        .repartition(Repartitioned.with(Serdes.String(), actionSerde));
  }

  private Bill populateBill(TableChange<Payment> value, Bill aggregate) {
    if (aggregate.getInitiatorId() == 0L) {
      aggregate.setInitiatorId(value.after().userId());
    }
    var after = value.after();
    boolean isValueFirst = after.isFirst();
    if (isValueFirst) {
      aggregate.setInitiatorId(after.userId());
      aggregate.setType(after.type());
      aggregate.setState("completed".equals(after.status()) ? 3 : 2);
    }
    aggregate.addPayment(populateMissingProperties(value.before(), value.after()));
    return aggregate;
  }

  private Payment populateMissingProperties(Payment before,
      @NonNull
      Payment after) {
    return new Payment(
        (Long) getRelevantValueIfMissing(0, before, after),
        (Long) getRelevantValueIfMissing(1, before, after),
        (Long) getRelevantValueIfMissing(2, before, after),
        (Long) getRelevantValueIfMissing(3, before, after),
        (Long) getRelevantValueIfMissing(4, before, after),
        (Double) getRelevantValueIfMissing(5, before, after),
        (Double) getRelevantValueIfMissing(6, before, after),
        (String) getRelevantValueIfMissing(7, before, after),
        (String) getRelevantValueIfMissing(8, before, after),
        (String) getRelevantValueIfMissing(9, before, after),
        (String) getRelevantValueIfMissing(10, before, after),
        (Boolean) getRelevantValueIfMissing(11, before, after));
  }

  private Object getRelevantValueIfMissing(int index, Payment before, Payment after) {
    try {
      var afterValue = after.getClass().getRecordComponents()[index].getAccessor().invoke(after);
      var beforeValue = before.getClass().getRecordComponents()[index].getAccessor().invoke(before);
      return afterValue == null ? beforeValue : afterValue;
    } catch (Exception e) {
      return null;
    }
  }

}
