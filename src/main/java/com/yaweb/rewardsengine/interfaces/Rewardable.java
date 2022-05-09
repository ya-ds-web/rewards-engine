package com.yaweb.rewardsengine.interfaces;

import com.yaweb.rewardsengine.models.Reward;

import java.util.List;

public interface Rewardable {
  List<Reward> toRewards();
}