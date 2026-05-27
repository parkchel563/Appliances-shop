package com.project.appliances.repository.specification;

import com.project.appliances.dto.appliance.ApplianceSearchCriteria;
import com.project.appliances.model.Appliance;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ApplianceSpecification {

    private ApplianceSpecification() {
    }

    public static Specification<Appliance> createFilterSpecification(ApplianceSearchCriteria criteria) {
        return (root, criteriaQuery, criteriaBuilder) -> {

            normalizeCriteria(criteria);

            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getName() != null && !criteria.getName().isBlank()) {
                String search = "%" + criteria.getName().toLowerCase() + "%";

                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), search
                );

                Predicate modelPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("model")), search
                );

                predicates.add(criteriaBuilder.or(namePredicate, modelPredicate));
            }

            if (criteria.getCategory() != null) {
                predicates.add(criteriaBuilder.equal(root.get("category"), criteria.getCategory()));
            }

            if (criteria.getManufacturerId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("manufacturer").get("id"), criteria.getManufacturerId()));
            }

            if (criteria.getPowerType() != null) {
                predicates.add(criteriaBuilder.equal(root.get("powerType"), criteria.getPowerType()));
            }

            if (criteria.getMinPower() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("power"), criteria.getMinPower()));
            }

            if (criteria.getMaxPower() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("power"), criteria.getMaxPower()));
            }

            if (criteria.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), criteria.getMinPrice()));
            }

            if (criteria.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), criteria.getMaxPrice()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void normalizeCriteria(ApplianceSearchCriteria criteria) {
        if (criteria == null) {
            return;
        }

        if (criteria.getMinPower() != null && criteria.getMinPower() < 0) {
            criteria.setMinPower(0);
        }

        if (criteria.getMaxPower() != null && criteria.getMaxPower() < 0) {
            criteria.setMaxPower(0);
        }

        if (criteria.getMinPrice() != null && criteria.getMinPrice().compareTo(BigDecimal.ZERO) < 0) {
            criteria.setMinPrice(BigDecimal.ZERO);
        }

        if (criteria.getMaxPrice() != null && criteria.getMaxPrice().compareTo(BigDecimal.ZERO) < 0) {
            criteria.setMaxPrice(BigDecimal.ZERO);
        }

        if (criteria.getMinPower() != null && criteria.getMaxPower() != null
                && criteria.getMinPower().compareTo(criteria.getMaxPower()) > 0) {
            Integer temp = criteria.getMinPower();
            criteria.setMinPower(criteria.getMaxPower());
            criteria.setMaxPower(temp);
        }

        if (criteria.getMinPrice() != null && criteria.getMaxPrice() != null
                && criteria.getMinPrice().compareTo(criteria.getMaxPrice()) > 0) {
            BigDecimal temp = criteria.getMinPrice();
            criteria.setMinPrice(criteria.getMaxPrice());
            criteria.setMaxPrice(temp);
        }
    }
}
