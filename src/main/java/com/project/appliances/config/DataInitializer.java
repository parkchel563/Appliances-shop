package com.project.appliances.config;

import com.project.appliances.model.Client;
import com.project.appliances.model.Employee;
import com.project.appliances.repository.ClientRepository;
import com.project.appliances.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("ACTIVE: Starting encrypting passwords");

        encryptClientPasswords();
        encryptEmployeePasswords();

        log.info("ACTIVE: Finishing encrypting passwords");
    }

    private void encryptClientPasswords() {
        Page<Client> clients = clientRepository.findAll(Pageable.ofSize(5));
        for (Client client : clients) {
            if (!client.getPassword().startsWith("$2a$")) {
                String rawPassword = client.getPassword();
                String encodedPassword = passwordEncoder.encode(rawPassword);
                client.setPassword(encodedPassword);
                clientRepository.save(client);
                log.info("Encoded newPassword for Client: {}", client.getEmail());
            }
        }
    }

    private void encryptEmployeePasswords() {
        Page<Employee> employees = employeeRepository.findAll(Pageable.ofSize(5));
        for (Employee employee : employees) {
            if (!employee.getPassword().startsWith("$2a$")) {
                String rawPassword = employee.getPassword();
                String encodedPassword = passwordEncoder.encode(rawPassword);
                employee.setPassword(encodedPassword);
                employeeRepository.save(employee);
                log.info("Encoded newPassword for Employee: {}", employee.getEmail());
            }
        }
    }
}
