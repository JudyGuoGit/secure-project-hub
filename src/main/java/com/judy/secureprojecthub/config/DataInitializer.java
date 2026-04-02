package com.judy.secureprojecthub.config;

import com.judy.secureprojecthub.entity.User;
import com.judy.secureprojecthub.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Initialize default users if they don't exist
 */
@Configuration
public class DataInitializer {
    
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    @Transactional
    public CommandLineRunner initializeUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            try {
                logger.info("🔵 Starting DataInitializer - checking for default users...");
                
                // Check if admin user already exists
                if (userRepository.findByUsername("admin").isEmpty()) {
                    logger.info("Creating admin user...");
                    User admin = new User();
                    admin.setUsername("admin");
                    admin.setEmail("admin@example.com");
                    admin.setPasswordHash(passwordEncoder.encode("admin"));
                    admin.setEnabled(true);
                    admin.setAccountNonExpired(true);
                    admin.setCredentialsNonExpired(true);
                    admin.setAccountNonLocked(true);
                    admin.setFullName("Admin User");
                    admin.setBio("Administrator account");
                    admin.setCreatedAt(LocalDateTime.now());
                    admin.setUpdatedAt(LocalDateTime.now());
                    userRepository.save(admin);
                    logger.info("✅ Admin user created");
                } else {
                    logger.info("✅ Admin user already exists");
                }

                // Check if user user exists
                if (userRepository.findByUsername("user").isEmpty()) {
                    logger.info("Creating user...");
                    User user = new User();
                    user.setUsername("user");
                    user.setEmail("user@example.com");
                    user.setPasswordHash(passwordEncoder.encode("password"));
                    user.setEnabled(true);
                    user.setAccountNonExpired(true);
                    user.setCredentialsNonExpired(true);
                    user.setAccountNonLocked(true);
                    user.setFullName("Test User");
                    user.setBio("Test user account");
                    user.setCreatedAt(LocalDateTime.now());
                    user.setUpdatedAt(LocalDateTime.now());
                    userRepository.save(user);
                    logger.info("✅ User created");
                } else {
                    logger.info("✅ User already exists");
                }

                // Check if john user exists
                if (userRepository.findByUsername("john").isEmpty()) {
                    logger.info("Creating john user...");
                    User john = new User();
                    john.setUsername("john");
                    john.setEmail("john.doe@example.com");
                    john.setPasswordHash(passwordEncoder.encode("password"));
                    john.setEnabled(true);
                    john.setAccountNonExpired(true);
                    john.setCredentialsNonExpired(true);
                    john.setAccountNonLocked(true);
                    john.setFullName("John Doe");
                    john.setBio("Developer");
                    john.setCreatedAt(LocalDateTime.now());
                    john.setUpdatedAt(LocalDateTime.now());
                    userRepository.save(john);
                    logger.info("✅ John user created");
                } else {
                    logger.info("✅ John user already exists");
                }

                // Check if jane user exists
                if (userRepository.findByUsername("jane").isEmpty()) {
                    logger.info("Creating jane user...");
                    User jane = new User();
                    jane.setUsername("jane");
                    jane.setEmail("jane.smith@example.com");
                    jane.setPasswordHash(passwordEncoder.encode("password"));
                    jane.setEnabled(true);
                    jane.setAccountNonExpired(true);
                    jane.setCredentialsNonExpired(true);
                    jane.setAccountNonLocked(true);
                    jane.setFullName("Jane Smith");
                    jane.setBio("Product Manager");
                    jane.setCreatedAt(LocalDateTime.now());
                    jane.setUpdatedAt(LocalDateTime.now());
                    userRepository.save(jane);
                    logger.info("✅ Jane user created");
                } else {
                    logger.info("✅ Jane user already exists");
                }
                
                logger.info("🎉 DataInitializer completed successfully");
            } catch (Exception e) {
                logger.error("❌ Error in DataInitializer", e);
                throw e;
            }
        };
    }
}
