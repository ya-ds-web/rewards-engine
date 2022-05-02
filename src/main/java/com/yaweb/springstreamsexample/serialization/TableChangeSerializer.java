package com.yaweb.springstreamsexample.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaweb.springstreamsexample.models.TableChange;

import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

public class TableChangeSerializer<T> implements Serializer<TableChange<T>> {

  ObjectMapper mapper = new ObjectMapper();

  @Override
  public byte[] serialize(String topic, TableChange data) {
    try {
      return mapper.writeValueAsBytes(data);
    } catch (JsonProcessingException e) {
      throw new SerializationException(String.format("Enable to serialize tableChange object: %s", data), e);
    }
  }

}
