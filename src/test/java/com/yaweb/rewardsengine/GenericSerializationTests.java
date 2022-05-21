package com.yaweb.rewardsengine;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaweb.rewardsengine.models.actors.User;
import com.yaweb.rewardsengine.serialization.GenericObjectDeserializer;
import com.yaweb.rewardsengine.serialization.GenericObjectSerializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;

import static com.yaweb.rewardsengine.exceptions.ExceptionMessagesStringsFormats.MESSAGE_TO_OBJECT_DESERIALIZATION_EXCEPTION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class GenericSerializationTests {

  byte[] messageBytes;

  GenericObjectDeserializer<User> deserializer = new GenericObjectDeserializer<>(User.class);
  GenericObjectSerializer<User> serializer = new GenericObjectSerializer<>();

  @BeforeEach
  void setUp() throws IOException {
    InputStream is = getClass().getClassLoader().getResourceAsStream("UserMessageSampler.json");
    messageBytes = is.readAllBytes();
  }

  @Test
  void serializationOfUserTest() {
    var user = deserializer.deserialize("test", messageBytes);
    var userSerialized = serializer.serialize("test", user);
    var newUser = deserializer.deserialize("test", userSerialized);
    assertEquals(newUser, user, "Deserialized message from file is not equal to "
        + "deserialized"
        + "-> serialized -> deserialized message!");
  }

  @Test
  void deserializeNull() {
    var user = deserializer.deserialize("test", null);
    assertNull(user, "Deserialization of null should return null!");
  }

  @Test
  void deserializationExceptionUnableToCreateObject() throws IOException {
    var newDeserializer = new GenericObjectDeserializer<>(User.class);
    var objectMapperMock = Mockito.mock(ObjectMapper.class);
    Mockito.when(objectMapperMock.readValue(any(byte[].class), eq(User.class))).thenThrow(new IOException());
    ReflectionTestUtils.setField(newDeserializer, "mapper", objectMapperMock);
    var thrown = Assertions.assertThrows(DeserializationException.class, () ->
        newDeserializer.deserialize("test", messageBytes));
    var expectedErrorMessage =
        String.format(MESSAGE_TO_OBJECT_DESERIALIZATION_EXCEPTION, User.class.getName());
    Assert.isTrue(thrown.getMessage()
            .contains(expectedErrorMessage)
        , "Incorrect exception was thrown for failed deserialization! \n  expected: " + expectedErrorMessage + "\n  "
            + "actual: " + thrown.getMessage());
  }

  @Test
  void serializationExceptionUnableToParseObject() throws IOException {
    var newSerializer = new GenericObjectSerializer<User>();
    var objectMapperMock = Mockito.mock(ObjectMapper.class);
    Mockito.when(objectMapperMock.writeValueAsBytes(any(User.class))).thenThrow(
        JsonMappingException.fromUnexpectedIOE(new IOException())
    );
    var dummyTableChange = new User("0", 0, false, null);
    ReflectionTestUtils.setField(newSerializer, "mapper", objectMapperMock);
    var thrown = Assertions.assertThrows(RuntimeException.class, () ->
        newSerializer.serialize("test", dummyTableChange));
    assertNotNull(thrown, "Serialization should throw exception on error!");
  }
}
