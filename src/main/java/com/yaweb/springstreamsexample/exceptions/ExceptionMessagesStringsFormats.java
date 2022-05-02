package com.yaweb.springstreamsexample.exceptions;

/**
 * Created by ya-ds on 30 April 2022
 */

public class ExceptionMessagesStringsFormats {
  private ExceptionMessagesStringsFormats() {
  }

  public static final String MISSING_DESERIALIZATION_CONFIG = "Deserializer is not configured!";
  public static final String MISSING_PARAMETRIC_TYPE = "Missing parametric type!";
  public static final String MESSAGE_TO_TABLECHANGE_DESERIALIZATION_EXCEPTION = "Unable to deserialize message to "
      + "table change for %s!";
  public static final String MESSAGE_TO_OBJECT_DESERIALIZATION_EXCEPTION = "Unable to deserialize message to "
      + "object %s!";
}
