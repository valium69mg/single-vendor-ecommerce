package com.croman.singlevendorecommerce.service.cart;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.croman.singlevendorecommerce.dto.cart.AddCartItemDTO;
import com.croman.singlevendorecommerce.dto.cart.CartDTO;
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
import com.croman.singlevendorecommerce.utils.exceptions.ApiServiceException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

	@Mock
	private CartRepository cartRepository;

	@Mock
	private CartItemRepository cartItemRepository;

	@Mock
	private ProductVariantRepository productVariantRepository;

	@Mock
	private CurrentUserService currentUserService;

	@Mock
	private MessageService messageService;

	@InjectMocks
	private CartService cartService;

	// ─── Fixtures ────────────────────────────────────────────────────────────

	private static final UUID USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
	private static final Long CART_ID = 1L;
	private static final Long VARIANT_ID = 100L;
	private static final Long CART_ITEM_ID = 500L;

	private User user;
	private Product product;
	private ProductVariant variant;
	private Cart cart;

	@BeforeEach
	void setUp() {
		user = User.builder().userId(USER_ID).email("shopper@example.com").username("shopper").build();

		product = Product.builder()
				.productId(UUID.fromString("33333333-3333-3333-3333-333333333333"))
				.name("Gold Ring")
				.status(ProductStatus.ACTIVE)
				.fileUrl("products/gold-ring.jpg")
				.build();

		variant = new ProductVariant();
		variant.setProductVariantId(VARIANT_ID);
		variant.setProduct(product);
		variant.setSku("RING-18K-7");
		variant.setPrice(new BigDecimal("100.00"));
		variant.setStock(10);

		cart = Cart.builder().cartId(CART_ID).user(user).items(new ArrayList<>()).build();
	}

	private CartItem lineFor(ProductVariant v, int quantity) {
		return CartItem.builder().cartItemId(CART_ITEM_ID).cart(cart).productVariant(v).quantity(quantity).build();
	}

	private void stubMessage(String key, String value) {
		when(messageService.getMessage(eq(key), any(Locale.class))).thenReturn(value);
	}

	// ─── getCart ─────────────────────────────────────────────────────────────

	@Test
	void testGetCartReturnsEmptyCartWhenUserHasNoCart() {
		when(currentUserService.getCurrentUser()).thenReturn(user);
		when(cartRepository.findByUser_UserId(USER_ID)).thenReturn(Optional.empty());

		CartDTO result = cartService.getCart();

		assertThat(result.getCartId()).isNull();
		assertThat(result.getItems()).isEmpty();
		assertThat(result.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
		assertThat(result.getTotalItems()).isZero();
		verify(cartRepository, never()).save(any());
	}

	@Test
	void testGetCartMapsExistingLinesWithLiveDiscountPrice() {
		variant.setDiscountPrice(new BigDecimal("80.00"));
		cart.getItems().add(lineFor(variant, 2));
		when(currentUserService.getCurrentUser()).thenReturn(user);
		when(cartRepository.findByUser_UserId(USER_ID)).thenReturn(Optional.of(cart));

		CartDTO result = cartService.getCart();

		assertThat(result.getCartId()).isEqualTo(CART_ID);
		assertThat(result.getItems()).hasSize(1);
		assertThat(result.getItems().get(0).getUnitPrice()).isEqualByComparingTo("80.00");
		assertThat(result.getItems().get(0).getLineTotal()).isEqualByComparingTo("160.00");
		assertThat(result.getItems().get(0).getSku()).isEqualTo("RING-18K-7");
		assertThat(result.getItems().get(0).getProductName()).isEqualTo("Gold Ring");
		assertThat(result.getSubtotal()).isEqualByComparingTo("160.00");
		assertThat(result.getTotalItems()).isEqualTo(2);
		verify(productVariantRepository, never()).save(any());
	}

	// ─── addItem ─────────────────────────────────────────────────────────────

	@Test
	void testAddItemLazyCreatesCartAndStoresNewLine() {
		when(currentUserService.getCurrentUser()).thenReturn(user);
		when(cartRepository.findByUser_UserId(USER_ID)).thenReturn(Optional.empty());
		when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> {
			Cart c = inv.getArgument(0);
			c.setCartId(CART_ID);
			return c;
		});
		when(productVariantRepository.findByIdWithProduct(VARIANT_ID)).thenReturn(Optional.of(variant));

		CartDTO result = cartService.addItem(AddCartItemDTO.builder().productVariantId(VARIANT_ID).quantity(3).build());

		verify(cartRepository).save(any(Cart.class));
		ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
		verify(cartItemRepository).save(captor.capture());
		assertThat(captor.getValue().getQuantity()).isEqualTo(3);
		assertThat(result.getTotalItems()).isEqualTo(3);
		verify(productVariantRepository, never()).save(any());
	}

	@Test
	void testAddItemIncrementsExistingLineQuantity() {
		cart.getItems().add(lineFor(variant, 3));
		when(currentUserService.getCurrentUser()).thenReturn(user);
		when(cartRepository.findByUser_UserId(USER_ID)).thenReturn(Optional.of(cart));
		when(productVariantRepository.findByIdWithProduct(VARIANT_ID)).thenReturn(Optional.of(variant));

		cartService.addItem(AddCartItemDTO.builder().productVariantId(VARIANT_ID).quantity(2).build());

		ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
		verify(cartItemRepository).save(captor.capture());
		assertThat(captor.getValue().getQuantity()).isEqualTo(5);
		verify(cartRepository, never()).save(any());
	}

	@Test
	void testAddItemRejectsWhenResultingQuantityExceedsStock() {
		variant.setStock(4);
		cart.getItems().add(lineFor(variant, 3));
		when(currentUserService.getCurrentUser()).thenReturn(user);
		when(cartRepository.findByUser_UserId(USER_ID)).thenReturn(Optional.of(cart));
		when(productVariantRepository.findByIdWithProduct(VARIANT_ID)).thenReturn(Optional.of(variant));
		stubMessage("cart_stock_exceeded", "Not enough stock");

		assertThatThrownBy(() -> cartService
				.addItem(AddCartItemDTO.builder().productVariantId(VARIANT_ID).quantity(2).build()))
				.isInstanceOf(ApiServiceException.class)
				.hasMessageContaining("Not enough stock")
				.satisfies(ex -> {
					assertThat(((ApiServiceException) ex).getStatusCode()).isEqualTo(400);
					assertThat(((ApiServiceException) ex).getMetadata()).containsEntry("availableStock", 4);
				});

		verify(cartItemRepository, never()).save(any());
		verify(productVariantRepository, never()).save(any());
	}

	@Test
	void testAddItemRejectsWhenVariantNotFound() {
		when(currentUserService.getCurrentUser()).thenReturn(user);
		when(cartRepository.findByUser_UserId(USER_ID)).thenReturn(Optional.of(cart));
		when(productVariantRepository.findByIdWithProduct(VARIANT_ID)).thenReturn(Optional.empty());
		stubMessage("cart_variant_not_found", "Variant not found");

		assertThatThrownBy(() -> cartService
				.addItem(AddCartItemDTO.builder().productVariantId(VARIANT_ID).quantity(1).build()))
				.isInstanceOf(ApiServiceException.class)
				.hasMessageContaining("Variant not found")
				.satisfies(ex -> assertThat(((ApiServiceException) ex).getStatusCode()).isEqualTo(404));

		verify(cartItemRepository, never()).save(any());
	}

	@Test
	void testAddItemRejectsWhenProductSoftDeleted() {
		product.setDeletedAt(LocalDateTime.now());
		when(currentUserService.getCurrentUser()).thenReturn(user);
		when(cartRepository.findByUser_UserId(USER_ID)).thenReturn(Optional.of(cart));
		when(productVariantRepository.findByIdWithProduct(VARIANT_ID)).thenReturn(Optional.of(variant));
		stubMessage("cart_product_unavailable", "Product unavailable");

		assertThatThrownBy(() -> cartService
				.addItem(AddCartItemDTO.builder().productVariantId(VARIANT_ID).quantity(1).build()))
				.isInstanceOf(ApiServiceException.class)
				.hasMessageContaining("Product unavailable")
				.satisfies(ex -> assertThat(((ApiServiceException) ex).getStatusCode()).isEqualTo(409));

		verify(cartItemRepository, never()).save(any());
	}

	@Test
	void testAddItemRejectsWhenProductNotActive() {
		product.setStatus(ProductStatus.INACTIVE);
		when(currentUserService.getCurrentUser()).thenReturn(user);
		when(cartRepository.findByUser_UserId(USER_ID)).thenReturn(Optional.of(cart));
		when(productVariantRepository.findByIdWithProduct(VARIANT_ID)).thenReturn(Optional.of(variant));
		stubMessage("cart_product_unavailable", "Product unavailable");

		assertThatThrownBy(() -> cartService
				.addItem(AddCartItemDTO.builder().productVariantId(VARIANT_ID).quantity(1).build()))
				.isInstanceOf(ApiServiceException.class)
				.satisfies(ex -> assertThat(((ApiServiceException) ex).getStatusCode()).isEqualTo(409));

		verify(cartItemRepository, never()).save(any());
	}

	// ─── updateItem ──────────────────────────────────────────────────────────

	@Test
	void testUpdateItemSetsQuantity() {
		CartItem line = lineFor(variant, 2);
		when(currentUserService.getCurrentUser()).thenReturn(user);
		when(cartRepository.findByUser_UserId(USER_ID)).thenReturn(Optional.of(cart));
		when(cartItemRepository.findByCartItemIdAndCart_CartId(CART_ITEM_ID, CART_ID)).thenReturn(Optional.of(line));

		cartService.updateItem(CART_ITEM_ID, UpdateCartItemDTO.builder().quantity(6).build());

		ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
		verify(cartItemRepository).save(captor.capture());
		assertThat(captor.getValue().getQuantity()).isEqualTo(6);
		verify(productVariantRepository, never()).save(any());
	}

	@Test
	void testUpdateItemRejectsQuantityBelowOne() {
		CartItem line = lineFor(variant, 2);
		when(currentUserService.getCurrentUser()).thenReturn(user);
		when(cartRepository.findByUser_UserId(USER_ID)).thenReturn(Optional.of(cart));
		when(cartItemRepository.findByCartItemIdAndCart_CartId(CART_ITEM_ID, CART_ID)).thenReturn(Optional.of(line));
		stubMessage("cart_quantity_invalid", "Quantity must be at least 1");

		assertThatThrownBy(() -> cartService.updateItem(CART_ITEM_ID, UpdateCartItemDTO.builder().quantity(0).build()))
				.isInstanceOf(ApiServiceException.class)
				.hasMessageContaining("Quantity must be at least 1")
				.satisfies(ex -> assertThat(((ApiServiceException) ex).getStatusCode()).isEqualTo(400));

		verify(cartItemRepository, never()).save(any());
	}

	@Test
	void testUpdateItemRejectsOverStock() {
		variant.setStock(5);
		CartItem line = lineFor(variant, 2);
		when(currentUserService.getCurrentUser()).thenReturn(user);
		when(cartRepository.findByUser_UserId(USER_ID)).thenReturn(Optional.of(cart));
		when(cartItemRepository.findByCartItemIdAndCart_CartId(CART_ITEM_ID, CART_ID)).thenReturn(Optional.of(line));
		stubMessage("cart_stock_exceeded", "Not enough stock");

		assertThatThrownBy(() -> cartService.updateItem(CART_ITEM_ID, UpdateCartItemDTO.builder().quantity(9).build()))
				.isInstanceOf(ApiServiceException.class)
				.satisfies(ex -> {
					assertThat(((ApiServiceException) ex).getStatusCode()).isEqualTo(400);
					assertThat(((ApiServiceException) ex).getMetadata()).containsEntry("availableStock", 5);
				});

		verify(cartItemRepository, never()).save(any());
	}

	@Test
	void testUpdateItemRejectsWhenItemNotInCallersCart() {
		when(currentUserService.getCurrentUser()).thenReturn(user);
		when(cartRepository.findByUser_UserId(USER_ID)).thenReturn(Optional.of(cart));
		when(cartItemRepository.findByCartItemIdAndCart_CartId(CART_ITEM_ID, CART_ID)).thenReturn(Optional.empty());
		stubMessage("cart_item_not_found", "Cart item not found");

		assertThatThrownBy(() -> cartService.updateItem(CART_ITEM_ID, UpdateCartItemDTO.builder().quantity(2).build()))
				.isInstanceOf(ApiServiceException.class)
				.hasMessageContaining("Cart item not found")
				.satisfies(ex -> assertThat(((ApiServiceException) ex).getStatusCode()).isEqualTo(404));

		verify(cartItemRepository, never()).save(any());
	}

	// ─── removeItem ──────────────────────────────────────────────────────────

	@Test
	void testRemoveItemDeletesLine() {
		CartItem line = lineFor(variant, 2);
		cart.getItems().add(line);
		when(currentUserService.getCurrentUser()).thenReturn(user);
		when(cartRepository.findByUser_UserId(USER_ID)).thenReturn(Optional.of(cart));
		when(cartItemRepository.findByCartItemIdAndCart_CartId(CART_ITEM_ID, CART_ID)).thenReturn(Optional.of(line));

		CartDTO result = cartService.removeItem(CART_ITEM_ID);

		verify(cartItemRepository).delete(line);
		assertThat(result.getItems()).isEmpty();
		assertThat(result.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
		verify(productVariantRepository, never()).save(any());
	}

	@Test
	void testRemoveItemRejectsWhenLineMissing() {
		when(currentUserService.getCurrentUser()).thenReturn(user);
		when(cartRepository.findByUser_UserId(USER_ID)).thenReturn(Optional.of(cart));
		when(cartItemRepository.findByCartItemIdAndCart_CartId(CART_ITEM_ID, CART_ID)).thenReturn(Optional.empty());
		stubMessage("cart_item_not_found", "Cart item not found");

		assertThatThrownBy(() -> cartService.removeItem(CART_ITEM_ID))
				.isInstanceOf(ApiServiceException.class)
				.hasMessageContaining("Cart item not found")
				.satisfies(ex -> assertThat(((ApiServiceException) ex).getStatusCode()).isEqualTo(404));

		verify(cartItemRepository, never()).delete(any());
	}

	// ─── toDTO price selection ───────────────────────────────────────────────

	@Test
	void testCartMappingUsesBasePriceWhenNoDiscount() {
		cart.getItems().add(lineFor(variant, 3));
		when(currentUserService.getCurrentUser()).thenReturn(user);
		when(cartRepository.findByUser_UserId(USER_ID)).thenReturn(Optional.of(cart));

		CartDTO result = cartService.getCart();

		assertThat(result.getItems().get(0).getUnitPrice()).isEqualByComparingTo("100.00");
		assertThat(result.getItems().get(0).getDiscountPrice()).isNull();
		assertThat(result.getItems().get(0).getLineTotal()).isEqualByComparingTo("300.00");
		assertThat(result.getSubtotal()).isEqualByComparingTo("300.00");
		assertThat(result.getTotalItems()).isEqualTo(3);
	}

	@Test
	void testCartSubtotalAndCountSumAcrossMultipleLines() {
		ProductVariant second = new ProductVariant();
		second.setProductVariantId(101L);
		second.setProduct(product);
		second.setSku("RING-14K-6");
		second.setPrice(new BigDecimal("50.00"));
		second.setStock(20);

		cart.getItems().add(lineFor(variant, 2));
		CartItem secondLine = CartItem.builder().cartItemId(501L).cart(cart).productVariant(second).quantity(4).build();
		cart.getItems().add(secondLine);

		when(currentUserService.getCurrentUser()).thenReturn(user);
		when(cartRepository.findByUser_UserId(USER_ID)).thenReturn(Optional.of(cart));

		CartDTO result = cartService.getCart();

		assertThat(result.getItems()).hasSize(2);
		assertThat(result.getSubtotal()).isEqualByComparingTo("400.00");
		assertThat(result.getTotalItems()).isEqualTo(6);
	}
}
