package com.taxflow.config;

import com.taxflow.tenant.Tenant;
import com.taxflow.tenant.TenantRepository;
import com.taxflow.user.User;
import com.taxflow.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DemoUserSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final TenantRepository tenantRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Tenant tenant = tenantRepository.findBySchemaName("tenant_demo").orElse(null);
        if (tenant == null) {
            log.warn("tenant_demo not found — skip demo user seed");
            return;
        }

        String email = "demo@taxflow.kr";
        boolean exists = userRepository.findAllByEmailIgnoreCase(email).stream()
                .anyMatch(u -> u.getTenant() != null && u.getTenant().getId().equals(tenant.getId()));
        if (exists) {
            return;
        }

        User user = User.builder()
                .tenant(tenant)
                .email(email)
                .passwordHash(passwordEncoder.encode("TaxFlow123!"))
                .name("데모 관리자")
                .role(User.Role.TENANT_ADMIN)
                .status(User.UserStatus.ACTIVE)
                .build();
        userRepository.save(user);
        log.info("Seeded demo user: {} / TaxFlow123!", email);
    }
}
