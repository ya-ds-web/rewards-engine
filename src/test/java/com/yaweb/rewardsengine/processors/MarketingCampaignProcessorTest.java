package com.yaweb.rewardsengine.processors;

import com.yaweb.rewardsengine.common.DefaultStreamProperties;
import com.yaweb.rewardsengine.interfaces.Campaign;
import com.yaweb.rewardsengine.models.campaigns.MarketingCampaign;
import com.yaweb.rewardsengine.serialization.GenericObjectDeserializer;
import com.yaweb.rewardsengine.serialization.GenericObjectSerializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.TestInputTopic;
import org.apache.kafka.streams.TestOutputTopic;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

/**
 * Created by ya-ds on 24 May 2022
 */
@ExtendWith(SpringExtension.class)
class MarketingCampaignProcessorTest {

  @Mock
  KafkaProperties kafkaProperties;
  @Mock
  KafkaProperties.Streams streams;

  private final GenericObjectDeserializer<MarketingCampaign> deserializer = new GenericObjectDeserializer(MarketingCampaign.class);
  private final GenericObjectSerializer<MarketingCampaign> serializer = new GenericObjectSerializer<>();
  private final GenericObjectDeserializer<Campaign> campaignDeserializer = new GenericObjectDeserializer<>(MarketingCampaign.class);
  private final Serde<MarketingCampaign> campaignSerde = Serdes.serdeFrom(serializer, deserializer);

  @Test
  void getStream() {
    var campaign = new MarketingCampaign(10L, 12L, 11L, 12L, "marketing", "fee", "fixed");
    when(kafkaProperties.getStreams()).thenReturn(streams);
    when(streams.getProperties()).thenReturn(Collections.singletonMap("campaign-topic", "input-topic-campaign"));
    StreamsBuilder streamsBuilder = new StreamsBuilder();
    KStream<String, MarketingCampaign> campaigns = streamsBuilder.stream("input-topic-campaign",
        Consumed.with(Serdes.String(), campaignSerde));
    var processor = new MarketingCampaignProcessor(kafkaProperties, streamsBuilder);
    KStream stream = processor.getStream();
    stream.to("campaign-processing-output");
    var topology = streamsBuilder.build();
    try (TopologyTestDriver topologyTestDriver = new TopologyTestDriver(topology, DefaultStreamProperties.getDefaultProps())) {
      TestInputTopic<String, MarketingCampaign> inputTopic = topologyTestDriver
          .createInputTopic("input-topic-campaign", new StringSerializer(), serializer);
      inputTopic.pipeInput("123", campaign);
      TestOutputTopic<String, Campaign> outputTopic = topologyTestDriver.createOutputTopic("campaign-processing-output",
          new StringDeserializer(), campaignDeserializer);
      assertThat(outputTopic.readKeyValue(), equalTo(new KeyValue<>("marketing", campaign)));
    }
  }
}