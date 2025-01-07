package com.foodDelivery.foodDeliveryCoursework.repository;

import com.foodDelivery.foodDeliveryCoursework.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
    List<Order> findByRestaurantId(Long restaurantId);

    List<Order> findByCourierIsNull();

    // Найти заказы со статусом NEW и courierId == null
    List<Order> findByStatusAndCourierIsNull(Order.Status status);

    // Найти заказы, назначенные курьеру
    List<Order> findByCourierId(Long courierId);

    List<Order> findAllByCourierIdAndStatus(Long courierId, Order.Status status);

    @Modifying
    @Query("UPDATE Order o SET o.courier = null WHERE o.courier.id = :courierId")
    void updateCourierIdToNull(@Param("courierId") Long courierId);

    @Modifying
    @Query("UPDATE Order o SET o.user = null WHERE o.user.id = :clientId")
    void updateClientIdToNull(@Param("clientId") Long clientId);

    @Modifying
    @Query("UPDATE Order o SET o.restaurant = null WHERE o.restaurant.id = :restaurantId")
    void updateRestaurantIdToNull(@Param("restaurantId") Long restaurantId);

    // Подсчёт количества заказов для каждого ресторана
    Long countByRestaurantId(Long restaurantId);

    // Сумма выручки для каждого ресторана
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.restaurant.id = :restaurantId")
    Double sumRevenueByRestaurantId(@Param("restaurantId") Long restaurantId);

    // Получение количества заказов по пользователям
    @Query("SELECT o.user.username, COUNT(o) FROM Order o GROUP BY o.user.username")
    List<Object[]> getUserOrderCounts();

}
