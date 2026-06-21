package com.project.appliances.repository.specification;

import com.project.appliances.dto.orders.OrdersSearchCriteria;
import com.project.appliances.model.OrderStatus;
import com.project.appliances.model.Orders;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class OrderSpecification {
    private OrderSpecification() {
    }

    public static Specification<Orders> createSpecification(OrdersSearchCriteria criteria) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getId() != null && !criteria.getId().isBlank()) {
                try {
                    Long orderId = Long.parseLong(criteria.getId().trim());
                    predicates.add(criteriaBuilder.equal(root.get("id"), orderId));
                } catch (NumberFormatException e) {
                    return criteriaBuilder.disjunction();
                }
            }

            if (criteria.getClientName() != null && !criteria.getClientName().isBlank()) {
                String search = "%" + criteria.getClientName().toLowerCase() + "%";

                Predicate clientNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("client").get("name")), search
                );

                predicates.add(clientNamePredicate);
            }

            if (criteria.getEmployeeName() != null && !criteria.getEmployeeName().isBlank()) {
                String search = "%" + criteria.getEmployeeName().toLowerCase() + "%";

                Predicate employeeNamePredicate = criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("employee").get("name")), search
                );

                predicates.add(employeeNamePredicate);
            }

            if (criteria.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), criteria.getStatus()));
            } else {
                predicates.add(criteriaBuilder.notEqual(root.get("status"), OrderStatus.NEW));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
