package com.yaweb.rewardsengine.models;

import com.yaweb.rewardsengine.interfaces.Action;
import com.yaweb.rewardsengine.interfaces.Actor;

/**
 * Created by ya-ds on 12 May 2022
 */

public class PersonalizedAction {
  private Action action;
  private Actor actor;

  public PersonalizedAction() {
    super();
  }

  public PersonalizedAction(Action action, Actor actor) {
    this.action = action;
    this.actor = actor;
  }

  public Action getAction() {
    return action;
  }

  public void setAction(Action action) {
    this.action = action;
  }

  public Actor getActor() {
    return actor;
  }

  public void setActor(Actor actor) {
    this.actor = actor;
  }
}
