package com.foodDelivery.foodDeliveryCoursework.controller;

import com.foodDelivery.foodDeliveryCoursework.model.Menu;
import com.foodDelivery.foodDeliveryCoursework.model.Order;
import com.foodDelivery.foodDeliveryCoursework.model.Restaurant;
import com.foodDelivery.foodDeliveryCoursework.model.User;
import com.foodDelivery.foodDeliveryCoursework.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/restaurant")
public class RestaurantController {

    private static final String[] CATEGORIES = {"Завтраки", "Основные блюда", "Десерты", "Напитки", "Салаты"};

    @Autowired
    private MenuService menuService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private RestaurantService restaurantService;

    @Autowired
    private FileStorageService fileStorageService;

    public RestaurantController(MenuService menuService, UserService userService) {
        this.menuService = menuService;
        this.userService = userService;
    }

    @GetMapping("/menus")
    public String getRestaurantMenus(Authentication authentication, Model model) {
        User currentUser = userService.findByUsername(authentication.getName());
        List<Menu> menus = menuService.findMenusByRestaurantId(currentUser.getId());
        model.addAttribute("categories", CATEGORIES);
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
                                   @RequestParam Integer menu_quantity,
                                   @RequestParam String menu_category,
                                   @RequestParam(required = false) MultipartFile menu_image,
                                   Authentication authentication,
                                   Model model,
                                   RedirectAttributes redirectAttributes
    ) {
        try {
            User currentUser = userService.findByUsername(authentication.getName());

            Menu menu;
            if (menu_id != null) {
                menu = menuService.findById(menu_id).orElseThrow(() -> new IllegalArgumentException("Menu not found"));
            } else {
                Restaurant restaurant = restaurantService.findById(currentUser.getId());
                menu = new Menu();
                menu.setRestaurant(restaurant);
            }

            menu.setName(menu_name);
            menu.setDescription(menu_description);
            menu.setPrice(BigDecimal.valueOf(menu_price));
            menu.setQuantity(menu_quantity);
            menu.setCategory(menu_category);

            if (menu_image != null && !menu_image.isEmpty()) {
                String imagePath = fileStorageService.saveFile(menu_image);
                menu.setImageUrl(imagePath);
            }

            menuService.save(menu);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/error";
        }

        model.addAttribute("menuInfo", "Menus updated");
        return "redirect:/restaurant/menus";
    }

    @PostMapping("/menus/delete")
    public String deleteMenu(@RequestParam Long menu_id, Model model) {
        menuService.deleteById(menu_id);
        model.addAttribute("menuInfo", "Menu deleted");

        return "redirect:/restaurant/menus";
    }

    @GetMapping("/reports")
    public String getRestaurantReports(Authentication authentication, Model model, RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userService.findByUsername(authentication.getName());
            Restaurant restaurant = restaurantService.findById(currentUser.getId());

            List<Order> orders = orderService.findOrdersByRestaurantId(currentUser.getId());

            List<Order> deliveredOrders = orders.stream()
                    .filter(order -> order.getStatus() == Order.Status.DELIVERED)
                    .toList();

            Map<String, Long> ordersByDate = orderService.getOrdersGroupedByDate(deliveredOrders);

            Map<String, BigDecimal> totalByDate = orderService.getTotalByDate(deliveredOrders);

            Map<String, Long> ordersByStatus = orders.stream()
                    .collect(Collectors.groupingBy(
                            order -> order.getStatus().name(),
                            Collectors.counting()
                    ));

            model.addAttribute("dates", ordersByDate.keySet());
            model.addAttribute("orderCounts", ordersByDate.values());
            model.addAttribute("totalAmounts", totalByDate.values());
            model.addAttribute("statuses", ordersByStatus.keySet());
            model.addAttribute("statusCounts", ordersByStatus.values());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/error";
        }

        return "restaurant/reports";
    }
}
