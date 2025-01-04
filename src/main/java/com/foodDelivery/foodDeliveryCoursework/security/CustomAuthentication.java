package com.foodDelivery.foodDeliveryCoursework.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class CustomAuthentication implements Authentication {

    private final String username;
    private boolean authenticated = true;

    public CustomAuthentication(String username) {
        this.username = username;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null; // Верните роли, если они есть
    }

    @Override
    public Object getCredentials() {
        return null; // Возвращайте данные аутентификации, если нужно
    }

    @Override
    public Object getDetails() {
        return null; // Дополнительные данные
    }

    @Override
    public Object getPrincipal() {
        return username; // Это будет использоваться для идентификации пользователя
    }

    @Override
    public boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        this.authenticated = isAuthenticated;
    }

    @Override
    public String getName() {
        return username;
    }
}
