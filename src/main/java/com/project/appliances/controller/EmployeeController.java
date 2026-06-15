package com.project.appliances.controller;

import com.project.appliances.dto.employee.EmployeeDto;
import com.project.appliances.dto.employee.EmployeeSearchCriteria;
import com.project.appliances.service.interfaces.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

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
}
