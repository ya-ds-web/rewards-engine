package com.yaweb.rewardsengine.models.actions;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yaweb.rewardsengine.interfaces.Action;

/**
 * Created by ya-ds on April, 2022
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public record Payment(
    Long id,
    Long billId,
    Long userId,
    Long insertionTime,
    Long receiverId,
    Double amount,
    Double taxAmount,
    String type,
    String status,
    String amountCurrency,
    String taxAmountCurrency,
    Boolean isFirst
) implements Action {

  @Override
  public String getActorMappingKey() {
    return String.valueOf(userId);
  }
}
