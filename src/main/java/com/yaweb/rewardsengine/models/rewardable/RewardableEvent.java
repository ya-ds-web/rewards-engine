package com.yaweb.rewardsengine.models.rewardable;

import com.yaweb.rewardsengine.interfaces.Event;
import com.yaweb.rewardsengine.interfaces.Rewardable;
import com.yaweb.rewardsengine.interfaces.RewardsContainer;

import java.util.List;

/**
 * Created by ya-ds on 02 May 2022
 */

public class RewardableEvent implements Rewardable, Event {

  private List<RewardsContainer> rewardsContainers;

  @Override
  public List toRewards() {
    return null;
  }

  @Override
  public boolean isProcessable() {
    return false;
  }
}
