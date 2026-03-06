package com.croman.singlevendorecommerce.Roles.Repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.Roles.UserRole;
import com.croman.singlevendorecommerce.Roles.DTO.RoleType;

public interface UserRoleRepository extends JpaRepository<UserRole, Long>{

	Optional<UserRole> findByRoleType(RoleType roleType);

}
