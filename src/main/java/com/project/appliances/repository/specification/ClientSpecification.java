package com.project.appliances.repository.specification;

import com.project.appliances.dto.client.ClientSearchCriteria;
import com.project.appliances.model.Client;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ClientSpecification {
    private ClientSpecification() {
    }

    public static Specification<Client> createSpecification(ClientSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getName() != null && !criteria.getName().isBlank()) {
                String search = "%" + criteria.getName().toLowerCase() + "%";

                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), search
                );
                predicates.add(namePredicate);
            }

            if (criteria.getEmail() != null && !criteria.getEmail().isBlank()) {
                String search = "%" + criteria.getEmail().toLowerCase() + "%";

                Predicate emailPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")), search
                );
                predicates.add(emailPredicate);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
