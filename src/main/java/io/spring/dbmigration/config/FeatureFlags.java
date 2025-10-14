package io.spring.dbmigration.config;

import org.togglz.core.Feature;
import org.togglz.core.annotation.Label;
import org.togglz.core.context.FeatureContext;

public enum FeatureFlags implements Feature {

    @Label("신규 스키마 읽기 전환")
    NEW_SCHEMA_READ_ENABLED;

    public boolean isActive() {
        return FeatureContext.getFeatureManager().isActive(this);
    }
}