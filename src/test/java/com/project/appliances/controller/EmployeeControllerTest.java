package com.project.appliances.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.project.appliances.dto.employee.EmployeeCreateDto;
import com.project.appliances.dto.employee.EmployeeDto;
import com.project.appliances.dto.employee.EmployeeSearchCriteria;
import com.project.appliances.dto.employee.EmployeeUpdateProfileDto;
import com.project.appliances.service.interfaces.EmployeeService;
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

@WebMvcTest(EmployeeController.class)
@EnableMethodSecurity
@Import({UrlUtil.class})
class EmployeeControllerTest {
    private static final String EMPLOYEE_EMAIL = "employee@store.com";
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private EmployeeService employeeService;

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void findAllEmployees_ShouldReturnViewAndModel() throws Exception {
        Page<EmployeeDto> mockPage = new PageImpl<>(List.of());

        when(employeeService.findAll(any(EmployeeSearchCriteria.class), any(Pageable.class)))
                .thenReturn(mockPage);

        mockMvc.perform(get("/employees")
                                .param("page", "0")
                                .param("size", "10")
                                .param("sort", "id,asc")
                       )
                .andExpect(status().isOk())
                .andExpect(view().name("employee/employeesPage"))
                .andExpect(model().attributeExists("employeesPage"))
                .andExpect(model().attributeExists("employees"))
                .andExpect(model().attributeExists("criteria")
                          );

        verify(employeeService).findAll(any(EmployeeSearchCriteria.class), any(Pageable.class));
    }

    @Test
    @WithMockUser(username = "client@store.com", authorities = "ROLE_CLIENT")
    void findAllEmployees_ShouldReturnForbidden_WhenUserIsClient() throws Exception {
        mockMvc.perform(get("/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void findEmployeeById_ShouldReturnViewAndModel() throws Exception {
        EmployeeUpdateProfileDto mockDto = new EmployeeUpdateProfileDto();
        when(employeeService.getEmployeeProfile(1L)).thenReturn(mockDto);

        mockMvc.perform(get("/employees/details/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("/employee/employeeDetailsPage"))
                .andExpect(model().attribute("employeeId", 1L))
                .andExpect(model().attribute("employeeUpdateProfileDto", mockDto))
                .andExpect(model().attribute("currentPage", "/employees/details/1"));
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void updateEmployeeDetails_ShouldReturnViewWithErrors_WhenDtoIsInvalid() throws Exception {
        mockMvc.perform(post("/employees/details/1")
                                .with(csrf())
                                .param("email", "invalid-email")
                       )
                .andExpect(status().isOk())
                .andExpect(view().name("/employee/employeeDetailsPage"))
                .andExpect(model().attributeHasErrors("employeeUpdateProfileDto"));

        verify(employeeService, never()).updateEmployeeProfile(anyLong(), any());
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void updateEmployeeDetails_ShouldReturnViewWithErrors_WhenEmailExists() throws Exception {
        doThrow(new IllegalStateException("Email exists"))
                .when(employeeService).updateEmployeeProfile(eq(1L), any(EmployeeUpdateProfileDto.class));

        mockMvc.perform(post("/employees/details/1")
                                .with(csrf())
                                .param("email", "taken@store.com")
                                .param("name", "John Doe")
                                .param("department", "sales")
                       )
                .andExpect(status().isOk())
                .andExpect(view().name("/employee/employeeDetailsPage"))
                .andExpect(model().attributeHasFieldErrorCode("employeeUpdateProfileDto", "email",
                                                              "validation.email.exist"));
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void updateEmployeeDetails_ShouldRedirect_WhenSuccess() throws Exception {
        mockMvc.perform(post("/employees/details/1")
                                .with(csrf())
                                .param("email", "new@store.com")
                                .param("name", "John Doe")
                                .param("department", "sales")
                       )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"));

        verify(employeeService).updateEmployeeProfile(eq(1L), any(EmployeeUpdateProfileDto.class));
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void generatePassword_ShouldRedirectWithFlashAttributes() throws Exception {
        when(employeeService.generatePassword(1L)).thenReturn("NewPass123!");

        mockMvc.perform(post("/employees/details/1/generate-password").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees/details/1"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(flash().attribute("generatedPassword", "NewPass123!"));

        verify(employeeService).generatePassword(1L);
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void deleteEmployee_ShouldRedirectToEmployees_WhenDeletingOtherUser() throws Exception {
        when(employeeService.deleteEmployeeProfile(2L, EMPLOYEE_EMAIL)).thenReturn(false);

        mockMvc.perform(post("/employees/delete/2").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void deleteEmployee_ShouldLogoutAndRedirectToLogin_WhenDeletingSelf() throws Exception {
        when(employeeService.deleteEmployeeProfile(1L, EMPLOYEE_EMAIL)).thenReturn(true);

        mockMvc.perform(post("/employees/delete/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void deleteEmployee_ShouldRedirectWithErrorFlash_WhenEmployeeHasActiveOrders() throws Exception {
        when(employeeService.deleteEmployeeProfile(2L, EMPLOYEE_EMAIL))
                .thenThrow(new IllegalStateException("Active orders"));

        mockMvc.perform(post("/employees/delete/2").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "employees.delete.active.orders"));
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void createEmployee_ShouldReturnViewAndModel() throws Exception {
        mockMvc.perform(get("/employees/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("employee/createEmployeePage"))
                .andExpect(model().attributeExists("employeeCreateDto"))
                .andExpect(model().attribute("currentPage", "/employees/create"));
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void createEmployee_ShouldReturnViewWithErrors_WhenDtoIsInvalid() throws Exception {
        mockMvc.perform(post("/employees/create")
                                .with(csrf())
                                .param("email", "invalid")
                       )
                .andExpect(status().isOk())
                .andExpect(view().name("employee/createEmployeePage"))
                .andExpect(model().attributeHasErrors("employeeCreateDto"));

        verify(employeeService, never()).createEmployee(any());
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void createEmployee_ShouldReturnViewWithErrors_WhenEmailExists() throws Exception {
        when(employeeService.createEmployee(any(EmployeeCreateDto.class)))
                .thenThrow(new IllegalStateException("Email exists"));

        mockMvc.perform(post("/employees/create")
                                .with(csrf())
                                .param("email", "taken@store.com")
                                .param("name", "John")
                                .param("department", "sales")
                       )
                .andExpect(status().isOk())
                .andExpect(view().name("employee/createEmployeePage"))
                .andExpect(model().attributeHasFieldErrorCode("employeeCreateDto", "email", "validation.email.exist"));
    }

    @Test
    @WithMockUser(username = EMPLOYEE_EMAIL, authorities = "ROLE_EMPLOYEE")
    void createEmployee_ShouldRedirectWithFlashAttributes_WhenSuccess() throws Exception {
        when(employeeService.createEmployee(any(EmployeeCreateDto.class))).thenReturn("GenPass777!");

        mockMvc.perform(post("/employees/create")
                                .with(csrf())
                                .param("email", "new@store.com")
                                .param("name", "John")
                                .param("department", "sales")
                       )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/employees/create"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(flash().attribute("generatedPassword", "GenPass777!"));
    }
}
