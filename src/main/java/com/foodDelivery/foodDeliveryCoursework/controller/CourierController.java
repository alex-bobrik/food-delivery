package com.foodDelivery.foodDeliveryCoursework.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.foodDelivery.foodDeliveryCoursework.model.Order;
import com.foodDelivery.foodDeliveryCoursework.model.User;
import com.foodDelivery.foodDeliveryCoursework.service.OrderService;
import com.foodDelivery.foodDeliveryCoursework.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/courier")
public class CourierController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @GetMapping("/new-orders")
    public String newOrdersPage(Model model) {
        List<Order> orders = orderService.findNewOrders();
        model.addAttribute("orders", orders);
        return "courier/new-orders";
    }

    @GetMapping("/orders")
    public String assignedOrdersPage(Model model) {
        User courier = this.userService.getCurrentUser();
        List<Order> orders = orderService.findOrdersByCourier(courier.getId());
        model.addAttribute("orders", orders);
        return "courier/assigned-orders";
    }

    @PostMapping("/assign/{orderId}")
    public String assignOrder(@PathVariable Long orderId, RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.findById(orderId);
            if (order != null && order.getCourier() == null) {
                User courier = this.userService.getCurrentUser();
                order.setCourier(courier);
                order.setStatus(Order.Status.IN_PROGRESS);
                orderService.save(order);
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/error";
        }
        return "redirect:/courier/orders";
    }

    @PostMapping("/complete/{orderId}")
    public String completeOrder(@PathVariable Long orderId) {
        Order order = orderService.findById(orderId);
        User courier = this.userService.getCurrentUser();
        if (order != null && order.getCourier().getId().equals(courier.getId()) && order.getStatus() == Order.Status.IN_PROGRESS) {
            order.setStatus(Order.Status.DELIVERED);
            order.setUpdatedAt(LocalDateTime.now());
            orderService.save(order);
        }

        return "redirect:/courier/orders";
    }

    @GetMapping("/reports")
    public String courierReportsPage(Model model) throws JsonProcessingException {
        User courier = this.userService.getCurrentUser();
        List<Order> deliveredOrders = orderService.findOrdersByCourierAndStatus(courier.getId(), Order.Status.DELIVERED);

        Map<String, Long> ordersByDate = deliveredOrders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getUpdatedAt().toLocalDate().toString(),
                        Collectors.counting()
                ));

        model.addAttribute("dates", ordersByDate.keySet());
        model.addAttribute("orderCounts", ordersByDate.values());

        return "courier/reports";
    }
}
