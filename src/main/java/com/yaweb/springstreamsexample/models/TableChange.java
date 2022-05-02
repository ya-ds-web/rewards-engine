package com.yaweb.springstreamsexample.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TableChange<E>(String tableName, String operationType, E before, E after) {
}
