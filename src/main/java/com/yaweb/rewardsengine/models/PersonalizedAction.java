package com.yaweb.rewardsengine.models;

import com.yaweb.rewardsengine.interfaces.Action;
import com.yaweb.rewardsengine.interfaces.Actor;

/**
 * Created by ya-ds on 12 May 2022
 */

public class PersonalizedAction {
  Action action;
  Actor actor;

  public PersonalizedAction(Action action, Actor actor) {
    this.action = action;
    this.actor = actor;
  }

}
