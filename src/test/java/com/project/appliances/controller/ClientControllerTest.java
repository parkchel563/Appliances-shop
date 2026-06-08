package com.project.appliances.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.project.appliances.dto.client.ClientCreateDto;
import com.project.appliances.dto.client.ClientDto;
import com.project.appliances.dto.client.ClientSearchCriteria;
import com.project.appliances.dto.client.ClientUpdateProfileDto;
import com.project.appliances.service.interfaces.ClientService;
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

@WebMvcTest(ClientController.class)
@EnableMethodSecurity
@Import(UrlUtil.class)
class ClientControllerTest {
    private static final String EMPLOYEE_EMAIL = "employee@store.com";
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ClientService clientService;

    // GET /clients
    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void getAllClients_ShouldReturnViewAndModel() throws Exception {
        Page<ClientDto> mockPage = new PageImpl<>(List.of());

        when(clientService.findAll(any(ClientSearchCriteria.class), any(Pageable.class)))
                .thenReturn(mockPage);

        mockMvc.perform(get("/clients")
                                .param("page", "0")
                                .param("size", "6")
                                .param("sort", "id,asc"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/clientsPage"))
                .andExpect(model().attributeExists("clientsPage"))
                .andExpect(model().attributeExists("clients"))
                .andExpect(model().attributeExists("criteria"));

        verify(clientService).findAll(any(ClientSearchCriteria.class), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "client@store.com", authorities = "ROLE_CLIENT")
    void getAllClients_ShouldReturnForbidden_WhenUserIsClient() throws Exception {
        mockMvc.perform(get("/clients"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void getClientDetails_ShouldReturnViewAndModel() throws Exception {
        ClientUpdateProfileDto mockDto = new ClientUpdateProfileDto();
        when(clientService.getClientProfile(1L)).thenReturn(mockDto);

        mockMvc.perform(get("/clients/details/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/clientDetailsPage"))
                .andExpect(model().attribute("clientId", 1L))
                .andExpect(model().attribute("clientUpdateProfileDto", mockDto))
                .andExpect(model().attribute("currentPage", "/clients/details/1"));
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void updateClientDetails_ShouldReturnViewWithErrors_WhenDtoIsInvalid() throws Exception {
        mockMvc.perform(post("/clients/details/1")
                                .with(csrf())
                                .param("email", "")
                       )
                .andExpect(status().isOk())
                .andExpect(view().name("client/clientDetailsPage"))
                .andExpect(model().attributeHasErrors("clientUpdateProfileDto"));

        verify(clientService, never()).updateClientProfile(anyLong(), any());
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void updateClientDetails_ShouldReturnViewWithErrors_WhenEmailExists() throws Exception {
        doThrow(new IllegalArgumentException("Email exists"))
                .when(clientService).updateClientProfile(eq(1L), any(ClientUpdateProfileDto.class));

        mockMvc.perform(post("/clients/details/1")
                                .with(csrf())
                                .param("email", "taken@store.com")
                                .param("name", "John Doe")
                                .param("card", "1234-1234-1234")
                       )
                .andExpect(status().isOk())
                .andExpect(view().name("client/clientDetailsPage"))
                .andExpect(model().attributeHasFieldErrorCode("clientUpdateProfileDto", "email",
                                                              "validation.email.exist"));
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void updateClientDetails_ShouldRedirect_WhenSuccess() throws Exception {
        mockMvc.perform(post("/clients/details/1")
                                .with(csrf())
                                .param("email", "new@store.com")
                                .param("name", "John Doe")
                                .param("card", "1234-1234-1234")
                       )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients"));

        verify(clientService).updateClientProfile(eq(1L), any(ClientUpdateProfileDto.class));
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void deleteClient_ShouldRedirectWithSuccessFlash_WhenSuccess() throws Exception {
        mockMvc.perform(post("/clients/delete/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(flash().attribute("successMessage", "client.delete.success"));

        verify(clientService).deleteClientProfile(1L);
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void deleteClient_ShouldRedirectWithErrorFlash_WhenClientHasActiveOrders() throws Exception {
        doThrow(new IllegalStateException("Active orders"))
                .when(clientService).deleteClientProfile(1L);

        mockMvc.perform(post("/clients/delete/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "client.delete.active.orders"));
    }

    // POST /clients/details/{id}/generate-password
    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void generatePassword_ShouldRedirectWithFlashAttributes() throws Exception {
        when(clientService.generatePassword(1L)).thenReturn("NewPass123!");

        mockMvc.perform(post("/clients/details/1/generate-password").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients/details/1"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(flash().attribute("generatedPassword", "NewPass123!"));

        verify(clientService).generatePassword(1L);
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void createClient_ShouldReturnViewAndModel() throws Exception {
        mockMvc.perform(get("/clients/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("client/createClientPage"))
                .andExpect(model().attributeExists("clientCreateDto"))
                .andExpect(model().attribute("currentPage", "/clients/create"));
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void createClient_ShouldReturnViewWithErrors_WhenDtoIsInvalid() throws Exception {
        mockMvc.perform(post("/clients/create")
                                .with(csrf())
                                .param("email", "invalid")
                       )
                .andExpect(status().isOk())
                .andExpect(view().name("client/createClientPage"))
                .andExpect(model().attributeHasErrors("clientCreateDto"));

        verify(clientService, never()).createClient(any());
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void createClient_ShouldReturnViewWithErrors_WhenEmailExists() throws Exception {
        when(clientService.createClient(any(ClientCreateDto.class)))
                .thenThrow(new IllegalStateException("Email exists"));

        mockMvc.perform(post("/clients/create")
                                .with(csrf())
                                .param("email", "taken@store.com")
                                .param("name", "John")
                                .param("card", "1234-1234-1234")
                       )
                .andExpect(status().isOk())
                .andExpect(view().name("client/createClientPage"))
                .andExpect(model().attributeHasFieldErrorCode("clientCreateDto", "email", "validation.email.exist"));
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void createClient_ShouldRedirectWithFlashAttributes_WhenSuccess() throws Exception {
        when(clientService.createClient(any(ClientCreateDto.class))).thenReturn("GenPass777!");

        mockMvc.perform(post("/clients/create")
                                .with(csrf())
                                .param("email", "new@store.com")
                                .param("name", "John")
                                .param("card", "1234-1234-1234")
                       )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/clients/create"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(flash().attribute("generatedPassword", "GenPass777!"));
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void handleAccessDenied_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/clients/access-denied"))
                .andExpect(status().isForbidden());
    }


}
