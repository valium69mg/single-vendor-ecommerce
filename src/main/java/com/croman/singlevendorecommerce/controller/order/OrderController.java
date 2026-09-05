package com.croman.singlevendorecommerce.controller.order;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.croman.singlevendorecommerce.dto.DefaultApiResponse;
import com.croman.singlevendorecommerce.dto.orders.CreateOrderRequest;
import com.croman.singlevendorecommerce.dto.orders.OrderResponse;
import com.croman.singlevendorecommerce.dto.orders.OrderSummaryResponse;
import com.croman.singlevendorecommerce.service.order.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	@Operation(summary = "Create an order from the current user's cart", responses = {
			@ApiResponse(responseCode = "201", description = "Order created",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = OrderResponse.class))),
			@ApiResponse(responseCode = "400", description = "Empty cart or invalid request body",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = DefaultApiResponse.class))),
			@ApiResponse(responseCode = "401", description = "Authentication required",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = DefaultApiResponse.class))),
			@ApiResponse(responseCode = "409", description = "Stock insufficient or product unavailable",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = DefaultApiResponse.class)))
	})
	public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderRequest body) {
		return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(body));
	}

	@GetMapping
	@Operation(summary = "List the current user's orders, most recent first", responses = {
			@ApiResponse(responseCode = "200", description = "Orders returned",
					content = @Content(mediaType = "application/json",
							array = @ArraySchema(schema = @Schema(implementation = OrderSummaryResponse.class)))),
			@ApiResponse(responseCode = "401", description = "Authentication required",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = DefaultApiResponse.class)))
	})
	public ResponseEntity<List<OrderSummaryResponse>> getMyOrders() {
		return ResponseEntity.status(HttpStatus.OK).body(orderService.getMyOrders());
	}

	@GetMapping("/{orderNumber}")
	@Operation(summary = "Get one of the current user's orders by order number", responses = {
			@ApiResponse(responseCode = "200", description = "Order returned",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = OrderResponse.class))),
			@ApiResponse(responseCode = "401", description = "Authentication required",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = DefaultApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Order not found or not owned by the caller",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = DefaultApiResponse.class)))
	})
	public ResponseEntity<OrderResponse> getMyOrder(@PathVariable String orderNumber) {
		return ResponseEntity.status(HttpStatus.OK).body(orderService.getMyOrder(orderNumber));
	}
}
