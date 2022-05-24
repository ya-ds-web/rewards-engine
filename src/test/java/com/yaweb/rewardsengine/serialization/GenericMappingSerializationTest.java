package com.yaweb.rewardsengine.serialization;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaweb.rewardsengine.interfaces.Action;
import com.yaweb.rewardsengine.interfaces.Actor;
import com.yaweb.rewardsengine.interfaces.Campaign;
import com.yaweb.rewardsengine.models.PersonalizedAction;
import com.yaweb.rewardsengine.models.PersonalizedRewardableAction;
import com.yaweb.rewardsengine.models.actions.Bill;
import com.yaweb.rewardsengine.models.actions.Payment;
import com.yaweb.rewardsengine.models.actors.User;
import com.yaweb.rewardsengine.models.campaigns.MarketingCampaign;
import org.apache.kafka.common.errors.SerializationException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.kafka.support.serializer.DeserializationException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.Assert;

import java.io.IOException;
import java.util.Map;

import static com.yaweb.rewardsengine.exceptions.ExceptionMessagesStringsFormats.MESSAGE_TO_OBJECT_DESERIALIZATION_EXCEPTION;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

/**
 * Created by ya-ds on 23 May 2022
 */

class GenericMappingSerializationTest {

  private final Map rewardableSubtypes = Map.of(Action.class, Payment.class, Actor.class, User.class, Campaign.class, MarketingCampaign.class);

  private final GenericMappingSerializer<PersonalizedRewardableAction> serializer =
      new GenericMappingSerializer<>(rewardableSubtypes);

  private final GenericMappingDeserializer<PersonalizedRewardableAction> deserializer =
      new GenericMappingDeserializer<>(PersonalizedRewardableAction.class,
          rewardableSubtypes);

  @Test
  void deserializeNull() {
    var object = deserializer.deserialize("test", null);
    assertNull(object, "Deserialization of null should return null!");
  }

  @Test
  void deserializeException() throws IOException {
    var newDeserializer = new GenericMappingDeserializer(User.class, rewardableSubtypes);
    var objectMapperMock = Mockito.mock(ObjectMapper.class);
    Mockito.when(objectMapperMock.readValue(any(byte[].class), eq(User.class))).thenThrow(new IOException());
    ReflectionTestUtils.setField(newDeserializer, "mapper", objectMapperMock);
    var thrown = Assertions.assertThrows(DeserializationException.class, () ->
        newDeserializer.deserialize("test", new byte[1]));
    var expectedErrorMessage =
        String.format(MESSAGE_TO_OBJECT_DESERIALIZATION_EXCEPTION, "class " + User.class.getName());
    Assert.isTrue(thrown.getMessage()
            .contains(expectedErrorMessage)
        , "Incorrect exception was thrown for failed deserialization! \n  expected: " + expectedErrorMessage + "\n  "
            + "actual: " + thrown.getMessage());
  }

  @Test
  void serializeException() throws IOException {
    var campaign = new MarketingCampaign(10L, 10L, 10L, 10L, "test", "test", "test");
    var newSerializer = new GenericMappingSerializer(rewardableSubtypes);
    var objectMapperMock = Mockito.mock(ObjectMapper.class);
    Mockito.when(objectMapperMock.writeValueAsBytes(eq(campaign))).thenThrow(
        JsonMappingException.fromUnexpectedIOE(new IOException())
    );
    ReflectionTestUtils.setField(newSerializer, "mapper", objectMapperMock);
    var thrown = Assertions.assertThrows(SerializationException.class, () ->
        newSerializer.serialize("test", campaign));
    var expectedErrorMessage =
        String.format(String.format("Enable to serialize object: %s", campaign));
    Assert.isTrue(thrown.getMessage()
            .contains(expectedErrorMessage)
        , "Incorrect exception was thrown for failed serialization! \n  expected: " + expectedErrorMessage + "\n  "
            + "actual: " + thrown.getMessage());
  }

  @Test
  void deserializationTest() {
    PersonalizedRewardableAction action = new PersonalizedRewardableAction(
        new PersonalizedAction(
            new Bill(), new User("123", 10L, false, "mail@mail.bg")),
        new MarketingCampaign(10L, 10L, 10L, 15L, "test", "base", "fee")
    );
    var serializedAction = serializer.serialize("test", action);
    var deserializeAction = deserializer.deserialize("test", serializedAction);

    assertEquals(action.getAction().getActor(), deserializeAction.getAction().getActor()
        , "Deserialized object" + deserializeAction + "\n  "
            + "did not match original object : " + action);
  }

}