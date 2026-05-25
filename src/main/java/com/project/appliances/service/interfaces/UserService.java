package com.project.appliances.service.interfaces;

import com.project.appliances.model.User;

import java.util.Optional;

public interface UserService {
    Optional<User> findUserByEmail(String email);

    void registerFailedAttempt(String email);

    void resetFailedAttempts(String email);

    void unlockIfLockExpired(User user);

    //void updatePassword(String currentEmail, UserUpdatePasswordDto userUpdatePasswordDto);
}
