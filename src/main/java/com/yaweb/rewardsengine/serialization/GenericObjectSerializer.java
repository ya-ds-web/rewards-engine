package com.yaweb.rewardsengine.serialization;

/**
 * Created by ya-ds on 30 April 2022
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

public class GenericObjectSerializer<T> implements Serializer<T> {

  ObjectMapper mapper = new ObjectMapper();

  @Override
  public byte[] serialize(String topic, T data) {
    try {
      return mapper.writeValueAsBytes(data);
    } catch (JsonProcessingException e) {
      throw new SerializationException(String.format("Enable to serialize object: %s", data), e);
    }
  }

}
