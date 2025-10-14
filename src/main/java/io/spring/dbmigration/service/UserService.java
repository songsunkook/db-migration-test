package io.spring.dbmigration.service;

import io.spring.dbmigration.domain.User;
import io.spring.dbmigration.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final FeatureFlagService featureFlagService;
    
    public UserService(UserRepository userRepository, FeatureFlagService featureFlagService) {
        this.userRepository = userRepository;
        this.featureFlagService = featureFlagService;
    }
    
    public User createUser(String firstName, String lastName, String email) {
        User user = new User(firstName, lastName, email);
        
        // Dual Write는 항상 실행 (Expand 단계에서 신규 컬럼에도 데이터 저장)
        user.setFullName(firstName + " " + lastName);
        
        return userRepository.save(user);
    }
    
    @Transactional(readOnly = true)
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Optional<User> getUser(Long id) {
        return userRepository.findById(id);
    }
    
    public User updateUserName(Long id, String firstName, String lastName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Dual Write는 항상 실행 (구/신 스키마 모두 업데이트)
        user.updateName(firstName, lastName);
        
        return userRepository.save(user);
    }
    
    @Transactional(readOnly = true)
    public String getDisplayName(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Feature Flag: 신규 스키마 읽기로 전환할지 결정
        boolean shouldUseNewSchema = featureFlagService.shouldUseNewSchemaForRead(userId);
        
        if (shouldUseNewSchema && user.getFullName() != null) {
            // 신규 스키마: full_name 컬럼 사용
            return user.getFullName();
        } else {
            // 기존 스키마: first_name + last_name 조합 사용
            return user.getFirstName() + " " + user.getLastName();
        }
    }
}