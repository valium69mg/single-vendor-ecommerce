package com.croman.SingleVendorEcommerce.Roles.Repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.SingleVendorEcommerce.Roles.UserRole;

public interface RoleTypeRepository extends JpaRepository<UserRole, Long>{

}
