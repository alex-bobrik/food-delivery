package com.foodDelivery.foodDeliveryCoursework.service;

import com.foodDelivery.foodDeliveryCoursework.model.Order;
import com.foodDelivery.foodDeliveryCoursework.model.User;
import com.foodDelivery.foodDeliveryCoursework.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }

    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> getOrdersForUser(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> findOrdersWithNoCourier() {
        return orderRepository.findByCourierIsNull();
    }

    public Order findById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    public void save(Order order) {
        orderRepository.save(order);
    }

    public List<Order> findNewOrders() {
        return orderRepository.findByStatusAndCourierIsNull(Order.Status.NEW);
    }

    public List<Order> findOrdersByCourier(Long courierId) {
        return orderRepository.findByCourierId(courierId);
    }

    public List<Order> findOrdersByRestaurantId(Long restaurantId) {
        return orderRepository.findByRestaurantId(restaurantId);
    }

    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    public void updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.setStatus(Order.Status.valueOf(status.toUpperCase()));
        orderRepository.save(order);
    }

    @Transactional
    public void detachCourierFromOrders(Long courierId) {
        orderRepository.updateCourierIdToNull(courierId);
    }

    @Transactional
    public void detachClientFromOrders(Long clientId) {
        orderRepository.updateClientIdToNull(clientId);
    }

    @Transactional
    public void detachRestaurantFromOrders(Long restaurantId) {
        orderRepository.updateRestaurantIdToNull(restaurantId);
    }

    public List<Order> findOrdersByCourierAndStatus(Long courierId, Order.Status status) {
        return orderRepository.findAllByCourierIdAndStatus(courierId, status);
    }

    public Map<String, Long> getOrdersGroupedByDate(List<Order> orders) {
        Map<String, Long> ordersByDate = new HashMap<>();
        for (Order order : orders) {
            String date = order.getCreatedAt().toLocalDate().toString();
            ordersByDate.put(date, ordersByDate.getOrDefault(date, 0L) + 1);
        }
        return ordersByDate;
    }

    public Map<String, BigDecimal> getTotalByDate(List<Order> orders) {
        Map<String, BigDecimal> totalByDate = new HashMap<>();
        for (Order order : orders) {
            String date = order.getCreatedAt().toLocalDate().toString();
            totalByDate.put(date, totalByDate.getOrDefault(date, BigDecimal.ZERO).add(order.getTotalAmount()));
        }
        return totalByDate;
    }
}
