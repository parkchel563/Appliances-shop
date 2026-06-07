package com.project.appliances.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.project.appliances.dto.manufacturer.ManufacturerCreateDto;
import com.project.appliances.dto.manufacturer.ManufacturerDto;
import com.project.appliances.dto.manufacturer.ManufacturerSearchCriteria;
import com.project.appliances.dto.manufacturer.ManufacturerUpdateDto;
import com.project.appliances.service.interfaces.ManufacturerService;
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

@WebMvcTest(ManufacturerController.class)
@EnableMethodSecurity
@Import({UrlUtil.class})
class ManufacturerControllerTest {
    private static final String EMPLOYEE_EMAIL = "employee@store.com";
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ManufacturerService manufacturerService;

    // GET /manufacturer

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void findAllManufacturers_ShouldReturnViewAndModel() throws Exception {
        Page<ManufacturerDto> mockPage = new PageImpl<>(List.of());

        when(manufacturerService.findAll(any(ManufacturerSearchCriteria.class), any(Pageable.class)))
                .thenReturn(mockPage);

        mockMvc.perform(get("/manufacturer")
                                .param("page", "0")
                                .param("size", "10")
                                .param("sort", "id,asc")
                       )
                .andExpect(status().isOk())
                .andExpect(view().name("manufacturer/manufacturersPage"))
                .andExpect(model().attributeExists("manufacturersPage"))
                .andExpect(model().attributeExists("manufacturers"))
                .andExpect(model().attributeExists("criteria"));

        verify(manufacturerService).findAll(any(ManufacturerSearchCriteria.class), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "client@store.com", authorities = "ROLE_CLIENT")
    void findAllManufacturers_ShouldReturnForbidden_WhenUserIsClient() throws Exception {
        mockMvc.perform(get("/manufacturer"))
                .andExpect(status().isForbidden());
    }

    // GET AND POST /manufacturer/create
    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void showCreateForm_ShouldReturnViewAndModel() throws Exception {
        mockMvc.perform(get("/manufacturer/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("manufacturer/createManufacturerPage"))
                .andExpect(model().attributeExists("manufacturerCreateDto"))
                .andExpect(model().attribute("currentPage", "/manufacturer/create"));
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void createManufacturer_ShouldReturnViewWithErrors_WhenDtoIsInvalid() throws Exception {
        mockMvc.perform(post("/manufacturer/create")
                                .with(csrf())
                                .param("name", "")
                       )
                .andExpect(status().isOk())
                .andExpect(view().name("manufacturer/createManufacturerPage"))
                .andExpect(model().attributeHasErrors("manufacturerCreateDto"));

        verify(manufacturerService, never()).createManufacturer(any());
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void createManufacturer_ShouldReturnViewWithErrors_WhenNameExists() throws Exception {
        doThrow(new IllegalArgumentException("Name exists"))
                .when(manufacturerService).createManufacturer(any(ManufacturerCreateDto.class));

        mockMvc.perform(post("/manufacturer/create")
                                .with(csrf())
                                .param("name", "ExistingName")
                       )
                .andExpect(status().isOk())
                .andExpect(view().name("manufacturer/createManufacturerPage"))
                .andExpect(model().attributeHasFieldErrorCode("manufacturerCreateDto", "name",
                                                              "manufacturers.create.error"));
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void createManufacturer_ShouldRedirectWithSuccessFlash_WhenSuccess() throws Exception {
        mockMvc.perform(post("/manufacturer/create")
                                .with(csrf())
                                .param("name", "New Manufacturer")
                       )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manufacturer"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(flash().attribute("successMessage", "manufacturers.create.success"));

        verify(manufacturerService).createManufacturer(any(ManufacturerCreateDto.class));
    }

    // GET AND POST /manufacturer/details/{id}
    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void showManufacturerDetails_ShouldReturnViewAndModel() throws Exception {
        ManufacturerUpdateDto mockDto = new ManufacturerUpdateDto();
        when(manufacturerService.getManufacturerDetails(1L)).thenReturn(mockDto);

        mockMvc.perform(get("/manufacturer/details/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("manufacturer/manufacturerDetailsPage"))
                .andExpect(model().attributeExists("manufacturerUpdateDto"))
                .andExpect(model().attribute("manufacturerId", 1L))
                .andExpect(model().attribute("currentPage", "/manufacturer/details/1"));

        verify(manufacturerService).getManufacturerDetails(1L);
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void updateManufacturer_ShouldReturnViewWithErrors_WhenDtoIsInvalid() throws Exception {
        mockMvc.perform(post("/manufacturer/details/1")
                                .with(csrf())
                                .param("name", "")
                       )
                .andExpect(status().isOk())
                .andExpect(view().name("manufacturer/manufacturerDetailsPage"))
                .andExpect(model().attributeHasErrors("manufacturerUpdateDto"));

        verify(manufacturerService, never()).updateManufacturer(any(), anyLong());
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void updateManufacturer_ShouldReturnViewWithErrors_WhenNameExists() throws Exception {
        doThrow(new IllegalArgumentException("Name exists"))
                .when(manufacturerService).updateManufacturer(any(ManufacturerUpdateDto.class), eq(1L));

        mockMvc.perform(post("/manufacturer/details/1")
                                .with(csrf())
                                .param("name", "TakenName")
                       )
                .andExpect(status().isOk())
                .andExpect(view().name("manufacturer/manufacturerDetailsPage"))
                .andExpect(model().attributeHasFieldErrorCode("manufacturerUpdateDto", "name",
                                                              "manufacturers.update.error"));
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void updateManufacturer_ShouldRedirectWithSuccessFlash_WhenSuccess() throws Exception {
        mockMvc.perform(post("/manufacturer/details/1")
                                .with(csrf())
                                .param("name", "Updated Manufacturer")
                       )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manufacturer"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(manufacturerService).updateManufacturer(any(ManufacturerUpdateDto.class), eq(1L));
    }

    // POST /manufacturer/delete/{id}
    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void deleteManufacturer_ShouldRedirectWithSuccessFlash_WhenSuccess() throws Exception {
        mockMvc.perform(post("/manufacturer/delete/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manufacturer"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(manufacturerService).deleteManufacturer(1L);
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void deleteManufacturer_ShouldRedirectWithErrorFlash_WhenRelationExists() throws Exception {
        doThrow(new IllegalStateException("Cannot delete manufacturer"))
                .when(manufacturerService).deleteManufacturer(1L);

        mockMvc.perform(post("/manufacturer/delete/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manufacturer"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "manufacturers.delete.error"));

        verify(manufacturerService).deleteManufacturer(1L);
    }
}
