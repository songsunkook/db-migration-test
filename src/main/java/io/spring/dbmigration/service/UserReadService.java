package io.spring.dbmigration.service;

import io.spring.dbmigration.domain.User;
import io.spring.dbmigration.repository.UserRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserReadService {
    
    private final UserRepository masterUserRepository;
    private final JdbcTemplate replicaJdbcTemplate;
    private final FeatureFlagService featureFlagService;
    
    public UserReadService(UserRepository masterUserRepository,
                          @Qualifier("replicaDataSource") DataSource replicaDataSource,
                          FeatureFlagService featureFlagService) {
        this.masterUserRepository = masterUserRepository;
        this.replicaJdbcTemplate = new JdbcTemplate(replicaDataSource);
        this.featureFlagService = featureFlagService;
    }
    
    @Transactional(readOnly = true)
    public Optional<User> getUserWithConsistency(Long userId, boolean forceFromMaster) {
        if (forceFromMaster) {
            return masterUserRepository.findById(userId);
        }
        
        try {
            String sql = "SELECT id, first_name, last_name, full_name, email, created_at, updated_at FROM users WHERE id = ?";
            List<User> users = replicaJdbcTemplate.query(sql, new UserRowMapper(), userId);
            return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
        } catch (Exception e) {
            return masterUserRepository.findById(userId);
        }
    }
    
    @Transactional(readOnly = true)
    public List<User> getAllUsersFromReplica() {
        try {
            String sql = "SELECT id, first_name, last_name, full_name, email, created_at, updated_at FROM users ORDER BY id";
            return replicaJdbcTemplate.query(sql, new UserRowMapper());
        } catch (Exception e) {
            return masterUserRepository.findAll();
        }
    }
    
    @Transactional(readOnly = true)
    public String getDisplayNameWithConsistency(Long userId, boolean forceFromMaster) {
        Optional<User> userOpt = getUserWithConsistency(userId, forceFromMaster);
        
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
        
        User user = userOpt.get();
        
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
    
    private static class UserRowMapper implements RowMapper<User> {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            // User 엔티티가 protected 생성자만 있으므로 임시로 매핑
            String firstName = rs.getString("first_name");
            String lastName = rs.getString("last_name");
            String email = rs.getString("email");
            
            User user = new User(firstName, lastName, email);
            user.setId(rs.getLong("id"));
            user.setFullName(rs.getString("full_name"));
            
            java.sql.Timestamp createdAtTs = rs.getTimestamp("created_at");
            if (createdAtTs != null) {
                user.setCreatedAt(createdAtTs.toLocalDateTime());
            }
            
            java.sql.Timestamp updatedAtTs = rs.getTimestamp("updated_at");
            if (updatedAtTs != null) {
                user.setUpdatedAt(updatedAtTs.toLocalDateTime());
            }
            
            return user;
        }
    }
}