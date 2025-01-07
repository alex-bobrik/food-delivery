package com.foodDelivery.foodDeliveryCoursework.controller;

import com.foodDelivery.foodDeliveryCoursework.model.Menu;
import com.foodDelivery.foodDeliveryCoursework.model.Order;
import com.foodDelivery.foodDeliveryCoursework.model.Restaurant;
import com.foodDelivery.foodDeliveryCoursework.model.User;
import com.foodDelivery.foodDeliveryCoursework.repository.OrderRepository;
import com.foodDelivery.foodDeliveryCoursework.repository.RestaurantRepository;
import com.foodDelivery.foodDeliveryCoursework.repository.UserRepository;
import com.foodDelivery.foodDeliveryCoursework.service.MenuService;
import com.foodDelivery.foodDeliveryCoursework.service.OrderService;
import com.foodDelivery.foodDeliveryCoursework.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private MenuService menuService;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    // Orders
    @GetMapping("/orders")
    public String getAllOrders(Model model) {
        List<Order> orders = orderService.findAllOrders();
        model.addAttribute("orders", orders);
        return "admin/orders";
    }

    @PostMapping("orders/{id}/cancel")
    public String cancelOrder(@PathVariable Long id) {
        orderService.updateOrderStatus(id, "CANCELLED");
        return "redirect:/admin/orders";
    }

    @PostMapping("orders/{id}/complete")
    public String completeOrder(@PathVariable Long id) {
        orderService.updateOrderStatus(id, "DELIVERED");
        return "redirect:/admin/orders";
    }

    // Restaurants
    @GetMapping("/restaurants")
    public String getAllRestaurants(Model model) {
        List<Restaurant> restaurants = restaurantRepository.findAll();
        model.addAttribute("restaurants", restaurants);
        return "admin/restaurants";
    }

    @GetMapping("/restaurants/{id}/menus")
    public String getRestaurantMenus(@PathVariable Long id, Model model) {
        List<Menu> menus = menuService.findMenusByRestaurantId(id);
        model.addAttribute("menus", menus);
        return "admin/restaurant-menus";
    }

    @PostMapping("/restaurants/save")
    public String saveRestaurant(
            @RequestParam(required = false) Long restaurant_id,
            @RequestParam String restaurant_name,
            @RequestParam String restaurant_address,
            @RequestParam String restaurant_contact,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String password
    ) {
        if (restaurant_id == null) {

            if (userRepository.findByUsername(username) != null) {
                throw new RuntimeException("User already exists");
            }

            User newUser = new User();
            newUser.setUsername(username);
            newUser.setPassword(passwordEncoder.encode(password));
            newUser.setRole(User.Role.RESTAURANT);
            userService.saveUser(newUser);

            Restaurant newRestaurant = new Restaurant();
            newRestaurant.setId(newUser.getId());
            newRestaurant.setName(restaurant_name);
            newRestaurant.setAddress(restaurant_address);
            newRestaurant.setContactInfo(restaurant_contact);
            newRestaurant.setCreatedAt(LocalDateTime.now());
            restaurantRepository.save(newRestaurant);
        } else {
            Restaurant existingRestaurant = restaurantRepository.findById(restaurant_id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid restaurant ID"));

            existingRestaurant.setName(restaurant_name);
            existingRestaurant.setAddress(restaurant_address);
            existingRestaurant.setContactInfo(restaurant_contact);
            restaurantRepository.save(existingRestaurant);
        }

        return "redirect:/admin/restaurants";
    }

    // Menus
    @PostMapping("/menus/delete/{id}")
    public String deleteMenu(@PathVariable Long id, @RequestParam Long restaurantId) {
        menuService.deleteById(id);

        return "redirect:/admin/restaurants/" + restaurantId + "/menus";
    }

    // Users
    @GetMapping("/users")
    public String getAllUsers(Authentication authentication, Model model) {
        User currentUser = userService.findByUsername(authentication.getName());
        List<User> users = userService.getAllUsers();
        model.addAttribute("currentUserId", currentUser.getId());
        model.addAttribute("users", users);
        return "admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUserById(id);
        return "redirect:/admin/users";
    }

    @PostMapping("/users/add")
    public String addUser(@RequestParam String username,
                          @RequestParam String password,
                          @RequestParam String role) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(User.Role.valueOf(role.toUpperCase()));

        userService.saveUser(user);
        return "redirect:/admin/users";
    }

    // Reports
    @GetMapping("/reports")
    public String getReportsPage(Model model) {
        try {
            List<Order> allOrders = orderService.findAllOrders();

            Map<String, Long> orderStatusCount = allOrders.stream()
                    .collect(Collectors.groupingBy(order -> order.getStatus().toString(), Collectors.counting()));

            model.addAttribute("orderStatuses", orderStatusCount.keySet());
            model.addAttribute("orderCountsByStatus", orderStatusCount.values());

            List<Order> deliveredOrders = allOrders.stream()
                    .filter(order -> order.getStatus() == Order.Status.DELIVERED)
                    .collect(Collectors.toList());

            Map<String, Long> ordersPerDay = deliveredOrders.stream()
                    .collect(Collectors.groupingBy(order -> order.getCreatedAt().toLocalDate().toString(), Collectors.counting()));

            model.addAttribute("orderDates", ordersPerDay.keySet());
            model.addAttribute("ordersByDay", ordersPerDay.values());

            List<Restaurant> restaurants = restaurantRepository.findAll();

            Map<String, Long> orderCountsByRestaurant = restaurants.stream()
                    .collect(Collectors.toMap(
                            Restaurant::getName,
                            restaurant -> deliveredOrders.stream()
                                    .filter(order -> order.getRestaurant().getId().equals(restaurant.getId()))
                                    .count()
                    ));

            Map<String, Double> revenueByRestaurant = restaurants.stream()
                    .collect(Collectors.toMap(
                            Restaurant::getName,
                            restaurant -> deliveredOrders.stream()
                                    .filter(order -> order.getRestaurant().getId().equals(restaurant.getId()))
                                    .mapToDouble(order -> order.getTotalAmount().doubleValue())
                                    .sum()
                    ));

            model.addAttribute("restaurants", orderCountsByRestaurant.keySet());
            model.addAttribute("orderCountsByRestaurant", orderCountsByRestaurant.values());
            model.addAttribute("revenuesByRestaurant", revenueByRestaurant.values());

            Map<String, Long> orderCountsByUser = deliveredOrders.stream()
                    .collect(Collectors.groupingBy(order -> order.getUser().getUsername(), Collectors.counting()));

            Map<String, Double> userRevenue = deliveredOrders.stream()
                    .collect(Collectors.groupingBy(
                            order -> order.getUser().getUsername(),
                            Collectors.summingDouble(order -> order.getTotalAmount().doubleValue())
                    ));

            model.addAttribute("users", orderCountsByUser.keySet());
            model.addAttribute("orderCountsByUser", orderCountsByUser.values());
            model.addAttribute("userRevenues", userRevenue.values());

            Map<String, Long> ordersByHour = deliveredOrders.stream()
                    .collect(Collectors.groupingBy(order -> String.valueOf(order.getCreatedAt().getHour()), Collectors.counting()));

            model.addAttribute("orderHours", ordersByHour.keySet());
            model.addAttribute("ordersByHour", ordersByHour.values());

        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return "admin/reports";
    }
}
