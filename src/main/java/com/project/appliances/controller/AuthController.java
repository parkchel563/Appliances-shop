package com.project.appliances.controller;

import com.project.appliances.dto.client.ClientRegistrationDto;
import com.project.appliances.service.interfaces.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/signUp")
    public String showSignUpPage(Model model) {
        model.addAttribute("clientRegistrationDto", new ClientRegistrationDto());
        return "auth/signUp";
    }

    @PostMapping("/signUp")
    public String register(@ModelAttribute("clientRegistrationDto") @Valid ClientRegistrationDto dto,
                           BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {
        if (bindingResult.hasErrors()) {
            return "auth/signUp";
        }

        try {
            authService.registerClient(dto);
            redirectAttributes.addFlashAttribute("successMessage", "signup.success");
        } catch (IllegalStateException e) {
            bindingResult.rejectValue("email", "validation.email.duplicate");
            return "auth/signUp";
        }

        return "redirect:/login";
    }
}
