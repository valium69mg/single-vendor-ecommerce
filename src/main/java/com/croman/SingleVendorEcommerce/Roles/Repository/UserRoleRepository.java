package com.croman.SingleVendorEcommerce.Roles.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.SingleVendorEcommerce.Roles.RoleType;
import com.croman.SingleVendorEcommerce.Roles.UserRole;

public interface UserRoleRepository extends JpaRepository<UserRole, Long>{

	Optional<UserRole> findByRoleType(RoleType roleType);

}
