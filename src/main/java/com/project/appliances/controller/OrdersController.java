package com.project.appliances.controller;

import com.project.appliances.dto.orders.OrdersAdminDto;
import com.project.appliances.dto.orders.OrdersSearchCriteria;
import com.project.appliances.model.OrderStatus;
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
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
@PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
public class OrdersController {

    private final OrdersService ordersService;

    @GetMapping
    public String showOrders(@ModelAttribute("criteria") OrdersSearchCriteria criteria,
                             @PageableDefault(sort = "id", direction = Sort.Direction.ASC, size = 4) Pageable pageable,
                             Model model) {
        Page<OrdersAdminDto> ordersPage = ordersService.findAllOrders(criteria, pageable);

        model.addAttribute("statuses", OrderStatus.values());

        model.addAttribute("ordersPage", ordersPage);
        model.addAttribute("orders", ordersPage.getContent());

        return "orders/ordersPage";
    }
    
    @GetMapping("/details/{id}")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
    public String showOrderDetails(@PathVariable Long id, Model model) {
        model.addAttribute("order", ordersService.findOrderDetailsById(id));
        return "orders/orderDetailsPage";
    }

    @PostMapping("/{id}/take")
    public String takeOrder(@PathVariable Long id, Authentication auth) {
        ordersService.takeOrderInWork(id, auth.getName());
        return "redirect:/orders/details/" + id;
    }

    @PostMapping("/{id}/ready")
    public String markReady(@PathVariable Long id, Authentication auth) {
        ordersService.markAsReady(id, auth.getName());
        return "redirect:/orders/details/" + id;
    }

    @PostMapping("/{id}/complete")
    public String complete(@PathVariable Long id, Authentication auth) {
        ordersService.markAsCompleted(id, auth.getName());
        return "redirect:/orders/details/" + id;
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, Authentication auth) {
        ordersService.markAsCancelled(id, auth.getName());
        return "redirect:/orders/details/" + id;
    }
}
