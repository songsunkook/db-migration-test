package io.spring.dbmigration.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.spring.dbmigration.domain.User;
import io.spring.dbmigration.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "사용자 API", description = "사용자 생성, 조회, 수정 기능을 제공하는 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    @Operation(summary = "사용자 생성", description = "새로운 사용자를 생성합니다. 5단계: 새로운 스키마만 사용합니다.")
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        User user = userService.createUser(request.fullName(), request.email());
        return ResponseEntity.ok(user);
    }
    
    @Operation(summary = "사용자 목록 조회", description = "모든 사용자를 조회합니다.")
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @Operation(summary = "사용자 조회", description = "ID로 특정 사용자를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@Parameter(description = "사용자 ID", example = "1") @PathVariable Long id) {
        return userService.getUser(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "사용자 표시명 조회", 
               description = "5단계: 새로운 스키마(fullName)로만 표시명을 조회합니다.")
    @GetMapping("/{id}/display-name")
    public ResponseEntity<Map<String, String>> getFullName(
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Long id) {
        try {
            String fullName = userService.getFullName(id);
            return ResponseEntity.ok(Map.of("fullName", fullName));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(summary = "사용자 이름 수정", 
               description = "사용자의 이름을 수정합니다. 5단계: 새로운 스키마만 업데이트합니다.")
    @PutMapping("/{id}/name")
    public ResponseEntity<User> updateUserName(
            @Parameter(description = "사용자 ID", example = "1") @PathVariable Long id, 
            @RequestBody UpdateNameRequest request) {
        User user = userService.updateUserName(id, request.fullName());
        return ResponseEntity.ok(user);
    }
    
    public record CreateUserRequest(String fullName, String email) {}
    public record UpdateNameRequest(String fullName) {}
}
