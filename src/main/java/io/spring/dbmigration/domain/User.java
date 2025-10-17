package io.spring.dbmigration.domain;

import java.time.LocalDateTime;

import io.spring.dbmigration.config.FeatureFlags;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public User(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.fullName = firstName + " " + lastName;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public String getFullName() {
        // Togglz가 현재 HTTP 요청의 사용자 컨텍스트에서 자동으로 판단
        boolean useNewSchema = FeatureFlags.USE_NEW_SCHEMA.isActive();

        if (useNewSchema && fullName != null) {
            return fullName;
        }
        if (fullName == null) {
            if (useNewSchema) {
                // 이미 읽기 전환되었는데 신규 스키마가 null인 경우 로그 남기기
                log.warn("Full name is null, falling back to firstName + lastName");
            }
            return firstName + " " + lastName;
        }
        return fullName;
    }

    public void updateName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.fullName = firstName + " " + lastName;
        this.updatedAt = LocalDateTime.now();
    }
}
