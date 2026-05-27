package com.project.appliances.controller;

import com.project.appliances.dto.appliance.ApplianceDto;
import com.project.appliances.dto.appliance.ApplianceSearchCriteria;
import com.project.appliances.dto.manufacturer.ManufacturerDto;
import com.project.appliances.model.Category;
import com.project.appliances.model.PowerType;
import com.project.appliances.service.interfaces.ApplianceService;
import com.project.appliances.service.interfaces.ManufacturerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class ApplianceController {

    private final ApplianceService applianceService;
    private final ManufacturerService manufacturerService;

    @ModelAttribute("categories")
    public Category[] categories() {
        return Category.values();
    }

    @ModelAttribute("powerTypes")
    public PowerType[] powerTypes() {
        return PowerType.values();
    }

    @ModelAttribute("manufacturers")
    public List<ManufacturerDto> manufacturers() {
        return manufacturerService.findAll();
    }

    @GetMapping("/")
    public String showAllAppliances(@ModelAttribute("criteria") ApplianceSearchCriteria criteria,
                                    @PageableDefault(sort = "id", direction = Sort.Direction.ASC, size = 9) Pageable pageable,
                                    Model model) {

        Page<ApplianceDto> appliancesPage = applianceService.findAll(criteria, pageable);

        model.addAttribute("appliancesPage", appliancesPage);
        model.addAttribute("appliances", appliancesPage.getContent());
        return "index";
    }
}
