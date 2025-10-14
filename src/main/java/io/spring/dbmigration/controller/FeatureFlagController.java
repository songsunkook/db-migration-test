package io.spring.dbmigration.controller;

import io.spring.dbmigration.service.FeatureFlagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Feature Flag API", description = "Feature Flag 관리 및 점진적 배포를 위한 API")
@RestController
@RequestMapping("/api/feature-flags")
public class FeatureFlagController {
    
    private final FeatureFlagService featureFlagService;
    
    public FeatureFlagController(FeatureFlagService featureFlagService) {
        this.featureFlagService = featureFlagService;
    }
    
    @Operation(summary = "Feature Flag 전체 조회", 
               description = "현재 설정된 모든 Feature Flag 상태를 조회합니다.")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllFlags() {
        return ResponseEntity.ok(Map.of(
            "newSchemaReadEnabled", featureFlagService.isNewSchemaReadEnabled(),
            "percentageRollout", featureFlagService.getPercentageRollout(),
            "provider", "Togglz",
            "dualWriteStatus", "항상 활성화 (Feature Flag 무관)",
            "note", "Feature Flag는 읽기 전환에만 사용됩니다."
        ));
    }
    
    @Operation(summary = "신규 스키마 읽기 전환", 
               description = "신규 스키마(full_name 컬럼) 사용 여부를 전환합니다. 점진적 배포의 핵심 기능입니다.")
    @PutMapping("/new-schema-read")
    public ResponseEntity<Map<String, Object>> toggleNewSchemaRead(@RequestBody Map<String, Boolean> request) {
        boolean enabled = request.getOrDefault("enabled", false);
        featureFlagService.updateNewSchemaReadFlag(enabled);
        
        return ResponseEntity.ok(Map.of(
            "message", "신규 스키마 읽기 전환이 업데이트되었습니다",
            "enabled", enabled,
            "explanation", enabled ? 
                "이제 full_name 컬럼을 사용하여 사용자 이름을 조회합니다" : 
                "기존처럼 first_name + last_name 조합을 사용합니다"
        ));
    }
    
    @Operation(summary = "점진적 배포 비율 설정", 
               description = "신규 스키마 읽기를 적용할 사용자 비율을 설정합니다. (0-100%)")
    @PutMapping("/percentage-rollout")
    public ResponseEntity<Map<String, Object>> updatePercentageRollout(@RequestBody Map<String, Integer> request) {
        int percentage = request.getOrDefault("percentage", 0);
        if (percentage < 0 || percentage > 100) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Percentage must be between 0 and 100"
            ));
        }
        
        featureFlagService.updatePercentageRollout(percentage);
        
        return ResponseEntity.ok(Map.of(
            "message", "점진적 배포 비율이 업데이트되었습니다",
            "percentage", percentage,
            "explanation", percentage + "% 사용자에게 신규 스키마 읽기가 적용됩니다"
        ));
    }
    
    @Operation(summary = "특정 사용자 Feature Flag 상태", 
               description = "특정 사용자에게 적용될 Feature Flag 상태를 확인합니다. 사용자 ID 해시 기반으로 일관된 결과를 제공합니다.")
    @GetMapping("/user/{userId}/status")
    public ResponseEntity<Map<String, Object>> getUserFeatureFlags(
            @Parameter(description = "사용자 ID", example = "123") @PathVariable Long userId) {
        boolean willUseNewSchema = featureFlagService.shouldUseNewSchemaForRead(userId);
        
        return ResponseEntity.ok(Map.of(
            "userId", userId,
            "willUseNewSchemaRead", willUseNewSchema,
            "readMethod", willUseNewSchema ? "full_name 컬럼" : "first_name + last_name 조합",
            "dualWriteStatus", "항상 활성화",
            "userHash", Math.abs(userId.hashCode()) % 100,
            "globalPercentage", featureFlagService.getPercentageRollout()
        ));
    }
    
    @Operation(summary = "Feature Flag 시스템 상태", 
               description = "Togglz Feature Flag 시스템의 전반적인 상태와 설정 정보를 제공합니다.")
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getFeatureFlagStatus() {
        return ResponseEntity.ok(Map.of(
            "provider", featureFlagService.getProviderInfo(),
            "activeFlags", Map.of(
                "new-schema-read-enabled", featureFlagService.isNewSchemaReadEnabled(),
                "percentage-rollout", featureFlagService.getPercentageRollout()
            ),
            "dualWriteStrategy", "Feature Flag와 무관하게 항상 활성화",
            "documentation", "https://www.togglz.org/",
            "console", "http://localhost:8080/togglz-console"
        ));
    }
}