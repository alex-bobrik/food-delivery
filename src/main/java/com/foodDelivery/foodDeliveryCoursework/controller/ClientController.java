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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping("/order")
    public String getOrderPage(@RequestParam("menuId") Long menuId, Model model) {
        Menu menu = menuRepository.findById(menuId)
                .orElseThrow(() -> new IllegalArgumentException("Меню с ID " + menuId + " не найдено"));
        model.addAttribute("menu", menu);
        model.addAttribute("totalPrice", menu.getPrice());
        return "client/order";
    }

    @PostMapping("/order/confirm")
    public String confirmOrder(@RequestParam("menuId") Long menuId,
                               @RequestParam("quantity") int quantity,
                               @RequestParam("deliveryAddress") String deliveryAddress,
                               RedirectAttributes redirectAttributes
    ) {
        try {
            Menu menu = menuRepository.findById(menuId)
                    .orElseThrow(() -> new IllegalArgumentException("Меню с ID " + menuId + " не найдено"));

            if (quantity < 1 || quantity > 50) {
                throw new IllegalArgumentException("Количество должно быть от 1 до 50.");
            }

            if (quantity > menu.getQuantity()) {
                throw new IllegalArgumentException("Недостаточно товара на складе. Доступное количество: " + menu.getQuantity());
            }

            User currentUser = userService.getCurrentUser();

            Order order = new Order();
            order.setUser(currentUser);
            order.setRestaurant(menu.getRestaurant());
            order.setStatus(Order.Status.NEW);
            order.setTotalAmount(menu.getPrice().multiply(BigDecimal.valueOf(quantity)));
            order.setCreatedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            order.setDeliveryAddress(deliveryAddress);

            order = orderRepository.save(order);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItem(menu);
            orderItem.setQuantity(quantity);
            orderItem.setPrice(menu.getPrice());
            orderItemRepository.save(orderItem);

            menu.setQuantity(menu.getQuantity() - quantity);
            menuRepository.save(menu);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/error";
        }

        return "redirect:/client/orders";
    }

}
