package com.foodDelivery.foodDeliveryCoursework.repository;

import com.foodDelivery.foodDeliveryCoursework.model.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
}
