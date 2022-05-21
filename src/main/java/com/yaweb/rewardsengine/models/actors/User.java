package com.yaweb.rewardsengine.models.actors;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yaweb.rewardsengine.interfaces.Actor;

/**
 * Created by ya-ds on April, 2022
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public record User(
    String id,
    long inserted,
    boolean isSpecial,
    String mail
) implements Actor {
  @Override
  public String getActionMappingKey() {
    return id;
  }
}
