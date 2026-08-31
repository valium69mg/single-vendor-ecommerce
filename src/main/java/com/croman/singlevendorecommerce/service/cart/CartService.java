package com.croman.singlevendorecommerce.service.cart;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.croman.singlevendorecommerce.dto.cart.AddCartItemDTO;
import com.croman.singlevendorecommerce.dto.cart.CartDTO;
import com.croman.singlevendorecommerce.dto.cart.CartItemDTO;
import com.croman.singlevendorecommerce.dto.cart.UpdateCartItemDTO;
import com.croman.singlevendorecommerce.dto.products.ProductStatus;
import com.croman.singlevendorecommerce.entity.cart.Cart;
import com.croman.singlevendorecommerce.entity.cart.CartItem;
import com.croman.singlevendorecommerce.entity.products.Product;
import com.croman.singlevendorecommerce.entity.products.ProductVariant;
import com.croman.singlevendorecommerce.entity.users.User;
import com.croman.singlevendorecommerce.repository.cart.CartItemRepository;
import com.croman.singlevendorecommerce.repository.cart.CartRepository;
import com.croman.singlevendorecommerce.repository.products.ProductVariantRepository;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.service.users.CurrentUserService;
import com.croman.singlevendorecommerce.utils.LocaleUtils;
import com.croman.singlevendorecommerce.utils.exceptions.ApiServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

	private static final String VARIANT_NOT_FOUND = "cart_variant_not_found";
	private static final String ITEM_NOT_FOUND = "cart_item_not_found";
	private static final String PRODUCT_UNAVAILABLE = "cart_product_unavailable";
	private static final String STOCK_EXCEEDED = "cart_stock_exceeded";
	private static final String QUANTITY_INVALID = "cart_quantity_invalid";

	private final CartRepository cartRepository;
	private final CartItemRepository cartItemRepository;
	private final ProductVariantRepository productVariantRepository;
	private final CurrentUserService currentUserService;
	private final MessageService messageService;

	@Transactional(readOnly = true)
	public CartDTO getCart() {
		User user = currentUserService.getCurrentUser();
		return cartRepository.findByUser_UserId(user.getUserId())
				.map(this::toDTO)
				.orElseGet(CartService::emptyCart);
	}

	public CartDTO addItem(AddCartItemDTO dto) {
		User user = currentUserService.getCurrentUser();
		Cart cart = cartRepository.findByUser_UserId(user.getUserId())
				.orElseGet(() -> cartRepository.save(Cart.builder().user(user).items(new ArrayList<>()).build()));

		ProductVariant variant = productVariantRepository.findByIdWithProduct(dto.getProductVariantId())
				.orElseThrow(() -> notFound(VARIANT_NOT_FOUND));

		guardProductAvailable(variant);

		CartItem existing = findLine(cart, dto.getProductVariantId());
		int newQuantity = existing != null ? existing.getQuantity() + dto.getQuantity() : dto.getQuantity();

		guardStock(variant, newQuantity);

		if (existing != null) {
			existing.setQuantity(newQuantity);
			cartItemRepository.save(existing);
		} else {
			CartItem item = CartItem.builder().cart(cart).productVariant(variant).quantity(newQuantity).build();
			cart.getItems().add(item);
			cartItemRepository.save(item);
		}

		return toDTO(cart);
	}

	public CartDTO updateItem(Long cartItemId, UpdateCartItemDTO dto) {
		Cart cart = resolveCart();
		CartItem item = cartItemRepository.findByCartItemIdAndCart_CartId(cartItemId, cart.getCartId())
				.orElseThrow(() -> notFound(ITEM_NOT_FOUND));

		if (dto.getQuantity() == null || dto.getQuantity() < 1) {
			throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
					messageService.getMessage(QUANTITY_INVALID, LocaleUtils.getDefaultLocale()));
		}

		guardStock(item.getProductVariant(), dto.getQuantity());

		item.setQuantity(dto.getQuantity());
		cartItemRepository.save(item);

		return toDTO(cart);
	}

	public CartDTO removeItem(Long cartItemId) {
		Cart cart = resolveCart();
		CartItem item = cartItemRepository.findByCartItemIdAndCart_CartId(cartItemId, cart.getCartId())
				.orElseThrow(() -> notFound(ITEM_NOT_FOUND));

		cart.getItems().remove(item);
		cartItemRepository.delete(item);

		return toDTO(cart);
	}

	// ─── helpers ─────────────────────────────────────────────────────────────

	private Cart resolveCart() {
		User user = currentUserService.getCurrentUser();
		return cartRepository.findByUser_UserId(user.getUserId())
				.orElseThrow(() -> notFound(ITEM_NOT_FOUND));
	}

	private CartItem findLine(Cart cart, Long productVariantId) {
		return cart.getItems().stream()
				.filter(line -> line.getProductVariant() != null
						&& productVariantId.equals(line.getProductVariant().getProductVariantId()))
				.findFirst()
				.orElse(null);
	}

	private void guardProductAvailable(ProductVariant variant) {
		Product product = variant.getProduct();
		if (product == null || product.getDeletedAt() != null || product.getStatus() != ProductStatus.ACTIVE) {
			throw new ApiServiceException(HttpStatus.CONFLICT.value(),
					messageService.getMessage(PRODUCT_UNAVAILABLE, LocaleUtils.getDefaultLocale()));
		}
	}

	private void guardStock(ProductVariant variant, int requestedQuantity) {
		Integer stock = variant.getStock();
		if (stock == null || requestedQuantity > stock) {
			throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
					messageService.getMessage(STOCK_EXCEEDED, LocaleUtils.getDefaultLocale()),
					Map.of("availableStock", stock));
		}
	}

	private ApiServiceException notFound(String messageKey) {
		return new ApiServiceException(HttpStatus.NOT_FOUND.value(),
				messageService.getMessage(messageKey, LocaleUtils.getDefaultLocale()));
	}

	private CartDTO toDTO(Cart cart) {
		List<CartItemDTO> items = cart.getItems().stream().map(CartService::toItemDTO).toList();
		BigDecimal subtotal = items.stream()
				.map(CartItemDTO::getLineTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		int totalItems = items.stream().mapToInt(CartItemDTO::getQuantity).sum();

		return CartDTO.builder()
				.cartId(cart.getCartId())
				.items(items)
				.subtotal(subtotal)
				.totalItems(totalItems)
				.build();
	}

	private static CartItemDTO toItemDTO(CartItem item) {
		ProductVariant variant = item.getProductVariant();
		Product product = variant.getProduct();
		BigDecimal unitPrice = variant.getDiscountPrice() != null ? variant.getDiscountPrice() : variant.getPrice();

		return CartItemDTO.builder()
				.cartItemId(item.getCartItemId())
				.productVariantId(variant.getProductVariantId())
				.productId(product != null ? product.getProductId() : null)
				.productName(product != null ? product.getName() : null)
				.sku(variant.getSku())
				.imageUrl(product != null ? product.getFileUrl() : null)
				.unitPrice(unitPrice)
				.discountPrice(variant.getDiscountPrice())
				.quantity(item.getQuantity())
				.availableStock(variant.getStock())
				.lineTotal(unitPrice.multiply(BigDecimal.valueOf(item.getQuantity())))
				.build();
	}

	private static CartDTO emptyCart() {
		return CartDTO.builder()
				.cartId(null)
				.items(new ArrayList<>())
				.subtotal(BigDecimal.ZERO)
				.totalItems(0)
				.build();
	}
}
