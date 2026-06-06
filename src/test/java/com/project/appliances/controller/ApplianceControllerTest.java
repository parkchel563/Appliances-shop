package com.project.appliances.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.project.appliances.dto.appliance.ApplianceDto;
import com.project.appliances.dto.appliance.ApplianceSearchCriteria;
import com.project.appliances.dto.manufacturer.ManufacturerDto;
import com.project.appliances.service.interfaces.ApplianceService;
import com.project.appliances.service.interfaces.ManufacturerService;
import com.project.appliances.util.UrlUtil;
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

    // GET /
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

}
