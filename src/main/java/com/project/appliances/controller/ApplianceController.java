package com.project.appliances.controller;

import com.project.appliances.dto.appliance.ApplianceCreateDto;
import com.project.appliances.dto.appliance.ApplianceDto;
import com.project.appliances.dto.appliance.ApplianceSearchCriteria;
import com.project.appliances.dto.appliance.ApplianceUpdateDto;
import com.project.appliances.dto.manufacturer.ManufacturerDto;
import com.project.appliances.model.Category;
import com.project.appliances.model.PowerType;
import com.project.appliances.service.interfaces.ApplianceService;
import com.project.appliances.service.interfaces.ManufacturerService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping("/appliancesList")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
    public String showAppliancesList(@ModelAttribute("criteria") ApplianceSearchCriteria criteria,
                                     @PageableDefault(sort = "id", direction = Sort.Direction.ASC, size = 6) Pageable pageable,
                                     Model model) {

        Page<ApplianceDto> appliancesPage = applianceService.findAll(criteria, pageable);

        model.addAttribute("appliancesPage", appliancesPage);
        model.addAttribute("appliances", appliancesPage.getContent());

        return "appliances/appliancesPage";
    }

    @GetMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
    public String showCreateAppliances(Model model) {
        model.addAttribute("applianceCreateDto", new ApplianceCreateDto());
        model.addAttribute("currentPage", "/create");

        return "appliances/createAppliancePage";
    }

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
    public String createAppliance(@Valid @ModelAttribute("applianceCreateDto") ApplianceCreateDto applianceCreateDto,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        model.addAttribute("currentPage", "/create");

        if (bindingResult.hasErrors()) {
            model.addAttribute("manufacturers", manufacturerService.findAll());
            model.addAttribute("currentPage", "/create");
            return "appliances/createAppliancePage";
        }


        applianceService.createAppliance(applianceCreateDto);
        redirectAttributes.addFlashAttribute("successMessage", "appliance.create.success");
        return "redirect:/appliancesList";
    }

    @GetMapping("/details/{id}")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
    public String showDetails(@PathVariable long id, Model model) {
        model.addAttribute("applianceUpdateDto", applianceService.getApplianceDetails(id));
        model.addAttribute("currentPage", "/details/" + id);

        return "appliances/applianceDetailsPage";
    }

    @PostMapping("/details/{id}")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
    public String updateAppliance(@Valid @ModelAttribute("applianceUpdateDto") ApplianceUpdateDto applianceUpdateDto,
                                  BindingResult bindingResult,
                                  @PathVariable Long id,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("currentPage", "/details/" + id);
            return "appliances/applianceDetailsPage";
        }

        try {
            applianceService.updateAppliance(applianceUpdateDto, id);
            redirectAttributes.addFlashAttribute("successMessage", "appliance.update.success");
            return "redirect:/appliancesList";
        } catch (EntityNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "appliance.update.notFound");
            return "redirect:/appliancesList";
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
    public String deleteAppliance(@PathVariable Long id,
                                  RedirectAttributes redirectAttributes) {
        try {
            applianceService.deleteAppliance(id);
            redirectAttributes.addFlashAttribute("successMessage", "appliance.delete.success");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "appliance.delete.denied");
        }

        return "redirect:/appliancesList";
    }

    @GetMapping("/access-denied")
    public void handleAccessDenied() {
        throw new AccessDeniedException("Access is denied");
    }
}
