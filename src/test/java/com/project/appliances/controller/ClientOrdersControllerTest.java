package com.project.appliances.controller;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


import com.project.appliances.dto.orders.OrderDetailsDto;
import com.project.appliances.dto.orders.OrdersAdminDto;
import com.project.appliances.service.interfaces.OrdersService;
import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ClientOrdersController.class)
@EnableMethodSecurity
class ClientOrdersControllerTest {
    private static final String CLIENT_EMAIL = "client@store.com";
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private OrdersService ordersService;

    @Test
    @WithMockUser(username = CLIENT_EMAIL, authorities = "ROLE_CLIENT")
    void showMyOrders_ShouldReturnViewAndModel() throws Exception {
        Page<OrdersAdminDto> ordersPage = new PageImpl<>(new ArrayList<>());

        when(ordersService.findAllOrdersByClientEmail(eq(CLIENT_EMAIL), any(Pageable.class)))
                .thenReturn(ordersPage);

        mockMvc.perform(get("/my/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/myOrdersPage"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(model().attributeExists("ordersPage"));

        verify(ordersService).findAllOrdersByClientEmail(eq(CLIENT_EMAIL), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL, authorities = "ROLE_EMPLOYEE")
    void showMyOrders_ShouldReturnErrorPageWhenAuthIsNotClient() throws Exception {
        mockMvc.perform(get("/my/orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL, authorities = "ROLE_CLIENT")
    void showMyOrderDetails_ShouldReturnViewAndModel() throws Exception {
        when(ordersService.findOrderDetails(1L, CLIENT_EMAIL))
                .thenReturn(new OrderDetailsDto());

        mockMvc.perform(get("/my/orders/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/myOrderDetailsPage"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attributeExists("currentPage"));

        verify(ordersService).findOrderDetails(1L, CLIENT_EMAIL);
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL, authorities = "ROLE_EMPLOYEE")
    void showMyOrdersDetails_ShouldReturnErrorPageWhenAuthIsNotClient() throws Exception {
        mockMvc.perform(get("/my/orders/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL, authorities = "ROLE_CLIENT")
    void cancelMyOrder_ShouldRedirectWithSuccessFlash_WhenSuccess() throws Exception {
        mockMvc.perform(post("/my/orders/1/cancel").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my/orders/1"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(flash().attribute("successMessage", "order.canceled.success"));

        verify(ordersService).cancelOrder(1L, CLIENT_EMAIL);
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL, authorities = "ROLE_CLIENT")
    void cancelMyOrder_ShouldRedirectWithErrorFlash_WhenIllegalArgumentExceptionThrown() throws Exception {
        String errorMessage = "Cannot cancel order in current status";
        doThrow(new IllegalArgumentException(errorMessage))
                .when(ordersService).cancelOrder(1L, CLIENT_EMAIL);

        mockMvc.perform(post("/my/orders/1/cancel").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my/orders/1"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", errorMessage));

        verify(ordersService).cancelOrder(1L, CLIENT_EMAIL);
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL, authorities = "ROLE_EMPLOYEE")
    void cancelMyOrder_ShouldReturnErrorPageWhenAuthIsNotClient() throws Exception {
        mockMvc.perform(post("/my/orders/1/cancel").with(csrf()))
                .andExpect(status().isForbidden());
    }

}
