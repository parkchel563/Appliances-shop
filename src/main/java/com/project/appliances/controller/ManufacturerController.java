package com.project.appliances.controller;

import com.project.appliances.dto.manufacturer.ManufacturerCreateDto;
import com.project.appliances.dto.manufacturer.ManufacturerDto;
import com.project.appliances.dto.manufacturer.ManufacturerSearchCriteria;
import com.project.appliances.dto.manufacturer.ManufacturerUpdateDto;
import com.project.appliances.service.interfaces.ManufacturerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/manufacturer")
@PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
public class ManufacturerController {

    private final ManufacturerService manufacturerService;

    @GetMapping
    public String findAllManufacturers(@ModelAttribute("criteria") ManufacturerSearchCriteria criteria,
                                       @PageableDefault(sort = "id", direction = Sort.Direction.ASC, size = 9) Pageable pageable,
                                       Model model) {
        Page<ManufacturerDto> manufacturersPage = manufacturerService.findAll(criteria, pageable);

        model.addAttribute("manufacturersPage", manufacturersPage);
        model.addAttribute("manufacturers", manufacturersPage.getContent());
        return "manufacturer/manufacturersPage";
    }

    @GetMapping("/create")
    public String createManufacturer(Model model) {
        model.addAttribute("manufacturerCreateDto", new ManufacturerCreateDto());
        model.addAttribute("currentPage", "/manufacturer/create");
        return "manufacturer/createManufacturerPage";
    }

    @PostMapping("/create")
    public String createManufacturer(@Valid @ModelAttribute("manufacturerCreateDto") ManufacturerCreateDto dto,
                                     BindingResult result, Model model,
                                     RedirectAttributes redirectAttributes) {
        model.addAttribute("currentPage", "/manufacturer/create");

        if (result.hasErrors()) {
            return "manufacturer/createManufacturerPage";
        }
        try {
            manufacturerService.createManufacturer(dto);
            redirectAttributes.addFlashAttribute("successMessage", "manufacturers.create.success");
            return "redirect:/manufacturer";
        } catch (IllegalArgumentException e) {
            result.rejectValue("name", "manufacturers.create.error");
            return "manufacturer/createManufacturerPage";
        }
    }

    @GetMapping("/details/{id}")
    public String manufacturerDetails(@PathVariable Long id, Model model) {
        model.addAttribute("manufacturerId", id);
        model.addAttribute("manufacturerUpdateDto", manufacturerService.getManufacturerDetails(id));
        model.addAttribute("currentPage", "/manufacturer/details/" + id);

        return "manufacturer/manufacturerDetailsPage";
    }

    @PostMapping("/details/{id}")
    public String manufacturerDetails(@Valid @ModelAttribute("manufacturerUpdateDto") ManufacturerUpdateDto manufacturerUpdateDto,
                                      BindingResult result,
                                      @PathVariable Long id,
                                      Model model,
                                      RedirectAttributes redirectAttributes) {

        model.addAttribute("manufacturerId", id);
        model.addAttribute("currentPage", "/manufacturer/details/" + id);

        if (result.hasErrors()) {
            return "manufacturer/manufacturerDetailsPage";
        }

        try {
            manufacturerService.updateManufacturer(manufacturerUpdateDto, id);
            redirectAttributes.addFlashAttribute("successMessage", "manufacturers.update.success");
            return "redirect:/manufacturer";
        } catch (IllegalArgumentException e) {
            result.rejectValue("name", "manufacturers.update.error");
            return "manufacturer/manufacturerDetailsPage";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteManufacturer(@PathVariable Long id,
                                     RedirectAttributes redirectAttributes) {
        try {
            manufacturerService.deleteManufacturer(id);
            redirectAttributes.addFlashAttribute("successMessage", "manufacturers.delete.success");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "manufacturers.delete.error");
        }

        return "redirect:/manufacturer";
    }

}
