package com.yaweb.springstreamsexample.models.user;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by ya-ds on April, 2022
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public record User(
    long id,
    boolean isSpecial,
    String mail
) {
}
