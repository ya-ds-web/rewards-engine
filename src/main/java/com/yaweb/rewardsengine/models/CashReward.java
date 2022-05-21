package com.yaweb.rewardsengine.models;

import com.yaweb.rewardsengine.interfaces.Reward;

/**
 * Created by ya-ds on 18 May 2022
 */

public record CashReward(String userId, String userEmail, String currency, double amount) implements Reward {
}
