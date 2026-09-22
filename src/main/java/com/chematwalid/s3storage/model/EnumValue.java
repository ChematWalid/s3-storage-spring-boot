package com.chematwalid.s3storage.model;

import com.fasterxml.jackson.annotation.JsonValue;

/** Stable string representation for values exposed by the library. */
public interface EnumValue {

    @JsonValue
    default String value() {
        return toString();
    }
}
