package com.yaweb.rewardsengine.models.rewardable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ya-ds on April, 2022
 */

public class Bill {
  long id;
  String type;
  List<Payment> payments = new ArrayList<>();
}
