package com.yaweb.rewardsengine.models.rewardable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
) {
}
