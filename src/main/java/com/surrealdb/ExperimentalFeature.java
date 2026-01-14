package com.surrealdb;

/**
 * Experimental features which can be enabled through capabilities.
 */
public enum ExperimentalFeature {
    RECORD_REFERENCES(0),
    GRAPH_QL(1),
    BEARER_ACCESS(2),
    DEFINE_API(3);

    private final int value;

    ExperimentalFeature(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
