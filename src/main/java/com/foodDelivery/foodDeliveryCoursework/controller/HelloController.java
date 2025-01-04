package com.foodDelivery.foodDeliveryCoursework.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
public class HelloController {

    @GetMapping("/")
//    @PreAuthorize("hasRole('ADMIN')")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        // Получение текущего пользователя
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Имя пользователя
        String username = authentication.getName();

        // Получение ролей
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        // Вывод ролей в консоль (или используйте их в логике)
        for (GrantedAuthority authority : authorities) {
            System.out.println("Role: " + authority.getAuthority());
        }

        return String.format("Hello %s!", name);
    }
}
