package com.project.appliances.controller;

import com.project.appliances.dto.orders.OrdersAdminDto;
import com.project.appliances.service.interfaces.OrdersService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/my/orders")
@PreAuthorize("hasAuthority('ROLE_CLIENT')")
public class ClientOrdersController {
    private final OrdersService ordersService;

    @GetMapping
    public String showMyOrders(Model model,
                               Authentication authentication,
                               @PageableDefault(size = 5, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<OrdersAdminDto> ordersPage = ordersService.findAllOrdersByClientEmail(authentication.getName(), pageable);

        model.addAttribute("ordersPage", ordersPage);
        model.addAttribute("orders", ordersPage.getContent());
        model.addAttribute("currentPage", "/my/orders");
        return "orders/myOrdersPage";
    }

    @GetMapping("/{id}")
    public String showMyOrderDetails(@PathVariable Long id,
                                     Model model,
                                     Authentication authentication) {
        model.addAttribute("order", ordersService.findOrderDetails(id, authentication.getName()));
        model.addAttribute("currentPage", "/my/orders/" + id);
        return "orders/myOrderDetailsPage";
    }

    @PostMapping("/{id}/cancel")
    public String cancelMyOrder(@PathVariable Long id,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        try {
            ordersService.cancelOrder(id, authentication.getName());
            redirectAttributes.addFlashAttribute("successMessage", "order.canceled.success");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/my/orders/" + id;
    }
}
