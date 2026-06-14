package com.project.appliances.controller;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.project.appliances.dto.orders.OrdersDto;
import com.project.appliances.service.interfaces.CartService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CartController.class)
@EnableMethodSecurity
class CartControllerTest {
    private static final String CLIENT_EMAIL = "client@store.com";
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CartService cartService;


    @Test
    @WithMockUser(username = CLIENT_EMAIL, authorities = "ROLE_CLIENT")
    void showCart_ShouldReturnViewAndModel() throws Exception {
        OrdersDto mockCart = new OrdersDto();
        when(cartService.getCart(CLIENT_EMAIL)).thenReturn(mockCart);

        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart/cart"))
                .andExpect(model().attributeExists("cart"))
                .andExpect(model().attribute("cart", mockCart));

        verify(cartService).getCart(CLIENT_EMAIL);
    }

    @Test
    @WithMockUser(username = "employee@store.com", authorities = "ROLE_EMPLOYEE")
    void showCart_ShouldReturnForbidden_WhenUserIsNotClient() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL, authorities = "ROLE_CLIENT")
    void addToCart_ShouldRedirectWithSuccessFlash() throws Exception {
        mockMvc.perform(post("/cart/add/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(flash().attribute("successMessage", "cart.added"));

        verify(cartService).addToCart(CLIENT_EMAIL, 1L);
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL, authorities = "ROLE_CLIENT")
    void removeFromCart_ShouldRedirectToCart() throws Exception {
        mockMvc.perform(post("/cart/remove/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(cartService).removeFromCart(CLIENT_EMAIL, 1L);
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL, authorities = "ROLE_CLIENT")
    void updateQuantity_ShouldRedirectToCart() throws Exception {
        mockMvc.perform(post("/cart/update/1")
                                .with(csrf())
                                .param("quantity", "5")
                       )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart"));

        verify(cartService).updateQuantity(CLIENT_EMAIL, 1L, 5L);
    }

    @Test
    @WithMockUser(username = CLIENT_EMAIL, authorities = "ROLE_CLIENT")
    void checkout_ShouldRedirectWithSuccessFlash() throws Exception {
        mockMvc.perform(post("/cart/checkout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(flash().attribute("successMessage", "order.submit.success"));

        verify(cartService).submitOrder(CLIENT_EMAIL);
    }
}
