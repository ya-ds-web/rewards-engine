package com.yaweb.rewardsengine.models.rewardable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ya-ds on April, 2022
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class Bill {
  private int state;
  private long id;
  private long initiatorId;
  private String type;

  private final List<Payment> payments = new ArrayList<>();

  public List<Payment> getPayments() {
    return payments;
  }

  public void addPayment(Payment payment) {
    payments.add(payment);
  }

  public boolean isCompleted() {
    return state == 3;
  }

  public long getInitiatorId() {
    return initiatorId;
  }

  public void setInitiatorId(long initiatorId) {
    this.initiatorId = initiatorId;
  }
}
