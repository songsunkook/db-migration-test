package io.spring.dbmigration.config;

import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;

public enum FeatureFlags implements Feature {

    @Label("Use New Schema for Reading")
    USE_NEW_SCHEMA;
}
