package com.croman.singlevendorecommerce.repository.roles;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.dto.roles.RoleType;
import com.croman.singlevendorecommerce.entity.roles.UserRole;

public interface UserRoleRepository extends JpaRepository<UserRole, Long>{

	Optional<UserRole> findByRoleType(RoleType roleType);

}
