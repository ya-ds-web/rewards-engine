package com.yaweb.rewardsengine.processors;

import com.yaweb.rewardsengine.interfaces.Campaign;
import com.yaweb.rewardsengine.interfaces.CampaignProcessor;
import com.yaweb.rewardsengine.models.campaigns.MarketingCampaign;
import com.yaweb.rewardsengine.serialization.GenericObjectDeserializer;
import com.yaweb.rewardsengine.serialization.GenericObjectSerializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Repartitioned;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.stereotype.Component;

@Component
public class MarketingCampaignProcessor implements CampaignProcessor {
  private final StreamsBuilder streamsBuilder;
  private final KafkaProperties kafkaProperties;
  private final GenericObjectSerializer<MarketingCampaign> marketingCampaignSerializer = new GenericObjectSerializer<>();
  private final GenericObjectSerializer<Campaign> campaignSerializer = new GenericObjectSerializer<>();
  private final GenericObjectDeserializer<MarketingCampaign> marketingCampaignDeSerializer = new GenericObjectDeserializer<>(
      MarketingCampaign.class);
  private final GenericObjectDeserializer<Campaign> campaignDeSerializer = new GenericObjectDeserializer<>(
      MarketingCampaign.class);
  private final Serde<MarketingCampaign> marketingCampaignSerde;
  private final Serde<Campaign> campaignSerde;

  public MarketingCampaignProcessor(KafkaProperties kafkaProperties, StreamsBuilder streamsBuilder) {
    this.streamsBuilder = streamsBuilder;
    this.kafkaProperties = kafkaProperties;
    this.marketingCampaignSerde = Serdes.serdeFrom(marketingCampaignSerializer, marketingCampaignDeSerializer);
    this.campaignSerde = Serdes.serdeFrom(campaignSerializer, campaignDeSerializer);
  }

  @Override
  public KStream<String, Campaign> getStream() {
    return streamsBuilder.stream(
            kafkaProperties.getStreams().getProperties().get("campaign-topic"),
            Consumed.with(Serdes.String(), marketingCampaignSerde))
        .groupBy((key, value) -> String.valueOf(value.type()), Grouped.with(Serdes.String(), marketingCampaignSerde))
        .reduce((value1, value2) -> value1.lastChangeTimeInstance() > value2.lastChangeTimeInstance() ? value1 :
            value2).mapValues(Campaign.class::cast).toStream().repartition(
            Repartitioned.with(Serdes.String(), campaignSerde));
  }

}
