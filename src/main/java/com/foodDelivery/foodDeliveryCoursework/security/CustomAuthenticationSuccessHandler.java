package com.foodDelivery.foodDeliveryCoursework.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            response.sendRedirect("/admin/orders");
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CLIENT"))) {
            response.sendRedirect("/client/menus");
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_COURIER"))) {
            response.sendRedirect("/courier/orders");
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_RESTAURANT"))) {
            response.sendRedirect("/restaurant/orders");
        } else {
            response.sendRedirect("/not-found");
        }
    }
}
