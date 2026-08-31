package com.croman.singlevendorecommerce.repository.cart;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.croman.singlevendorecommerce.entity.cart.CartItem;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

	Optional<CartItem> findByCartItemIdAndCart_CartId(Long cartItemId, Long cartId);

}
