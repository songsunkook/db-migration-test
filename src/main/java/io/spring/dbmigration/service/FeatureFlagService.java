package io.spring.dbmigration.service;

import io.spring.dbmigration.config.FeatureFlags;
import org.springframework.stereotype.Service;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.FeatureState;

@Service
public class FeatureFlagService {
    
    private final FeatureManager featureManager;
    private int percentageRollout = 0;
    
    public FeatureFlagService(FeatureManager featureManager) {
        this.featureManager = featureManager;
    }
    
    public boolean isNewSchemaReadEnabled() {
        return featureManager.isActive(FeatureFlags.NEW_SCHEMA_READ_ENABLED);
    }
    
    public int getPercentageRollout() {
        return percentageRollout;
    }
    
    public boolean shouldUseNewSchemaForRead(Long userId) {
        if (!isNewSchemaReadEnabled()) {
            return false;
        }
        
        return shouldApplyToUser(userId);
    }
    
    private boolean shouldApplyToUser(Long userId) {
        int percentage = getPercentageRollout();
        
        if (percentage >= 100) {
            return true;
        }
        
        if (percentage <= 0) {
            return false;
        }
        
        return (Math.abs(userId.hashCode()) % 100) < percentage;
    }
    
    public void updateNewSchemaReadFlag(boolean enabled) {
        FeatureState state = new FeatureState(FeatureFlags.NEW_SCHEMA_READ_ENABLED, enabled);
        featureManager.setFeatureState(state);
    }
    
    public void updatePercentageRollout(int percentage) {
        this.percentageRollout = percentage;
    }
    
    public String getProviderInfo() {
        return "Togglz Feature Flag Library - Spring Boot Integration";
    }
}