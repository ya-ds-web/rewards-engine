package com.yaweb.rewardsengine.common;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;

import java.util.Properties;

import static org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.BOOTSTRAP_SERVERS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG;

/**
 * Created by ya-ds on 20 May 2022
 */

public final class DefaultStreamProperties {

  private static final Properties props = new Properties();

  public static Properties getDefaultProps() {
    props.put(APPLICATION_ID_CONFIG, "rewards-engin");
    props.put(BOOTSTRAP_SERVERS_CONFIG, "test");
    props.put(DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    props.put(DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
    props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
        "org.apache.kafka.streams.errors.LogAndContinueExceptionHandler");
    return props;
  }
}
