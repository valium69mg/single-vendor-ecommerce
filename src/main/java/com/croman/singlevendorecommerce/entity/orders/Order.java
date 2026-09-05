package com.croman.singlevendorecommerce.entity.orders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.croman.singlevendorecommerce.entity.users.User;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "orders")
public class Order {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "order_id")
	private Long orderId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "order_number", nullable = false, unique = true, length = 50)
	private String orderNumber;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 20)
	private OrderStatus status;

	@Column(name = "shipping_recipient", nullable = false, length = 200)
	private String shippingRecipient;

	@Column(name = "shipping_line1", nullable = false, length = 255)
	private String shippingLine1;

	@Column(name = "shipping_line2", length = 255)
	private String shippingLine2;

	@Column(name = "shipping_city", nullable = false, length = 100)
	private String shippingCity;

	@Column(name = "shipping_state", nullable = false, length = 100)
	private String shippingState;

	@Column(name = "shipping_postal_code", nullable = false, length = 20)
	private String shippingPostalCode;

	@Column(name = "shipping_country", nullable = false, length = 100)
	private String shippingCountry;

	@Column(name = "shipping_phone", nullable = false, length = 30)
	private String shippingPhone;

	@Column(name = "subtotal", precision = 10, scale = 2, nullable = false)
	private BigDecimal subtotal;

	@Column(name = "shipping_cost", precision = 10, scale = 2, nullable = false)
	private BigDecimal shippingCost;

	@Column(name = "total", precision = 10, scale = 2, nullable = false)
	private BigDecimal total;

	@Builder.Default
	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrderItem> items = new ArrayList<>();

	@CreationTimestamp
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@UpdateTimestamp
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

}
