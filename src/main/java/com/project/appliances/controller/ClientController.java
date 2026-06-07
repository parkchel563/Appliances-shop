package com.project.appliances.controller;

import com.project.appliances.dto.client.ClientCreateDto;
import com.project.appliances.dto.client.ClientDto;
import com.project.appliances.dto.client.ClientSearchCriteria;
import com.project.appliances.dto.client.ClientUpdateProfileDto;
import com.project.appliances.service.interfaces.ClientService;
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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@RequestMapping("/clients")
@PreAuthorize("hasAuthority('ROLE_EMPLOYEE')")
public class ClientController {

    private final ClientService clientService;

    @GetMapping
    public String getAllClients(@ModelAttribute("criteria") ClientSearchCriteria criteria,
                                @PageableDefault(sort = "id", direction = Sort.Direction.ASC, size = 6) Pageable pageable,
                                Model model) {

        Page<ClientDto> clientsPage = clientService.findAll(criteria, pageable);

        model.addAttribute("clientsPage", clientsPage);
        model.addAttribute("clients", clientsPage.getContent());

        return "client/clientsPage";
    }

    @GetMapping("/details/{id}")
    public String getClientDetails(@PathVariable Long id, Model model) {
        model.addAttribute("clientId", id);
        model.addAttribute("clientUpdateProfileDto", clientService.getClientProfile(id));
        model.addAttribute("currentPage", "/clients/details/" + id);
        return "client/clientDetailsPage";
    }

    @PostMapping("/details/{id}")
    public String updateClientDetails(@PathVariable Long id,
                                      @Valid @ModelAttribute("clientUpdateProfileDto") ClientUpdateProfileDto clientUpdateProfileDto,
                                      BindingResult bindingResult,
                                      Model model) {

        model.addAttribute("clientId", id);
        model.addAttribute("currentPage", "/clients/details/" + id);

        if (bindingResult.hasErrors()) {
            return "client/clientDetailsPage";
        }

        try {
            clientService.updateClientProfile(id, clientUpdateProfileDto);
        } catch (IllegalArgumentException e) {
            bindingResult.rejectValue("email", "validation.email.exist");
            return "client/clientDetailsPage";
        }

        return "redirect:/clients";
    }

    @PostMapping("/delete/{id}")
    public String deleteClient(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            clientService.deleteClientProfile(id);
            redirectAttributes.addFlashAttribute("successMessage", "client.delete.success");
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "client.delete.active.orders");
        }
        return "redirect:/clients";
    }

    @PostMapping("/details/{id}/generate-password")
    public String generatePassword(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        String generatedPassword = clientService.generatePassword(id);

        redirectAttributes.addFlashAttribute("successMessage", "client.password.generated");
        redirectAttributes.addFlashAttribute("generatedPassword", generatedPassword);

        return "redirect:/clients/details/" + id;
    }

    @GetMapping("/create")
    public String createClient(Model model) {
        model.addAttribute("clientCreateDto", new ClientCreateDto());
        model.addAttribute("currentPage", "/clients/create");

        return "client/createClientPage";
    }

    @PostMapping("/create")
    public String createClient(@Valid @ModelAttribute("clientCreateDto") ClientCreateDto clientCreateDto,
                               BindingResult bindingResult,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        model.addAttribute("currentPage", "/clients/create");

        if (bindingResult.hasErrors()) {
            return "client/createClientPage";
        }

        try {
            String generatedPassword = clientService.createClient(clientCreateDto);
            redirectAttributes.addFlashAttribute("successMessage", "client.create.success");
            redirectAttributes.addFlashAttribute("generatedPassword", generatedPassword);
            return "redirect:/clients/create";
        } catch (IllegalStateException e) {
            bindingResult.rejectValue("email", "validation.email.exist");
            return "client/createClientPage";
        }
    }

    @GetMapping("/access-denied")
    public void handleAccessDenied() {
        throw new AccessDeniedException("Access is denied");
    }
}
