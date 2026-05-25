package com.project.appliances.security.impl;

import com.project.appliances.constants.Roles;
import com.project.appliances.model.Client;
import com.project.appliances.model.Employee;
import com.project.appliances.model.User;
import com.project.appliances.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userService.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + email + " not found"));

        userService.unlockIfLockExpired(user);

        if (!Boolean.TRUE.equals(user.getAccountNonLocked())) {
            throw new LockedException("Account is locked");
        }

        String role;
        if (user instanceof Client) {
            role = Roles.CLIENT;
        } else if (user instanceof Employee) {
            role = Roles.EMPLOYEE;
        } else {
            throw new UsernameNotFoundException("Unknown user type for email " + email);
        }

        return new CustomUserDetails(user, role);
    }
}
