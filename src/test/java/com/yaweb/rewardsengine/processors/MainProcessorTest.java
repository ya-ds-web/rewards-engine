package com.yaweb.rewardsengine.processors;

import com.yaweb.rewardsengine.common.DefaultStreamProperties;
import com.yaweb.rewardsengine.interfaces.Action;
import com.yaweb.rewardsengine.interfaces.ActionsProcessor;
import com.yaweb.rewardsengine.interfaces.Actor;
import com.yaweb.rewardsengine.interfaces.ActorsProcessor;
import com.yaweb.rewardsengine.interfaces.Campaign;
import com.yaweb.rewardsengine.interfaces.Reward;
import com.yaweb.rewardsengine.models.CashReward;
import com.yaweb.rewardsengine.models.PersonalizedAction;
import com.yaweb.rewardsengine.models.PersonalizedRewardableAction;
import com.yaweb.rewardsengine.models.actions.Payment;
import com.yaweb.rewardsengine.models.actors.User;
import com.yaweb.rewardsengine.models.campaigns.MarketingCampaign;
import com.yaweb.rewardsengine.serialization.GenericMappingDeserializer;
import com.yaweb.rewardsengine.serialization.GenericMappingSerializer;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.lang.Double.valueOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by ya-ds on 18 May 2022
 */

@ExtendWith(SpringExtension.class)
class MainProcessorTest {

  @Mock
  KafkaProperties kafkaProperties;

  @Mock
  KafkaProperties.Streams streams;

  @Mock
  private ActionsProcessor actionsProcessor;

  @Mock
  private ActorsProcessor actorsProcessor;

  @Mock
  private MarketingCampaignProcessor marketingCampaignProcessor;

  @InjectMocks
  @Spy
  MainProcessor main;
  private final List<ActionsProcessor> actionProcessors = new ArrayList<>();

  private final List<ActorsProcessor> actorsProcessors = new ArrayList<>();

  private final List<MarketingCampaignProcessor> marketingCampaignProcessors = new ArrayList<>();


  Payment payment = new Payment(
      1L,
      123L,
      144L,
      10L,
      123L,
      valueOf("10"),
      valueOf("5"),
      "UPZ",
      "success",
      "BGN",
      "BGN",
      false
  );

  User user = new User("144",
      123L,
      true,
      "test@mail.mail");

  MarketingCampaign campaign = new MarketingCampaign(
      123L,
      1223L,
      123L,
      1234L,
      "test",
      "amount",
      "percentage");
  private final Map rewardableSubtypes = Map.of(Action.class, Payment.class, Actor.class, User.class, Campaign.class, MarketingCampaign.class);
  private final GenericObjectSerializer<Payment> paymentSerializer = new GenericObjectSerializer<>();
  private final GenericObjectDeserializer<Payment> paymentDeserializer = new GenericObjectDeserializer<>(Payment.class);
  private final GenericObjectSerializer<User> userSerializer = new GenericObjectSerializer<>();
  private final GenericObjectDeserializer<User> userDeserializer = new GenericObjectDeserializer<>(User.class);
  private final GenericObjectSerializer<MarketingCampaign> campaignGenericObjectSerializer =
      new GenericObjectSerializer<>();
  private final GenericObjectDeserializer<MarketingCampaign> campaignGenericObjectDeserializer =
      new GenericObjectDeserializer<>(MarketingCampaign.class);
  private final GenericMappingSerializer<PersonalizedRewardableAction> rewardGenericObjectSerializer =
      new GenericMappingSerializer<>(rewardableSubtypes);
  private final GenericMappingDeserializer<PersonalizedRewardableAction> rewardGenericObjectDeserializer =
      new GenericMappingDeserializer<>(PersonalizedRewardableAction.class,
          rewardableSubtypes);
  Serde paymentSerde = Serdes.serdeFrom(paymentSerializer, paymentDeserializer);
  Serde userSerde = Serdes.serdeFrom(userSerializer, userDeserializer);
  Serde campaignSerde = Serdes.serdeFrom(campaignGenericObjectSerializer, campaignGenericObjectDeserializer);
  Serde rewardableSerde = Serdes.serdeFrom(rewardGenericObjectSerializer, rewardGenericObjectDeserializer);

  @BeforeEach
  private void setUp() {
    addMockProcessors();
  }

  @Test
  void buildPipelineTest() {
    createTopologyAndSendMessages();
    verify(main, atLeast(1)).processRewardable(any());
  }

  @Test
  void buildPipelineMultipleProcessorsTest() {
    addMockProcessors();
    createTopologyAndSendMessages();
    verify(main, atLeast(1)).processRewardable(any());
  }

  @Test
  void processRewardableTest() {
    StreamsBuilder streamsBuilder = new StreamsBuilder();
    KStream<String, PersonalizedRewardableAction> rewardableActionKStream = streamsBuilder.stream("input-topic-reward",
        Consumed.with(Serdes.String(), rewardableSerde));
    when(streams.getProperties()).thenReturn(Collections.singletonMap("rewards-topic", "output-topic-rewards"));
    when(kafkaProperties.getStreams()).thenReturn(streams);
    main.processRewardable(rewardableActionKStream);
    var topology = streamsBuilder.build();
    try (TopologyTestDriver topologyTestDriver = new TopologyTestDriver(topology, DefaultStreamProperties.getDefaultProps())) {
      TestInputTopic inputTopicRewards = topologyTestDriver
          .createInputTopic("input-topic-reward", new StringSerializer(), new GenericMappingSerializer<>(rewardableSubtypes));
      TestOutputTopic<String, Reward> rewards = topologyTestDriver.createOutputTopic("output-topic-rewards",
          new StringDeserializer(), new GenericObjectDeserializer<>(CashReward.class));
      inputTopicRewards.pipeInput("123", new PersonalizedRewardableAction(new PersonalizedAction(payment, user), campaign));
      assertThat(rewards.readKeyValue(), equalTo(new KeyValue<>("123", new CashReward("123", "ya@ya.ya", "USD", 10))));
    }
  }

  private void createTopologyAndSendMessages() {
    StreamsBuilder streamsBuilder = new StreamsBuilder();
    KStream<String, Action> actions = streamsBuilder.stream("input-topic-actions",
        Consumed.with(Serdes.String(), paymentSerde));
    KStream<String, Actor> actors = streamsBuilder.stream("input-topic-actors",
        Consumed.with(Serdes.String(), userSerde));
    KStream<String, Campaign> campaigns = streamsBuilder.stream("input-topic-campaigns",
        Consumed.with(Serdes.String(), campaignSerde));
    when(actionsProcessor.getStream()).thenReturn(actions);
    when(actorsProcessor.getStream()).thenReturn(actors);
    when(marketingCampaignProcessor.getStream()).thenReturn(campaigns);
    when(streams.getProperties()).thenReturn(Collections.singletonMap("rewards-topic", "output-topic-rewards"));
    when(kafkaProperties.getStreams()).thenReturn(streams);
    main.buildPipeline(actionProcessors, actorsProcessors, marketingCampaignProcessors);
    var topology = streamsBuilder.build();
    try (TopologyTestDriver topologyTestDriver = new TopologyTestDriver(topology, DefaultStreamProperties.getDefaultProps())) {
      TestInputTopic<String, Action> inputTopicActions = topologyTestDriver
          .createInputTopic("input-topic-actions", new StringSerializer(), new GenericObjectSerializer<>());
      TestInputTopic<String, Actor> inputTopicActors = topologyTestDriver
          .createInputTopic("input-topic-actors", new StringSerializer(), new GenericObjectSerializer<>());
      TestInputTopic<String, Campaign> inputTopicCampaign = topologyTestDriver
          .createInputTopic("input-topic-campaigns", new StringSerializer(), new GenericObjectSerializer<>());
      inputTopicActions.pipeInput("123", payment);
      inputTopicActors.pipeInput("123", user);
      inputTopicCampaign.pipeInput("123", campaign);
    }
  }

  private void addMockProcessors() {
    actionProcessors.add(actionsProcessor);
    actorsProcessors.add(actorsProcessor);
    marketingCampaignProcessors.add(marketingCampaignProcessor);
  }
}