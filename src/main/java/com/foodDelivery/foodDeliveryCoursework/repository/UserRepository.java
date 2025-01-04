package com.foodDelivery.foodDeliveryCoursework.repository;

import com.foodDelivery.foodDeliveryCoursework.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    User findByUsername(String username);

    List<User> findAllByRole(User.Role role);
}
