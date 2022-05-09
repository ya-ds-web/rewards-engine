package com.yaweb.rewardsengine.serialization;

import static com.yaweb.rewardsengine.exceptions.ExceptionMessagesStringsFormats.MESSAGE_TO_TABLECHANGE_DESERIALIZATION_EXCEPTION;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaweb.rewardsengine.models.TableChange;

import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.kafka.support.serializer.DeserializationException;

import java.io.IOException;

public class TableChangeDeserializer<T> implements Deserializer<TableChange<T>> {

  private final ObjectMapper mapper = new ObjectMapper();
  private final JavaType type;

  public TableChangeDeserializer(Class<T> contentClass) {
    type = mapper.getTypeFactory().constructParametricType(TableChange.class, contentClass);
  }

  @Override
  public TableChange<T> deserialize(String topic, byte[] data) {
    if (data == null) {
      return null;
    }
    try {
      return mapper.readValue(data, type);
    } catch (IOException e) {
      throw new DeserializationException(
          String.format(MESSAGE_TO_TABLECHANGE_DESERIALIZATION_EXCEPTION,
              type.containedType(0).getTypeName()),
          data, false, e);
    }
  }
}
