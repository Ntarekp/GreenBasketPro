package com.greenbasket.api.repository;

import com.greenbasket.api.model.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUserId(Long userId);
    
    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);
}
