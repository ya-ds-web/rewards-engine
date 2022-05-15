package com.yaweb.rewardsengine.models.campaigns;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.yaweb.rewardsengine.interfaces.Campaign;

/**
 * Created by ya-ds on April, 2022
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public record MarketingCampaign(
    Long id,
    Long lastChangeTimeInstance,
    Long from,
    Long to,
    String type,
    String calculationBase,
    String calculationType
) implements Campaign {
}
