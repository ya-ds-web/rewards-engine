package com.yaweb.rewardsengine.serialization;

/**
 * Created by ya-ds on 30 April 2022
 */

import static com.yaweb.rewardsengine.exceptions.ExceptionMessagesStringsFormats.MESSAGE_TO_OBJECT_DESERIALIZATION_EXCEPTION;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.kafka.support.serializer.DeserializationException;

import java.io.IOException;

public class GenericObjectDeserializer<T> implements Deserializer<T> {

  private final ObjectMapper mapper = new ObjectMapper();
  private final Class<T> clazz;

  public GenericObjectDeserializer(Class clazz) {
    this.clazz = clazz;
  }

  @Override
  public T deserialize(String topic, byte[] data) {
    if (data == null) {
      return null;
    }
    try {
      return mapper.readValue(data, clazz);
    } catch (IOException e) {
      throw new DeserializationException(
          String.format(MESSAGE_TO_OBJECT_DESERIALIZATION_EXCEPTION, clazz.getName()),
          data, false, e);
    }
  }
}
