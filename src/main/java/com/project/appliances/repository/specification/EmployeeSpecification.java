package com.project.appliances.repository.specification;

import com.project.appliances.dto.employee.EmployeeSearchCriteria;
import com.project.appliances.model.Employee;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class EmployeeSpecification {
    private EmployeeSpecification() {
    }

    public static Specification<Employee> createSpecification(EmployeeSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getName() != null && !criteria.getName().isBlank()) {
                String search = "%" + criteria.getName().toLowerCase() + "%";

                Predicate namePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")), search
                );
                predicates.add(namePredicate);
            }

            if (criteria.getDepartment() != null && !criteria.getDepartment().isBlank()) {
                String search = "%" + criteria.getDepartment().toLowerCase() + "%";

                Predicate departmentPredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("department")), search
                );
                predicates.add(departmentPredicate);
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
