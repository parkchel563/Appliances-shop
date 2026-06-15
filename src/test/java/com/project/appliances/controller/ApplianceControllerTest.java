package com.project.appliances.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.project.appliances.dto.appliance.*;
import com.project.appliances.dto.manufacturer.ManufacturerDto;
import com.project.appliances.exception.ApplianceNotFoundException;
import com.project.appliances.model.Category;
import com.project.appliances.model.PowerType;
import com.project.appliances.service.interfaces.ApplianceService;
import com.project.appliances.service.interfaces.ManufacturerService;
import com.project.appliances.util.UrlUtil;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
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

@WebMvcTest(ApplianceController.class)
@EnableMethodSecurity
@Import(UrlUtil.class)
class ApplianceControllerTest {
    private static final String EMPLOYEE_EMAIL = "employee@store.com";
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ApplianceService applianceService;
    @MockBean
    private ManufacturerService manufacturerService;

    @BeforeEach
    void setup() {
        when(manufacturerService.findAll()).thenReturn(List.of(new ManufacturerDto()));
    }

    @Test
    @WithMockUser
    void showAllAppliances_ShouldReturnIndexPage() throws Exception {
        Page<ApplianceDto> mockPage = new PageImpl<>(List.of());

        when(applianceService.findAll(any(ApplianceSearchCriteria.class), any(Pageable.class)))
                .thenReturn(mockPage);

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("appliancesPage", "appliances", "criteria"))
                .andExpect(model().attributeExists("categories", "powerTypes", "manufacturers"));

        verify(applianceService).findAll(any(ApplianceSearchCriteria.class), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void showAppliancesList_ShouldReturnViewAndModel() throws Exception {
        Page<ApplianceDto> mockPage = new PageImpl<>(List.of());

        when(applianceService.findAll(any(ApplianceSearchCriteria.class), any(Pageable.class)))
                .thenReturn(mockPage);

        mockMvc.perform(get("/appliancesList"))
                .andExpect(status().isOk())
                .andExpect(view().name("appliances/appliancesPage"))
                .andExpect(model().attributeExists("appliancesPage", "appliances"));
    }

    @Test
    @WithMockUser(username = "client@store.com", authorities = "ROLE_CLIENT")
    void showAppliancesList_ShouldReturnForbidden_WhenUserIsClient() throws Exception {
        mockMvc.perform(get("/appliancesList"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void showCreateAppliances_ShouldReturnViewAndModel() throws Exception {
        mockMvc.perform(get("/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("appliances/createAppliancePage"))
                .andExpect(model().attributeExists("applianceCreateDto"))
                .andExpect(model().attribute("currentPage", "/create"));
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void createAppliance_ShouldReturnViewWithErrors_WhenDtoIsInvalid() throws Exception {
        mockMvc.perform(post("/create")
                                .with(csrf())
                                .param("name", "")
                       )
                .andExpect(status().isOk())
                .andExpect(view().name("appliances/createAppliancePage"))
                .andExpect(model().attributeHasErrors("applianceCreateDto"));

        verify(applianceService, never()).createAppliance(any());
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void createAppliance_ShouldRedirectWithSuccessFlash_WhenSuccess() throws Exception {
        mockMvc.perform(post("/create")
                                .with(csrf())
                                .param("name", "Tesla")
                                .param("category", Category.BIG.name())
                                .param("model", "Model X")
                                .param("manufacturerId", ("1"))
                                .param("powerType", PowerType.AC220.name())
                                .param("characteristic", "Best car today")
                                .param("description", "Best car right now")
                                .param("power", "100")
                                .param("price", "100.0")
                       )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/appliancesList"))
                .andExpect(flash().attribute("successMessage", "appliance.create.success"));

        verify(applianceService).createAppliance(any(ApplianceCreateDto.class));
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void showDetails_ShouldReturnViewAndModel() throws Exception {
        ApplianceUpdateDto mockDto = new ApplianceUpdateDto();
        when(applianceService.getApplianceDetails(1L)).thenReturn(mockDto);

        mockMvc.perform(get("/details/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("appliances/applianceDetailsPage"))
                .andExpect(model().attribute("applianceUpdateDto", mockDto))
                .andExpect(model().attribute("currentPage", "/details/1"));
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void updateAppliance_ShouldReturnViewWithErrors_WhenDtoIsInvalid() throws Exception {
        mockMvc.perform(post("/details/1")
                                .with(csrf())
                                .param("name", "a")
                                .param("category", Category.BIG.name())
                                .param("model", "Model X")
                                .param("manufacturerId", ("1"))
                                .param("powerType", PowerType.AC220.name())
                                .param("characteristic", "B")
                                .param("description", "B")
                                .param("power", "100")
                                .param("price", "100.0")
                       )
                .andExpect(status().isOk())
                .andExpect(view().name("appliances/applianceDetailsPage"))
                .andExpect(model().attributeHasErrors("applianceUpdateDto"));
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void updateAppliance_ShouldRedirect_WhenSuccess() throws Exception {
        mockMvc.perform(post("/details/1")
                                .with(csrf())
                                .param("name", "Tesla")
                                .param("category", Category.BIG.name())
                                .param("model", "Model X")
                                .param("manufacturerId", ("1"))
                                .param("powerType", PowerType.AC220.name())
                                .param("characteristic", "Best car today")
                                .param("description", "Best car right now")
                                .param("power", "100")
                                .param("price", "100.0")
                       )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/appliancesList"))
                .andExpect(flash().attribute("successMessage", "appliance.update.success"));

        verify(applianceService).updateAppliance(any(ApplianceUpdateDto.class), eq(1L));
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void updateAppliance_ShouldReturnViewWithErrorFlash_WhenNotFound() throws Exception {
        doThrow(new EntityNotFoundException("Not found"))
                .when(applianceService).updateAppliance(any(ApplianceUpdateDto.class), eq(1L));

        mockMvc.perform(post("/details/1")
                                .with(csrf())
                                .param("name", "Valid Name")
                                .param("category", Category.BIG.name())
                                .param("model", "Model X")
                                .param("manufacturerId", "1")
                                .param("powerType", PowerType.AC220.name())
                                .param("characteristic", "Valid characteristic")
                                .param("description", "Valid long description")
                                .param("power", "100")
                                .param("price", "100.0")
                       )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/appliancesList"))
                .andExpect(flash().attribute("errorMessage", "appliance.update.notFound"));
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void deleteAppliance_ShouldRedirectWithSuccessFlash() throws Exception {
        mockMvc.perform(post("/delete/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/appliancesList"))
                .andExpect(flash().attribute("successMessage", "appliance.delete.success"));

        verify(applianceService).deleteAppliance(1L);
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void deleteAppliance_ShouldRedirectWithErrorFlash_WhenStateIsIllegal() throws Exception {
        doThrow(new IllegalStateException("Appliance is in order"))
                .when(applianceService).deleteAppliance(1L);

        mockMvc.perform(post("/delete/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/appliancesList"))
                .andExpect(flash().attribute("errorMessage", "appliance.delete.denied"));
    }

    @Test
    @WithMockUser
    void showCustomerDetails_ShouldReturnCustomerDetailsPage_WhenApplianceExists() throws Exception {
        Long applianceId = 1L;

        ApplianceCustomerDetailsDto mockAppliance = new ApplianceCustomerDetailsDto();
        List<ApplianceDto> mockSimilar = List.of(new ApplianceDto());

        when(applianceService.getCustomerApplianceDetails(applianceId)).thenReturn(mockAppliance);
        when(applianceService.getSimilarAppliances(applianceId)).thenReturn(mockSimilar);

        mockMvc.perform(get("/appliance/{id}", applianceId))
                .andExpect(status().isOk())
                .andExpect(view().name("appliances/customerDetailsPage"))
                .andExpect(model().attribute("appliance", mockAppliance))
                .andExpect(model().attribute("similarAppliances", mockSimilar))
                .andExpect(model().attribute("currentPage", "/appliance/" + applianceId));

        verify(applianceService).getCustomerApplianceDetails(applianceId);
        verify(applianceService).getSimilarAppliances(applianceId);
    }

    @Test
    @WithMockUser
    void showCustomerDetails_ShouldReturnErrorOrRedirect_WhenApplianceNotFound() throws Exception {
        Long applianceId = 999L;

        when(applianceService.getCustomerApplianceDetails(applianceId))
                .thenThrow(new ApplianceNotFoundException(applianceId));

        mockMvc.perform(get("/appliance/{id}", applianceId))
                .andExpect(status().isNotFound());
    }

    @Test
    void showCustomerDetails_ShouldReturnUnauthorized_WhenUserIsAnonymous() throws Exception {
        mockMvc.perform(get("/appliance/1"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser
    void handleAccessDenied_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/access-denied"))
                .andExpect(status().isForbidden());
    }
}
