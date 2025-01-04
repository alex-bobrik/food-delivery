package com.foodDelivery.foodDeliveryCoursework.repository;

import com.foodDelivery.foodDeliveryCoursework.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Modifying
    @Query("UPDATE OrderItem oi SET oi.menuItem = null WHERE oi.menuItem.id = :menuId")
    void detachMenuFromOrderItems(@Param("menuId") Long menuId);
}
