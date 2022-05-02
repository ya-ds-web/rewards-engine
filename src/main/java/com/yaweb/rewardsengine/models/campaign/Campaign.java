package com.yaweb.rewardsengine.models.campaign;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yaweb.rewardsengine.interfaces.RewardsContainer;

/**
 * Created by ya-ds on April, 2022
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public record Campaign(
    Long id,
    Long from,
    Long to,
    String type,
    String calculationBase,
    String calculationType
) implements RewardsContainer {
}
