package com.project.appliances.controller;

import com.project.appliances.service.interfaces.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
@PreAuthorize("hasAuthority('ROLE_CLIENT')")
public class CartController {
    private final CartService cartService;

    @GetMapping
    public String showCart(Model model,
                           Authentication authentication) {
        model.addAttribute("cart", cartService.getCart(authentication.getName()));
        return "cart/cart";
    }

    @PostMapping("/add/{applianceId}")
    public String addToCart(@PathVariable Long applianceId,
                            Authentication authentication, RedirectAttributes redirectAttributes) {
        cartService.addToCart(authentication.getName(), applianceId);

        redirectAttributes.addFlashAttribute("successMessage", "cart.added");

        return "redirect:/";
    }

    @PostMapping("/remove/{orderRowId}")
    public String removeFromCart(@PathVariable Long orderRowId,
                                 Authentication authentication) {
        cartService.removeFromCart(authentication.getName(), orderRowId);
        return "redirect:/cart";
    }

    @PostMapping("update/{orderRowId}")
    public String updateQuantity(@PathVariable Long orderRowId,
                                 @RequestParam Long quantity,
                                 Authentication authentication) {
        cartService.updateQuantity(authentication.getName(), orderRowId, quantity);

        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String checkout(Authentication authentication,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        cartService.submitOrder(authentication.getName());
        redirectAttributes.addFlashAttribute("successMessage", "order.submit.success");
        return "redirect:/";
    }
}
