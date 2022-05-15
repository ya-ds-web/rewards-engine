package com.yaweb.rewardsengine.models;

import com.yaweb.rewardsengine.interfaces.Campaign;
import com.yaweb.rewardsengine.interfaces.Reward;

import java.util.Collections;
import java.util.List;

/**
 * Created by ya-ds on 10 May 2022
 */

public class PersonalizedRewardableAction {

  public PersonalizedRewardableAction(PersonalizedAction action, Campaign campaign) {
    this.action = action;
    this.campaign = campaign;
  }

  private final PersonalizedAction action;
  private final Campaign campaign;

  public List<Reward> createRewards() {
    return Collections.singletonList(new Reward() {
      private static final int test = 1233333;
    });
  }

}
