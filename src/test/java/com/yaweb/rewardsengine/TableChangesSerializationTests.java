package com.yaweb.rewardsengine;

import static com.yaweb.rewardsengine.exceptions.ExceptionMessagesStringsFormats.MESSAGE_TO_TABLECHANGE_DESERIALIZATION_EXCEPTION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaweb.rewardsengine.models.TableChange;
import com.yaweb.rewardsengine.models.rewardable.Payment;
import com.yaweb.rewardsengine.serialization.TableChangeDeserializer;
import com.yaweb.rewardsengine.serialization.TableChangeSerializer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.InputStream;

class TableChangesSerializationTests {

  byte[] messageBytes;

  TableChangeDeserializer deserializer = new TableChangeDeserializer(Payment.class);
  TableChangeSerializer serializer = new TableChangeSerializer();

  @BeforeEach
  void setUp() throws IOException {
    InputStream is = getClass().getClassLoader().getResourceAsStream("PaymentsTableMessageSample.json");
    messageBytes = is.readAllBytes();
  }

  @Test
  void serializationOfPaymentsTableChangesTest() {
    var paymentChange = deserializer.deserialize("test", messageBytes);
    var paymentChangeSerialized = serializer.serialize("test", paymentChange);
    var newPaymentsChange = deserializer.deserialize("test", paymentChangeSerialized);
    assertEquals(newPaymentsChange, paymentChange, "Deserialized message from file is not equal to "
        + "deserialized"
        + "-> serialized -> deserialized message!");
  }

  @Test
  void deserializeNull() {
    var tableChange = deserializer.deserialize("test", null);
    assertNull(tableChange, "Deserialization of null should return null!");
  }

  @Test
  void deserializationExceptionUnableToCreateObject() throws IOException {
    var newDeserializer = new TableChangeDeserializer<>(Payment.class);
    var objectMapperMock = Mockito.mock(ObjectMapper.class);
    Mockito.when(objectMapperMock.readValue(any(byte[].class), any(JavaType.class))).thenThrow(new IOException());
    ReflectionTestUtils.setField(newDeserializer, "mapper", objectMapperMock);
    var thrown = Assertions.assertThrows(DeserializationException.class, () ->
        newDeserializer.deserialize("test", messageBytes));
    var expectedErrorMessage =
        String.format(MESSAGE_TO_TABLECHANGE_DESERIALIZATION_EXCEPTION, "[simple type, class " +
            Payment.class.getName() + "]");
    Assert.isTrue(thrown.getMessage()
            .contains(expectedErrorMessage)
        , "Incorrect exception was thrown for failed deserialization! \n  expected: " + expectedErrorMessage + "\n  "
            + "actual: " + thrown.getMessage());
  }

  @Test
  void serializationExceptionUnableToParseObject() throws IOException {
    var newSerializer = new TableChangeSerializer<Payment>();
    var objectMapperMock = Mockito.mock(ObjectMapper.class);
    Mockito.when(objectMapperMock.writeValueAsBytes(any(TableChange.class))).thenThrow(
        JsonMappingException.fromUnexpectedIOE(new IOException())
    );
    var dummyTableChange = new TableChange<Payment>(null, null, null, null);
    ReflectionTestUtils.setField(newSerializer, "mapper", objectMapperMock);
    var thrown = Assertions.assertThrows(RuntimeException.class, () ->
        newSerializer.serialize("test", dummyTableChange));
    assertNotNull(thrown, "Serialization should throw exception on error!");
  }
}
