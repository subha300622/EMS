package com.example.ems.config;

import com.example.ems.auth.entity.User;
import com.example.ems.auth.entity.UserSession;
import com.example.ems.auth.repository.UserRepository;
import com.example.ems.auth.service.DatabaseSessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

@Component
@Order(3)
@Profile("!prod")
public class DevSessionSeedingRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevSessionSeedingRunner.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DatabaseSessionStore databaseSessionStore;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Checking/seeding dev user session...");

        // Find the SUPER_ADMIN user
        Optional<User> superAdminOpt = userRepository.findAll().stream()
                .filter(u -> u.getRole() != null && "SUPER_ADMIN".equalsIgnoreCase(u.getRole().getName()))
                .findFirst();

        if (superAdminOpt.isPresent()) {
            User user = superAdminOpt.get();
            String devSessionId = "dev-session-id";
            
            Optional<UserSession> existingSession = databaseSessionStore.findById(devSessionId);
            if (existingSession.isEmpty()) {
                UserSession devSession = new UserSession(
                        devSessionId,
                        user.getUserId(),
                        user.getWorkEmail(),
                        "Developer-Console",
                        "127.0.0.1",
                        "dev-refresh-token",
                        LocalDateTime.now(),
                        LocalDateTime.now().plusYears(1), // Long expiry
                        false,
                        1,
                        1L,
                        "ACTIVE"
                );
                databaseSessionStore.save(devSession);
                log.info("Successfully seeded dev user session in DB: SessionID={}, UserID={}", devSessionId, user.getUserId());
            } else {
                log.info("Dev user session already exists in DB.");
            }
        } else {
            log.warn("SUPER_ADMIN user not found. Dev session seeding skipped.");
        }
    }
}
