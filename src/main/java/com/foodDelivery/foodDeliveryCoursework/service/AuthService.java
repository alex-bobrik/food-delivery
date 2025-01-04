package com.foodDelivery.foodDeliveryCoursework.service;

import com.foodDelivery.foodDeliveryCoursework.model.User;
import com.foodDelivery.foodDeliveryCoursework.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Аутентификация пользователя
    public boolean authenticate(String username, String password) {
        User user = userRepository.findByUsername(username);

        // Проверяем, существует ли пользователь и правильно ли введен пароль
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return true;  // Успешная аутентификация
        }
        return false;  // Неудачная аутентификация
    }
}
