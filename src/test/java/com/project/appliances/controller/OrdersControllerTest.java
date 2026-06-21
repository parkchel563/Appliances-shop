package com.project.appliances.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.project.appliances.dto.orders.OrderDetailsDto;
import com.project.appliances.dto.orders.OrdersAdminDto;
import com.project.appliances.dto.orders.OrdersSearchCriteria;
import com.project.appliances.service.interfaces.OrdersService;
import com.project.appliances.util.UrlUtil;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(OrdersController.class)
@EnableMethodSecurity
@Import({UrlUtil.class})
class OrdersControllerTest {
    private static final String EMPLOYEE_EMAIL = "employee@store.com";
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private OrdersService ordersService;

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void showOrders_ShouldReturnViewAndModel_whenUserIsEmployee() throws Exception {
        Page<OrdersAdminDto> mockPage = new PageImpl<>(List.of());

        when(ordersService.findAllOrders(any(OrdersSearchCriteria.class), any(Pageable.class)))
                .thenReturn(mockPage);

        mockMvc.perform(get("/orders")
                                .param("page", "0")
                                .param("size", "10")
                                .param("sort", "id,desc")
                       )
                .andExpect(status().isOk())
                .andExpect(view().name("orders/ordersPage"))
                .andExpect(model().attributeExists("statuses"))
                .andExpect(model().attributeExists("ordersPage"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(model().attributeExists("criteria"));

        verify(ordersService).findAllOrders(any(OrdersSearchCriteria.class), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "client@store.com", authorities = "ROLE_CLIENT")
    void showOrders_ShouldReturnForbidden_WhenUserIsClient() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void showOrderDetails_ShouldReturnViewAndModel() throws Exception {
        OrderDetailsDto dto = new OrderDetailsDto();

        when(ordersService.findOrderDetailsById(1L)).thenReturn(dto);

        mockMvc.perform(get("/orders/details/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders/orderDetailsPage"))
                .andExpect(model().attribute("order", dto));

        verify(ordersService).findOrderDetailsById(1L);
    }


    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void takeOrder_ShouldRedirectToDetails() throws Exception {
        mockMvc.perform(post("/orders/1/take").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/details/1"));

        verify(ordersService).takeOrderInWork(1L, EMPLOYEE_EMAIL);
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void markReady_ShouldRedirectToDetails() throws Exception {
        mockMvc.perform(post("/orders/2/ready").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/details/2"));

        verify(ordersService).markAsReady(2L, EMPLOYEE_EMAIL);
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void complete_ShouldRedirectToDetails() throws Exception {
        mockMvc.perform(post("/orders/3/complete").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/details/3"));

        verify(ordersService).markAsCompleted(3L, EMPLOYEE_EMAIL);
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void cancel_ShouldRedirectToDetails() throws Exception {
        mockMvc.perform(post("/orders/4/cancel").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/details/4"));

        verify(ordersService).markAsCancelled(4L, EMPLOYEE_EMAIL);
    }
}
