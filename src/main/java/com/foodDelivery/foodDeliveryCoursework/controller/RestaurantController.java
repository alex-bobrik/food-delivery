package com.foodDelivery.foodDeliveryCoursework.controller;

import com.foodDelivery.foodDeliveryCoursework.model.Menu;
import com.foodDelivery.foodDeliveryCoursework.model.Order;
import com.foodDelivery.foodDeliveryCoursework.model.Restaurant;
import com.foodDelivery.foodDeliveryCoursework.model.User;
import com.foodDelivery.foodDeliveryCoursework.repository.RestaurantRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
                                   @RequestParam(required = false) MultipartFile menu_image,
                                   Authentication authentication,
                                   Model model
    ) {

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

        System.out.println("MENU_IMAGE:");
        System.out.println(menu_image);

        if (menu_image != null && !menu_image.isEmpty()) {
            String imagePath = fileStorageService.saveFile(menu_image); // Сохранение файла
            menu.setImageUrl(imagePath); // Установка пути к картинке
        }

        menuService.save(menu);

        model.addAttribute("menuInfo", "Menus updated");
        return "redirect:/restaurant/menus";
    }

    @PostMapping("/menus/delete")
    public String deleteMenu(@RequestParam Long menu_id, Model model) {
        // Ищем меню по ID
        menuService.deleteById(menu_id); // Вызываем метод удаления из сервиса
        model.addAttribute("menuInfo", "Menu deleted");

        return "redirect:/restaurant/menus"; // После удаления перенаправляем обратно на страницу меню
    }

    @GetMapping("/reports")
    public String getRestaurantReports(Authentication authentication, Model model) {
        User currentUser = userService.findByUsername(authentication.getName());
        Restaurant restaurant = restaurantService.findById(currentUser.getId());

        // Получаем все заказы ресторана
        List<Order> orders = orderService.findOrdersByRestaurantId(currentUser.getId());

        // Фильтруем заказы со статусом DELIVERED для количества заказов и суммы выручки
        List<Order> deliveredOrders = orders.stream()
                .filter(order -> order.getStatus() == Order.Status.DELIVERED)
                .toList();

        // Количество заказов по дням (только DELIVERED)
        Map<String, Long> ordersByDate = orderService.getOrdersGroupedByDate(deliveredOrders);

        // Общая сумма по дням (только DELIVERED)
        Map<String, BigDecimal> totalByDate = orderService.getTotalByDate(deliveredOrders);

        // Количество заказов по статусам
        Map<String, Long> ordersByStatus = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getStatus().name(), // Статус заказа
                        Collectors.counting() // Количество заказов для каждого статуса
                ));

        model.addAttribute("dates", ordersByDate.keySet());
        model.addAttribute("orderCounts", ordersByDate.values());
        model.addAttribute("totalAmounts", totalByDate.values());
        model.addAttribute("statuses", ordersByStatus.keySet());
        model.addAttribute("statusCounts", ordersByStatus.values());

        return "restaurant/reports";
    }



}
