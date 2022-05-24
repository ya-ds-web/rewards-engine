package com.yaweb.rewardsengine.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.kafka.common.serialization.Deserializer;
import org.springframework.kafka.support.serializer.DeserializationException;

import java.io.IOException;
import java.util.Map;

import static com.yaweb.rewardsengine.exceptions.ExceptionMessagesStringsFormats.MESSAGE_TO_OBJECT_DESERIALIZATION_EXCEPTION;

public class GenericMappingDeserializer<T> implements Deserializer<T> {

  private final ObjectMapper mapper = new ObjectMapper();

  final SimpleModule simpleModule = new SimpleModule();
  Class<T> mainClass;

  public GenericMappingDeserializer(Class<T> mainClass, Map<Class<?>, Class<?>> subClasses) {
    this.mainClass = mainClass;
    if (subClasses != null) {
      for (Class clazz : subClasses.keySet()) {
        simpleModule.addAbstractTypeMapping(clazz, subClasses.get(clazz));
      }
    }
  }

  @Override
  public T deserialize(String topic, byte[] data) {
    if (data == null) {
      return null;
    }
    try {
      mapper.registerModule(simpleModule);
      return mapper.readValue(data, mainClass);
    } catch (IOException e) {
      throw new DeserializationException(
          String.format(MESSAGE_TO_OBJECT_DESERIALIZATION_EXCEPTION,
              mainClass),
          data, false, e);
    }
  }
}
