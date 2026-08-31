package com.croman.singlevendorecommerce.controller.cart;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.croman.singlevendorecommerce.dto.DefaultApiResponse;
import com.croman.singlevendorecommerce.dto.cart.AddCartItemDTO;
import com.croman.singlevendorecommerce.dto.cart.CartDTO;
import com.croman.singlevendorecommerce.dto.cart.UpdateCartItemDTO;
import com.croman.singlevendorecommerce.service.cart.CartService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/cart")
public class CartController {

	private final CartService cartService;

	@GetMapping
	@Operation(summary = "Get the current user's cart with live line totals and subtotal", responses = {
			@ApiResponse(responseCode = "200", description = "Cart returned",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = CartDTO.class))),
			@ApiResponse(responseCode = "401", description = "Authentication required",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = DefaultApiResponse.class)))
	})
	public ResponseEntity<CartDTO> getCart() {
		return ResponseEntity.status(HttpStatus.OK).body(cartService.getCart());
	}

	@PostMapping("/items")
	@Operation(summary = "Add a variant to the cart or increment its quantity", responses = {
			@ApiResponse(responseCode = "200", description = "Updated cart returned",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = CartDTO.class))),
			@ApiResponse(responseCode = "400", description = "Invalid quantity or requested quantity exceeds stock",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = DefaultApiResponse.class))),
			@ApiResponse(responseCode = "401", description = "Authentication required",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = DefaultApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Product variant not found",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = DefaultApiResponse.class))),
			@ApiResponse(responseCode = "409", description = "Product is inactive or soft-deleted",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = DefaultApiResponse.class)))
	})
	public ResponseEntity<CartDTO> addItem(@Valid @RequestBody AddCartItemDTO body) {
		return ResponseEntity.status(HttpStatus.OK).body(cartService.addItem(body));
	}

	@PatchMapping("/items/{cartItemId}")
	@Operation(summary = "Set the quantity of a cart line", responses = {
			@ApiResponse(responseCode = "200", description = "Updated cart returned",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = CartDTO.class))),
			@ApiResponse(responseCode = "400", description = "Invalid quantity or requested quantity exceeds stock",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = DefaultApiResponse.class))),
			@ApiResponse(responseCode = "401", description = "Authentication required",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = DefaultApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Cart item not found or not owned by the caller",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = DefaultApiResponse.class)))
	})
	public ResponseEntity<CartDTO> updateItem(@PathVariable Long cartItemId,
			@Valid @RequestBody UpdateCartItemDTO body) {
		return ResponseEntity.status(HttpStatus.OK).body(cartService.updateItem(cartItemId, body));
	}

	@DeleteMapping("/items/{cartItemId}")
	@Operation(summary = "Remove a line from the cart and return the updated cart", responses = {
			@ApiResponse(responseCode = "200", description = "Updated cart returned",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = CartDTO.class))),
			@ApiResponse(responseCode = "401", description = "Authentication required",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = DefaultApiResponse.class))),
			@ApiResponse(responseCode = "404", description = "Cart item not found or not owned by the caller",
					content = @Content(mediaType = "application/json",
							schema = @Schema(implementation = DefaultApiResponse.class)))
	})
	public ResponseEntity<CartDTO> removeItem(@PathVariable Long cartItemId) {
		return ResponseEntity.status(HttpStatus.OK).body(cartService.removeItem(cartItemId));
	}
}
