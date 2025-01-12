package com.foodDelivery.foodDeliveryCoursework.service;

import com.foodDelivery.foodDeliveryCoursework.model.Restaurant;
import com.foodDelivery.foodDeliveryCoursework.model.User;
import com.foodDelivery.foodDeliveryCoursework.model.User.Role;
import com.foodDelivery.foodDeliveryCoursework.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final RestaurantService restaurantService;

    private final MenuService menuService;

    @Autowired
    private OrderService orderService;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, RestaurantService restaurantService, MenuService menuService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.restaurantService = restaurantService;
        this.menuService = menuService;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User saveUser(User user) {
        user.setEmail(user.getUsername() + "@test.com");
        user.setCreatedAt(LocalDateTime.now());

        return userRepository.save(user);
    }

    public void registerUser(String email, String username, String password) {
        if (userRepository.findByUsername(username) != null) {
            logger.error("User with username '{}' already exists!", username);
            throw new RuntimeException("User already exists");
        }

        System.out.println("Registering new user with username: " + username);

        try {
            String encodedPassword = passwordEncoder.encode(password);

            User user = new User();
            user.setUsername(username);
            user.setPassword(encodedPassword);
            user.setRole(Role.CLIENT);
            user.setEmail(email);
            user.setCreatedAt(LocalDateTime.now());

            userRepository.save(user);
        } catch (Exception exception) {
            logger.error("Error while registering user: {}", exception.getMessage());
            throw new RuntimeException("Registration failed: " + exception.getMessage());
        }
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username);

        if (user.getId() == null) {
            throw new UsernameNotFoundException("Пользователь " + username + " не найден");
        }

        return user;
    }

    public List<User> findAllByRole(User.Role role) {
        return userRepository.findAllByRole(role);
    }

    public void deleteUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getRole() == Role.RESTAURANT) {
            Restaurant restaurant = restaurantService.findById(user.getId());

            menuService.deleteByRestaurantId(restaurant.getId());

            orderService.detachRestaurantFromOrders(restaurant.getId());

            restaurantService.delete(restaurant);
        }

        else if (user.getRole() == Role.COURIER) {
            orderService.detachCourierFromOrders(user.getId());
        }

        else if (user.getRole() == Role.CLIENT) {
            orderService.detachClientFromOrders(user.getId());
        }

        userRepository.deleteById(id);
    }

    public User saveUserAndReturn(User user) {
        return userRepository.save(user);
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));
    }
}

