package com.foodDelivery.foodDeliveryCoursework.controller;

import com.foodDelivery.foodDeliveryCoursework.model.Menu;
import com.foodDelivery.foodDeliveryCoursework.model.Order;
import com.foodDelivery.foodDeliveryCoursework.model.OrderItem;
import com.foodDelivery.foodDeliveryCoursework.model.User;
import com.foodDelivery.foodDeliveryCoursework.repository.MenuRepository;
import com.foodDelivery.foodDeliveryCoursework.repository.OrderItemRepository;
import com.foodDelivery.foodDeliveryCoursework.repository.OrderRepository;
import com.foodDelivery.foodDeliveryCoursework.service.OrderService;
import com.foodDelivery.foodDeliveryCoursework.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/client")
public class ClientController {

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;

    // Отображение страницы меню
    @GetMapping("/menus")
    public String getAllMenus(Model model) {
        model.addAttribute("menus", menuRepository.findAll());
        return "client/menus";
    }

    @GetMapping("/orders")
    public String getClientOrders(Model model) {
        User currentUser = userService.getCurrentUser();
        List<Order> orders = orderService.getOrdersForUser(currentUser.getId());
        model.addAttribute("orders", orders);
        return "client/orders";
    }

    // Отображение страницы заказа
    @GetMapping("/order")
    public String getOrderPage(@RequestParam("menuId") Long menuId, Model model) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("Меню с ID " + menuId + " не найдено"));
        model.addAttribute("menu", menu);
        model.addAttribute("totalPrice", menu.getPrice());
        return "client/order";
    }

    // Подтверждение заказа
    @PostMapping("/order/confirm")
    public String confirmOrder(@RequestParam("menuId") Long menuId,
                               @RequestParam("quantity") int quantity) {
        if (quantity < 1 || quantity > 50) {
            throw new IllegalArgumentException("Количество должно быть от 1 до 50.");
        }

        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("Меню с ID " + menuId + " не найдено"));

        User currentUser = userService.getCurrentUser(); // Получение текущего пользователя

        // Создаем заказ
        Order order = new Order();
        order.setUser(currentUser);
        order.setRestaurant(menu.getRestaurant());
        order.setStatus(Order.Status.NEW);
        order.setTotalAmount(menu.getPrice().multiply(BigDecimal.valueOf(quantity))); // Итоговая цена
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order = orderRepository.save(order);

        // Создаем элемент заказа
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(order);
        orderItem.setMenuItem(menu);
        orderItem.setQuantity(quantity); // Установить выбранное количество
        orderItem.setPrice(menu.getPrice()); // Цена за единицу
        orderItemRepository.save(orderItem);

        return "redirect:/client/orders";
    }

}
