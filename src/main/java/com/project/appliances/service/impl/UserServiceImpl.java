package com.project.appliances.service.impl;

import com.project.appliances.model.Client;
import com.project.appliances.model.Employee;
import com.project.appliances.model.User;
import com.project.appliances.repository.ClientRepository;
import com.project.appliances.repository.EmployeeRepository;
import com.project.appliances.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final int LOCK_DURATION_MINUTES = 15;

    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Optional<User> findUserByEmail(String email) {
        return clientRepository.findByEmail(email).map(user -> (User) user)
                .or(() -> employeeRepository.findByEmail(email).map(user -> (User) user));
    }

    @Override
    @Transactional
    public void registerFailedAttempt(String email) {
        findUserByEmail(email).ifPresent(user -> {
            int attempts = user.getFailedAttempts() == null ? 0 : user.getFailedAttempts();
            attempts++;

            user.setFailedAttempts(attempts);

            log.warn("SECURITY EVENT | Failed login attempt for '{}' (attempt #{})", email, attempts);

            if (attempts >= MAX_FAILED_ATTEMPTS) {
                user.setAccountNonLocked(false);
                user.setLockTime(LocalDateTime.now());

                log.warn("SECURITY EVENT | Account '{}' locked for {} minutes", email, LOCK_DURATION_MINUTES);
            }

            update(user);
        });
    }

    @Override
    @Transactional
    public void resetFailedAttempts(String email) {
        findUserByEmail(email).ifPresent(user -> {
            user.setFailedAttempts(0);
            user.setAccountNonLocked(true);
            user.setLockTime(null);
            update(user);

            log.info("SECURITY EVENT | Failed attempts reset for '{}'", email);
        });
    }

    @Override
    @Transactional
    public void unlockIfLockExpired(User user) {
        if (Boolean.TRUE.equals(user.getAccountNonLocked())) {
            return;
        }

        LocalDateTime lockTime = user.getLockTime();
        if (lockTime != null && lockTime.plusMinutes(LOCK_DURATION_MINUTES).isBefore(LocalDateTime.now())) {
            user.setAccountNonLocked(true);
            user.setFailedAttempts(0);
            user.setLockTime(null);
            update(user);

            log.info("SECURITY EVENT | Account '{}' auto-unlocked after {} minutes", user.getEmail(), LOCK_DURATION_MINUTES);
        }
    }

    private void update(User user) {
        if (user instanceof Client client) {
            clientRepository.save(client);
        } else if (user instanceof Employee employee) {
            employeeRepository.save(employee);
        }
    }
}