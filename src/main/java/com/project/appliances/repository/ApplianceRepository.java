package com.project.appliances.repository;

import com.project.appliances.model.Appliance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplianceRepository extends JpaRepository<Appliance, Long>, JpaSpecificationExecutor<Appliance> {
    boolean existsAppliancesByManufacturerId(Long id);

    Page<Appliance> findByNameContainingIgnoreCaseAndManufacturerIdAndIdNot(
            String name, Long manufacturerId, Long id, Pageable pageable);

    Page<Appliance> findByNameContainingIgnoreCaseAndManufacturerIdNotAndIdNot(
            String name, Long manufacturerId, Long id, Pageable pageable);
}
