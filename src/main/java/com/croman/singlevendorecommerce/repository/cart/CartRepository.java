package com.croman.singlevendorecommerce.repository.cart;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.croman.singlevendorecommerce.entity.cart.Cart;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

	Optional<Cart> findByUser_UserId(UUID userId);

}
