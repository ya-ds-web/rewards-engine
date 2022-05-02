package com.yaweb.rewardsengine.models.rewardable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by ya-ds on April, 2022
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public record Payment(
    long id,
    long billId,
    long userId,
    long insertionTime,
    long receiverId,
    double amount,
    double taxAmount,
    String type,
    String status,
    String amountCurrency,
    String taxAmountCurrency
) {
}
