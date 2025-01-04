package com.foodDelivery.foodDeliveryCoursework.service;

import com.foodDelivery.foodDeliveryCoursework.model.Order;
import com.foodDelivery.foodDeliveryCoursework.model.User;
import com.foodDelivery.foodDeliveryCoursework.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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

    // Получаем все заказы, у которых courierId == null
    public List<Order> findOrdersWithNoCourier() {
        return orderRepository.findByCourierIsNull();
    }

    // Найти заказ по ID
    public Order findById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    // Сохранить заказ
    public void save(Order order) {
        orderRepository.save(order);
    }

    public List<Order> findNewOrders() {
        // Найти заказы со статусом NEW и courierId == null
        return orderRepository.findByStatusAndCourierIsNull(Order.Status.NEW);
    }

    public List<Order> findOrdersByCourier(Long courierId) {
        // Найти заказы, назначенные текущему курьеру
        return orderRepository.findByCourierId(courierId);
    }

    public List<Order> findOrdersByRestaurantId(Long restaurantId) {
        return orderRepository.findByRestaurantId(restaurantId);
    }

    public List<Order> findAllOrders() {
        return orderRepository.findAll();
    }

    // Метод для обновления статуса заказа
    public void updateOrderStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        order.setStatus(Order.Status.valueOf(status.toUpperCase()));
        orderRepository.save(order);
    }

    @Transactional
    public void detachCourierFromOrders(Long courierId) {
        // Устанавливаем COURIER_ID = null для заказов, связанных с курьером
        orderRepository.updateCourierIdToNull(courierId);
    }

    @Transactional
    public void detachClientFromOrders(Long clientId) {
        // Устанавливаем USER_ID = null для заказов, связанных с клиентом
        orderRepository.updateClientIdToNull(clientId);
    }

    @Transactional
    public void detachRestaurantFromOrders(Long restaurantId) {
        // Устанавливаем RESTAURANT_ID = null для заказов, связанных с рестораном
        orderRepository.updateRestaurantIdToNull(restaurantId);
    }
}
