package com.project.appliances.controller;

import com.project.appliances.dto.employee.EmployeeCreateDto;
import com.project.appliances.dto.employee.EmployeeDto;
import com.project.appliances.dto.employee.EmployeeSearchCriteria;
import com.project.appliances.dto.employee.EmployeeUpdateProfileDto;
import com.project.appliances.service.interfaces.EmployeeService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/employees")
@PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping
    public String findAllEmployees(@ModelAttribute("criteria") EmployeeSearchCriteria criteria,
                                   @PageableDefault(sort = "id", direction = Sort.Direction.ASC, size = 6) Pageable pageable,
                                   Model model) {
        Page<EmployeeDto> employeesPage = employeeService.findAll(criteria, pageable);

        model.addAttribute("employeesPage", employeesPage);
        model.addAttribute("employees", employeesPage.getContent());

        return "employee/employeesPage";
    }

    @GetMapping("/details/{id}")
    public String findEmployeeById(@PathVariable Long id,
                                   Model model) {
        model.addAttribute("employeeId", id);
        model.addAttribute("employeeUpdateProfileDto", employeeService.getEmployeeProfile(id));
        model.addAttribute("currentPage", "/employees/details/" + id);

        return "/employee/employeeDetailsPage";
    }

    @PostMapping("/details/{id}")
    public String updateEmployeeDetails(@PathVariable Long id,
                                        @Valid @ModelAttribute("employeeUpdateProfileDto") EmployeeUpdateProfileDto employeeUpdateProfileDto,
                                        BindingResult bindingResult, Model model) {
        model.addAttribute("employeeId", id);
        model.addAttribute("currentPage", "/employees/details/" + id);

        if (bindingResult.hasErrors()) {
            return "/employee/employeeDetailsPage";
        }

        try {
            employeeService.updateEmployeeProfile(id, employeeUpdateProfileDto);
        } catch (IllegalStateException e) {
            bindingResult.rejectValue("email", "validation.email.exist");
            return "/employee/employeeDetailsPage";
        }

        return "redirect:/employees";
    }

    @PostMapping("/details/{id}/generate-password")
    public String generatePassword(@PathVariable Long id,
                                   RedirectAttributes redirectAttributes) {

        String generatedPassword = employeeService.generatePassword(id);

        redirectAttributes.addFlashAttribute("successMessage", "employees.password.generate");
        redirectAttributes.addFlashAttribute("generatedPassword", generatedPassword);

        return "redirect:/employees/details/" + id;
    }

    @PostMapping("/delete/{id}")
    public String deleteEmployee(@PathVariable Long id,
                                 RedirectAttributes redirectAttributes,
                                 Authentication authentication,
                                 HttpServletRequest request,
                                 HttpServletResponse response) {
        try {
            boolean deleteSelf = employeeService.deleteEmployeeProfile(id, authentication.getName());

            if (deleteSelf) {
                new SecurityContextLogoutHandler().logout(request, response, authentication);
                return "redirect:/login";
            }
            redirectAttributes.addFlashAttribute("successMessage", "employees.delete.success");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "employees.delete.active.orders");
        }

        return "redirect:/employees";
    }

    @GetMapping("/create")
    public String createEmployee(Model model) {
        model.addAttribute("employeeCreateDto", new EmployeeCreateDto());
        model.addAttribute("currentPage", "/employees/create");

        return "employee/createEmployeePage";
    }

    @PostMapping("/create")
    public String createEmployee(@Valid @ModelAttribute("employeeCreateDto") EmployeeCreateDto employeeCreateDto,
                                 BindingResult bindingResult, Model model,
                                 RedirectAttributes redirectAttributes) {

        model.addAttribute("currentPage", "/employees/create");

        if (bindingResult.hasErrors()) {
            return "employee/createEmployeePage";
        }

        try {
            String generatedPassword = employeeService.createEmployee(employeeCreateDto);
            redirectAttributes.addFlashAttribute("successMessage", "employees.create.success");
            redirectAttributes.addFlashAttribute("generatedPassword", generatedPassword);
            return "redirect:/employees/create";
        } catch (IllegalStateException e) {
            bindingResult.rejectValue("email", "validation.email.exist");
            return "employee/createEmployeePage";
        }
    }
}
