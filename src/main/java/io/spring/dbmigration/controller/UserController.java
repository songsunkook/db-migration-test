package io.spring.dbmigration.controller;

import io.spring.dbmigration.domain.User;
import io.spring.dbmigration.service.UserService;
import io.spring.dbmigration.service.UserReadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "사용자 API", description = "사용자 생성, 조회, 수정 기능을 제공하는 API")
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private final UserService userService;
    private final UserReadService userReadService;
    
    public UserController(UserService userService, UserReadService userReadService) {
        this.userService = userService;
        this.userReadService = userReadService;
    }
    
    @Operation(summary = "사용자 생성", description = "새로운 사용자를 생성합니다. Dual Write로 기존/신규 스키마 모두에 저장됩니다.")
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        User user = userService.createUser(request.firstName(), request.lastName(), request.email());
        return ResponseEntity.ok(user);
    }
    
    @Operation(summary = "사용자 목록 조회", 
               description = "모든 사용자를 조회합니다. fromMaster=true 시 Master DB에서, false 시 Replica DB에서 조회합니다.")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers(
            @Parameter(description = "Master DB에서 조회 여부 (Read-After-Write Consistency)", example = "false")
            @RequestParam(defaultValue = "false") boolean fromMaster) {
        List<User> users = fromMaster ? 
            userService.getAllUsers() : 
            userReadService.getAllUsersFromReplica();
        return ResponseEntity.ok(users);
    }
    
    @Operation(summary = "사용자 조회", 
               description = "ID로 특정 사용자를 조회합니다. fromMaster=true 시 Master DB에서, false 시 Replica DB에서 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Long id, 
            @Parameter(description = "Master DB에서 조회 여부 (Read-After-Write Consistency)", example = "false")
            @RequestParam(defaultValue = "false") boolean fromMaster) {
        return userReadService.getUserWithConsistency(id, fromMaster)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "사용자 표시명 조회", 
               description = "Feature Flag에 따라 기존 스키마(firstName + lastName) 또는 신규 스키마(fullName)로 표시명을 조회합니다.")
    @GetMapping("/{id}/display-name")
    public ResponseEntity<Map<String, String>> getDisplayName(
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Long id,
            @Parameter(description = "Master DB에서 조회 여부", example = "false")
            @RequestParam(defaultValue = "false") boolean fromMaster) {
        try {
            String displayName = userReadService.getDisplayNameWithConsistency(id, fromMaster);
            return ResponseEntity.ok(Map.of("displayName", displayName));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(summary = "사용자 이름 수정", 
               description = "사용자의 이름을 수정합니다. Dual Write로 기존/신규 스키마 모두 업데이트됩니다.")
    @PutMapping("/{id}/name")
    public ResponseEntity<User> updateUserName(
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Long id, 
            @RequestBody UpdateNameRequest request) {
        User user = userService.updateUserName(id, request.firstName(), request.lastName());
        return ResponseEntity.ok(user);
    }
    
    public record CreateUserRequest(String firstName, String lastName, String email) {}
    public record UpdateNameRequest(String firstName, String lastName) {}
}