package com.foodDelivery.foodDeliveryCoursework.controller;

import com.foodDelivery.foodDeliveryCoursework.model.Menu;
import com.foodDelivery.foodDeliveryCoursework.model.Order;
import com.foodDelivery.foodDeliveryCoursework.model.Restaurant;
import com.foodDelivery.foodDeliveryCoursework.model.User;
import com.foodDelivery.foodDeliveryCoursework.repository.RestaurantRepository;
import com.foodDelivery.foodDeliveryCoursework.service.MenuService;
import com.foodDelivery.foodDeliveryCoursework.service.OrderService;
import com.foodDelivery.foodDeliveryCoursework.service.RestaurantService;
import com.foodDelivery.foodDeliveryCoursework.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/restaurant")
public class RestaurantController {

    @Autowired
    private MenuService menuService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RestaurantService restaurantService;

    public RestaurantController(MenuService menuService, UserService userService) {
        this.menuService = menuService;
        this.userService = userService;
    }

    @GetMapping("/menus")
    public String getRestaurantMenus(Authentication authentication, Model model) {
        User currentUser = userService.findByUsername(authentication.getName());
        List<Menu> menus = menuService.findMenusByRestaurantId(currentUser.getId());
        model.addAttribute("menus", menus);
        return "restaurant/menus";
    }

    @GetMapping("/orders")
    public String getRestaurantOrders(Authentication authentication, Model model) {
        User currentUser = userService.findByUsername(authentication.getName());
        List<Order> orders = orderService.findOrdersByRestaurantId(currentUser.getId());
        model.addAttribute("orders", orders);
        return "restaurant/orders";
    }

    @PostMapping("/menus/save")
    public String saveOrUpdateMenu(@RequestParam(required = false) Long menu_id,
                                   @RequestParam String menu_name,
                                   @RequestParam String menu_description,
                                   @RequestParam double menu_price,
                                   Authentication authentication) {

        User currentUser = userService.findByUsername(authentication.getName());

        System.out.println("MENU_ID:");
        System.out.println(menu_id);

        Menu menu;
        if (menu_id != null) {
            // Обновление существующего меню
            menu = menuService.findById(menu_id).orElseThrow(() -> new IllegalArgumentException("Menu not found"));
        } else {
            Restaurant restaurant = restaurantService.findById(currentUser.getId());
            // Создание нового меню
            menu = new Menu();
            menu.setRestaurant(restaurant);
        }

        menu.setName(menu_name);
        menu.setDescription(menu_description);
        menu.setPrice(BigDecimal.valueOf(menu_price));

        menuService.save(menu);

        return "redirect:/restaurant/menus";
    }
}
