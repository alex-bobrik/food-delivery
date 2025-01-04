package com.foodDelivery.foodDeliveryCoursework.service;

import com.foodDelivery.foodDeliveryCoursework.model.Menu;
import com.foodDelivery.foodDeliveryCoursework.model.Restaurant;
import com.foodDelivery.foodDeliveryCoursework.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RestaurantService {

    @Autowired
    private RestaurantRepository restaurantRepository;

    public Restaurant findById(Long id) {
        return restaurantRepository.findById(id).orElse(null);
    }

    public void delete(Restaurant restaurant) {
        restaurantRepository.delete(restaurant);
    }
}
