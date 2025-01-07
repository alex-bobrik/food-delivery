package com.foodDelivery.foodDeliveryCoursework.service;

import com.foodDelivery.foodDeliveryCoursework.model.Menu;
import com.foodDelivery.foodDeliveryCoursework.repository.MenuRepository;
import com.foodDelivery.foodDeliveryCoursework.repository.OrderItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MenuService {

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    public MenuService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    public List<Menu> findMenusByRestaurantId(Long restaurantId) {
        return menuRepository.findByRestaurantId(restaurantId);
    }

    public Optional<Menu> findById(Long id) {
        return menuRepository.findById(id);
    }

    public void save(Menu menu) {
        menuRepository.save(menu);
    }

    public void deleteById(Long id) {
        menuRepository.deleteById(id);
    }

    @Transactional
    public void deleteByRestaurantId(Long restaurantId) {
        List<Menu> menus = menuRepository.findByRestaurantId(restaurantId);
        for (Menu menu : menus) {
            orderItemRepository.detachMenuFromOrderItems(menu.getId());

            menuRepository.delete(menu);
        }
    }
}
