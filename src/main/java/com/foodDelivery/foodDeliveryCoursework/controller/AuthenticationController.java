package com.foodDelivery.foodDeliveryCoursework.controller;

import com.foodDelivery.foodDeliveryCoursework.model.User;
import com.foodDelivery.foodDeliveryCoursework.security.JWTUtil;
import com.foodDelivery.foodDeliveryCoursework.service.AuthService;
import com.foodDelivery.foodDeliveryCoursework.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller; // Используем @Controller вместо @RestController
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;

@Controller // Это аннотация для контроллеров, которые возвращают HTML
@RequestMapping("/auth") // Рекомендуется использовать префикс для аутентификации
public class AuthenticationController {

    private final UserService userService;
    private final AuthService authenticationService;

    private final JWTUtil jwtUtil;

    @Autowired
    public AuthenticationController(UserService userService, JWTUtil jwtUtil, AuthService authenticationService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.authenticationService = authenticationService;
    }

//    @GetMapping("/not-found")
//    public String notFoundPage() {
//        System.out.println("NOT FOUND PAGE");
//        return "notFound"; // Отображает страницу login.html из папки templates
//    }

    // Страница логина, которая будет отображаться при GET-запросе
    @GetMapping("/login")
    public String loginPage() {
        System.out.println("LOGIN PAGE GET");
        return "login"; // Отображает страницу login.html из папки templates
    }

    // Страница регистрации
    @GetMapping("/register")
    public String registerPage() {
        System.out.println("REGISTER PAGE GET");
        return "register"; // Отображает страницу register.html из папки templates
    }

    @PostMapping("/register")
    public String register(@RequestParam String email, @RequestParam String username, @RequestParam String password, Model model) {
        try {
            System.out.println("NEW REGISTER");
            userService.registerUser(email, username, password);
            System.out.println("REGISTERED");
            return "redirect:/auth/login";  // Перенаправление на страницу логина
        } catch (RuntimeException e) {
            // Обрабатываем ошибку и передаем на страницу с ошибкой
            model.addAttribute("error", e.getMessage());
            return "register"; // Если ошибка, возвращаем на страницу регистрации
        }
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password) {
        System.out.println("TRYING TO LOGIN");
        User user = userService.findByUsername(username);

        boolean authenticated = authenticationService.authenticate(username, password);

        if (!authenticated) {
            return "redirect:/auth/login?error=true";
        }

        return "redirect:/";
    }

    public String getUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        for (GrantedAuthority authority : authorities) {
            return authority.getAuthority();
        }

        return "ROLE_CLIENT";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        // Инвалидируем текущую сессию
        session.invalidate();

        // Удаляем данные аутентификации
        SecurityContextHolder.clearContext();

        // Дополнительно можно удалить JWT из куков, если используется
        return "redirect:/auth/login?logout=true"; // Перенаправляем на страницу логина
    }
}
