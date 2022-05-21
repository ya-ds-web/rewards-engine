package com.yaweb.rewardsengine.serialization;

/**
 * Created by ya-ds on 30 April 2022
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class GenericMappingSerializer<T> implements Serializer<T> {

  final SimpleModule simpleModule = new SimpleModule();
  ObjectMapper mapper = new ObjectMapper();

  public GenericMappingSerializer(Map<Class, Class> subClasses) {
    if (subClasses != null) {
      for (Class clazz : subClasses.keySet()) {
        simpleModule.addAbstractTypeMapping(clazz, subClasses.get(clazz));
      }
    }
  }

  @Override
  public byte[] serialize(String topic, T data) {
    try {
      mapper.registerModule(simpleModule);
      return mapper.writeValueAsBytes(data);
    } catch (JsonProcessingException e) {
      throw new SerializationException(String.format("Enable to serialize object: %s", data), e);
    }
  }

}
