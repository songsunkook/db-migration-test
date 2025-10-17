package io.spring.dbmigration.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.togglz.core.manager.FeatureManager;
import org.togglz.core.repository.FeatureState;
import org.togglz.core.user.UserProvider;

import io.spring.dbmigration.config.FeatureFlags;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/feature-flags")
@RequiredArgsConstructor
public class FeatureFlagController {

    private final FeatureManager featureManager;
    private final UserProvider userProvider;

    // Feature Flag 상태 조회
    @GetMapping("/USE_NEW_SCHEMA")
    public ResponseEntity<Map<String, Object>> getFeatureFlag() {
        boolean isActive = FeatureFlags.USE_NEW_SCHEMA.isActive();
        FeatureState state = featureManager.getFeatureState(FeatureFlags.USE_NEW_SCHEMA);
        
        return ResponseEntity.ok(Map.of(
            "isActive", isActive,
            "enabled", state.isEnabled(),
            "feature", FeatureFlags.USE_NEW_SCHEMA.name(),
            "strategy", state.getStrategyId() != null ? state.getStrategyId() : "None",
            "percentage", state.getParameter("percentage") != null ? 
                state.getParameter("percentage") + "%" : "Not set",
            "consoleUrl", "http://localhost:8083/togglz-console/index"
        ));
    }

    // 사용자별 분기 처리 확인
    @GetMapping("/current-user")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
        // FeatureFlag 사용자 식별용 헤더
        @RequestHeader String userId
    ) {
        var currentUser = userProvider.getCurrentUser();
        boolean isActive = FeatureFlags.USE_NEW_SCHEMA.isActive();
        
        return ResponseEntity.ok(Map.of(
            "currentUser", currentUser.getName(),
            "isFeatureUser", currentUser.isFeatureAdmin(),
            "featureActive", isActive,
            "userSpecificActivation", "User '" + currentUser.getName() + "' sees feature as: " + (isActive ? "ACTIVE" : "INACTIVE")
        ));
    }
}
