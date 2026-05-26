package com.project.appliances.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.project.appliances.dto.client.ClientRegistrationDto;
import com.project.appliances.security.config.SecurityConfig;
import com.project.appliances.security.handler.CustomAuthenticationFailureHandler;
import com.project.appliances.security.handler.CustomAuthenticationSuccessHandler;
import com.project.appliances.service.interfaces.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @MockitoBean
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
    @MockitoBean
    private CustomAuthenticationFailureHandler customAuthenticationFailureHandler;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthService authService;

    // GET /login
    @Test
    void loginPage_ShouldReturnView() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    // GET /signUp
    @Test
    void showSignUpPage_ShouldReturnViewAndModel() throws Exception {
        mockMvc.perform(get("/signUp").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signUp"))
                .andExpect(model().attributeExists("clientRegistrationDto"));
    }

    // POST /signUp
    @Test
    void register_ShouldReturnViewWithErrors_WhenDtoIsInvalid() throws Exception {
        mockMvc.perform(post("/signUp")
                                .with(csrf())
                                .param("email", "invalid-email")
                       )
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signUp"))
                .andExpect(model().attributeHasErrors("clientRegistrationDto"));

        verify(authService, never()).registerClient(any());
    }

    @Test
    void register_ShouldReturnViewWithErrors_WhenEmailExists() throws Exception {
        doThrow(new IllegalStateException("Email already taken"))
                .when(authService).registerClient(any(ClientRegistrationDto.class));

        mockMvc.perform(post("/signUp")
                                .with(csrf())
                                .param("name", "John Doe")
                                .param("email", "taken@gmail.com")
                                .param("password", "ValidPass123!")
                                .param("confirmPassword", "ValidPass123!")
                                .param("card", "1234-1234")
                       )
                .andExpect(status().isOk())
                .andExpect(view().name("auth/signUp"))
                .andExpect(model().attributeHasFieldErrorCode("clientRegistrationDto", "email",
                                                              "validation.email.duplicate"));

        verify(authService).registerClient(any(ClientRegistrationDto.class));
    }

    @Test
    void register_ShouldRedirectToLoginWithSuccessFlash_WhenSuccess() throws Exception {
        mockMvc.perform(post("/signUp")
                                .with(csrf())
                                .param("name", "John Doe")
                                .param("email", "taken@store.com")
                                .param("password", "ValidPass123!")
                                .param("confirmPassword", "ValidPass123!")
                                .param("card", "1234-1234")
                       )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(flash().attribute("successMessage", "signup.success"));

        verify(authService).registerClient(any(ClientRegistrationDto.class));
    }

    @Test
    void login_ShouldAuthenticateSuccessfully() throws Exception {
        mockMvc.perform(formLogin("/login")
                                .user("username", "client@test.com")
                                .password("password", "password123"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void register_ShouldFail_WhenPasswordTooShort() throws Exception {
        mockMvc.perform(post("/signUp")
                                .with(csrf())
                                .param("name", "John")
                                .param("email", "john@test.com")
                                .param("password", "123")
                                .param("confirmPassword", "123"))
                .andExpect(status().isOk())
                .andExpect(model()
                                   .attributeHasFieldErrors("clientRegistrationDto", "password"));
    }

    @Test
    void register_ShouldFail_WhenUsernameTooShort() throws Exception {
        mockMvc.perform(post("/signUp")
                                .with(csrf())
                                .param("name", "ab")
                                .param("email", "john@test.com")
                                .param("password", "password123")
                                .param("confirmPassword", "password123"))
                .andExpect(status().isOk())
                .andExpect(model()
                                   .attributeHasFieldErrors("clientRegistrationDto", "name"));
    }

    @Test
    void register_ShouldFail_WhenPasswordTooLong() throws Exception {
        mockMvc.perform(post("/signUp")
                                .with(csrf())
                                .param("name", "John")
                                .param("email", "john@test.com")
                                .param("password", "123")
                                .param("confirmPassword", "123"))
                .andExpect(status().isOk())
                .andExpect(model()
                                   .attributeHasFieldErrors("clientRegistrationDto", "password"));
    }

    @Test
    void register_ShouldFail_WhenUsernameTooLong() throws Exception {
        mockMvc.perform(post("/signUp")
                                .with(csrf())
                                .param("name", "a".repeat(31))
                                .param("email", "john@test.com")
                                .param("password", "password123")
                                .param("confirmPassword", "password123"))
                .andExpect(status().isOk())
                .andExpect(model()
                                   .attributeHasFieldErrors("clientRegistrationDto", "name"));
    }

    @Test
    void register_ShouldFail_WhenPasswordsDoNotMatch() throws Exception {
        mockMvc.perform(post("/signUp")
                                .with(csrf())
                                .param("name", "John")
                                .param("email", "john@test.com")
                                .param("password", "password123")
                                .param("confirmPassword", "password1234"))
                .andExpect(status().isOk())
                .andExpect(model()
                                   .attributeHasFieldErrors("clientRegistrationDto", "name"));
    }
}
