package com.yaweb.rewardsengine.models;

import com.yaweb.rewardsengine.interfaces.Campaign;
import com.yaweb.rewardsengine.interfaces.Reward;

import java.util.Collections;
import java.util.List;

/**
 * Created by ya-ds on 10 May 2022
 */

public class PersonalizedRewardableAction {
  private PersonalizedAction action;
  private Campaign campaign;

  public PersonalizedRewardableAction() {
    super();
  }

  public PersonalizedRewardableAction(PersonalizedAction action, Campaign campaign) {
    this.action = action;
    this.campaign = campaign;
  }

  public PersonalizedAction getAction() {
    return action;
  }

  public void setAction(PersonalizedAction action) {
    this.action = action;
  }

  public Campaign getCampaign() {
    return campaign;
  }

  public void setCampaign(Campaign campaign) {
    this.campaign = campaign;
  }

  public List<Reward> createRewards() {
    return Collections.singletonList(new CashReward("123", "ya@ya.ya", "USD", 10));
  }

}
