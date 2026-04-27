package com.sparta.delivhub.domain.address.repository;

import com.sparta.delivhub.domain.address.entity.Address;
import com.sparta.delivhub.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID>, JpaSpecificationExecutor<Address> {

    List<Address> findByUserAndIsDefaultTrueAndDeletedAtIsNull(User user);
}
