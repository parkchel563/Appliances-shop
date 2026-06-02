package com.project.appliances.repository.specification;

import com.project.appliances.dto.manufacturer.ManufacturerSearchCriteria;
import com.project.appliances.model.Manufacturer;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ManufacturerSpecification {
    private ManufacturerSpecification() {
    }

    public static Specification<Manufacturer> createFilterSpecification(ManufacturerSearchCriteria criteria) {
        return (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getName() != null && !criteria.getName().isEmpty()) {
                String search = "%" + criteria.getName().toLowerCase() + "%";

                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), search
                );

                predicates.add(namePredicate);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
