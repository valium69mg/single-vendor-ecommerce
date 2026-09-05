package com.croman.singlevendorecommerce.repository.orders;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.croman.singlevendorecommerce.entity.orders.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

	List<Order> findByUser_UserIdOrderByCreatedAtDesc(UUID userId);

	Optional<Order> findByOrderNumberAndUser_UserId(String orderNumber, UUID userId);

}
