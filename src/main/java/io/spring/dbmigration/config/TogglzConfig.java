package io.spring.dbmigration.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.togglz.core.manager.EnumBasedFeatureProvider;
import org.togglz.core.spi.FeatureProvider;
import org.togglz.core.user.SimpleFeatureUser;
import org.togglz.core.user.UserProvider;

import jakarta.servlet.http.HttpServletRequest;

@Configuration
public class TogglzConfig {

    @Bean
    public FeatureProvider featureProvider() {
        return new EnumBasedFeatureProvider(FeatureFlags.class);
    }

    @Bean
    public UserProvider userProvider() {
        return () -> {
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();

                    // userId 헤더로 사용자 식별
                    String userId = request.getHeader("userId");
                    if (userId != null && !userId.trim().isEmpty()) {
                        return new SimpleFeatureUser(userId.trim(), false);
                    }
                }
            } catch (Exception e) {
                // 무시하고 기본값 사용
            }
            return new SimpleFeatureUser("anonymous", false);
        };
    }
}
