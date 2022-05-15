package com.yaweb.rewardsengine.processors;

import com.yaweb.rewardsengine.interfaces.Action;
import com.yaweb.rewardsengine.interfaces.ActionsProcessor;
import com.yaweb.rewardsengine.interfaces.Actor;
import com.yaweb.rewardsengine.interfaces.ActorsProcessor;
import com.yaweb.rewardsengine.interfaces.Campaign;
import com.yaweb.rewardsengine.interfaces.Reward;
import com.yaweb.rewardsengine.models.PersonalizedAction;
import com.yaweb.rewardsengine.models.PersonalizedRewardableAction;
import com.yaweb.rewardsengine.serialization.GenericObjectDeserializer;
import com.yaweb.rewardsengine.serialization.GenericObjectSerializer;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.LinkedList;
import java.util.List;

@Component
public class MainProcessor {

  private final KafkaProperties kafkaProperties;
  GenericObjectSerializer<Reward> rewardSerializer = new GenericObjectSerializer<>();
  GenericObjectDeserializer<Reward> rewardDeserializer = new GenericObjectDeserializer<>(Reward.class);
  Serde<Reward> rewardSerde = Serdes.serdeFrom(rewardSerializer, rewardDeserializer);

  @Autowired
  public MainProcessor(KafkaProperties kafkaProperties) {
    this.kafkaProperties = kafkaProperties;
  }

  @Autowired
  void buildPipeline(List<ActionsProcessor> actionsProcessors, List<ActorsProcessor> actorsProcessors,
      List<MarketingCampaignProcessor> campaignsProcessors) {

    KStream<String, Action> combinedActions = null;
    for (ActionsProcessor actionsProcessor : actionsProcessors) {
      var stream = actionsProcessor.getStream();
      if (combinedActions == null) {
        combinedActions = stream;
        continue;
      }
      combinedActions.merge(stream);
    }

    KStream<String, Actor> combinedActors = null;
    for (ActorsProcessor actorsProcessor : actorsProcessors) {
      var stream = actorsProcessor.getStream();
      if (combinedActors == null) {
        combinedActors = stream;
        continue;
      }
      combinedActors.merge(stream);
    }

    KStream<String, Campaign> combinedCampaigns = null;
    for (MarketingCampaignProcessor campaignsProcessor : campaignsProcessors) {
      var stream = campaignsProcessor.getStream();
      if (combinedCampaigns == null) {
        combinedCampaigns = stream;
        continue;
      }
      combinedCampaigns.merge(stream);
    }
    Assert.notNull(combinedActions, "There should be at leas one action processor which return stream!");
    Assert.notNull(combinedActors, "There should be at least one actor processor which return stream");
    Assert.notNull(combinedCampaigns, "There should be at least one campaign processor which return stream");
    combinedActions.peek((key, value) -> {
      System.out.println(key);
      System.out.println(value);
    });
    combinedActors.peek((key, value) -> {
      System.out.println(key);
      System.out.println(value);
    });
    combinedCampaigns.peek((key, value) -> {
      System.out.println(key);
      System.out.println(value);
    });
    KStream<String, PersonalizedRewardableAction> rewardableActions =
        combinedActions.join(combinedActors.toTable(), PersonalizedAction::new).join(combinedCampaigns.toTable(),
            PersonalizedRewardableAction::new);
    processRewardable(rewardableActions);
  }

  private void processRewardable(KStream<String, PersonalizedRewardableAction> stream) {
    stream.flatMapValues((key, value) -> {
          List<String> result = new LinkedList<>();
          value.createRewards().forEach(reward -> result.add(reward.toString()));
          return result;
        })
        .to(kafkaProperties.getStreams().getProperties().get("rewards-topic"));
  }

}
