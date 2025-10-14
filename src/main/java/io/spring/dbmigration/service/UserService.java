package io.spring.dbmigration.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.spring.dbmigration.domain.User;
import io.spring.dbmigration.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;

    @Transactional
    public User createUser(String firstName, String lastName, String email) {
        User user = new User(firstName, lastName, email);
        return userRepository.save(user);
    }
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public Optional<User> getUser(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User updateUserName(Long id, String firstName, String lastName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        user.updateName(firstName, lastName);
        return userRepository.save(user);
    }
    
    public String getDisplayName(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        return user.getFullName();
    }
}
