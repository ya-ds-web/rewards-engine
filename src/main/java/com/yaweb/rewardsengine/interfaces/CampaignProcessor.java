package com.yaweb.rewardsengine.interfaces;

import org.apache.kafka.streams.kstream.KStream;

/**
 * Created by ya-ds on 10 May 2022
 */

public interface CampaignProcessor {
  KStream<String, Campaign> getStream();
}
