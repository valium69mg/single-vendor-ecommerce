package com.croman.singlevendorecommerce.integration.support;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;

import com.croman.singlevendorecommerce.dto.products.ProductStatus;
import com.croman.singlevendorecommerce.dto.roles.RoleType;
import com.croman.singlevendorecommerce.entity.cart.Cart;
import com.croman.singlevendorecommerce.entity.cart.CartItem;
import com.croman.singlevendorecommerce.entity.products.Category;
import com.croman.singlevendorecommerce.entity.products.Product;
import com.croman.singlevendorecommerce.entity.products.ProductVariant;
import com.croman.singlevendorecommerce.entity.users.User;
import com.croman.singlevendorecommerce.repository.cart.CartItemRepository;
import com.croman.singlevendorecommerce.repository.cart.CartRepository;
import com.croman.singlevendorecommerce.repository.products.CategoryRepository;
import com.croman.singlevendorecommerce.repository.products.ProductRepository;
import com.croman.singlevendorecommerce.repository.products.ProductVariantRepository;
import com.croman.singlevendorecommerce.repository.roles.UserRoleRepository;
import com.croman.singlevendorecommerce.repository.users.UserRepository;
import com.croman.singlevendorecommerce.utils.SlugUtils;

/**
 * Stateless construction helper for authenticated-cart integration-test fixtures,
 * mirroring {@link ProductFixtures} / {@link AuthFixtures} (private constructor,
 * static helpers, called from each test's {@code @BeforeEach}).
 *
 * <p>Encodes the traps: {@code Product.builder()} must set {@code .featured(false)}
 * and {@code .unitsSold(0)} ({@code NOT NULL}, builder drops initialisers);
 * {@link ProductVariant} has no {@code @Builder} (no-args ctor + setters, {@code sku}
 * unique &lt;= 100 chars with a {@code "CART-IT-"} prefix, {@code price} / {@code stock}
 * {@code NOT NULL}); category is looked up by the V21 spanish name {@code "Anillos"};
 * {@code carts.user_id} is {@code UNIQUE NOT NULL} so {@link #seedCartLine} reuses an
 * existing cart row.
 */
public final class CartFixtures {

    // Flyway V12 seeds English category names; V21 ("spanish only") overwrites
    // `categories.name` with the Spanish translation, so the runtime name is Spanish.
    private static final String RINGS_CATEGORY = "Anillos";
    private static final String SKU_PREFIX = "CART-IT-";

    private CartFixtures() {
    }

    /** Convenience delegate: persist an active, validated {@link RoleType#USER}. */
    public static User seedShopper(UserRepository userRepo, UserRoleRepository roleRepo, String email,
            String rawPassword) {
        return AuthFixtures.seedUser(userRepo, roleRepo, email, rawPassword, RoleType.USER);
    }

    /** ACTIVE product (category {@code "Anillos"}) + one variant with a discount price. */
    public static ProductVariant seedActiveVariant(ProductRepository productRepo, CategoryRepository categoryRepo,
            ProductVariantRepository variantRepo, String skuSuffix, BigDecimal price, BigDecimal discountPrice,
            int stock) {
        return seedVariant(productRepo, categoryRepo, variantRepo, skuSuffix, price, discountPrice, stock,
                ProductStatus.ACTIVE, false);
    }

    /** ACTIVE product (category {@code "Anillos"}) + one variant, no discount price. */
    public static ProductVariant seedActiveVariant(ProductRepository productRepo, CategoryRepository categoryRepo,
            ProductVariantRepository variantRepo, String skuSuffix, BigDecimal price, int stock) {
        return seedVariant(productRepo, categoryRepo, variantRepo, skuSuffix, price, null, stock,
                ProductStatus.ACTIVE, false);
    }

    /** Variant whose owning product is {@code INACTIVE} (add -&gt; 409). */
    public static ProductVariant seedInactiveProductVariant(ProductRepository productRepo,
            CategoryRepository categoryRepo, ProductVariantRepository variantRepo, String skuSuffix, BigDecimal price,
            int stock) {
        return seedVariant(productRepo, categoryRepo, variantRepo, skuSuffix, price, null, stock,
                ProductStatus.INACTIVE, false);
    }

    /** Variant whose owning product is soft-deleted ({@code deletedAt} set; add -&gt; 409). */
    public static ProductVariant seedSoftDeletedProductVariant(ProductRepository productRepo,
            CategoryRepository categoryRepo, ProductVariantRepository variantRepo, String skuSuffix, BigDecimal price,
            int stock) {
        return seedVariant(productRepo, categoryRepo, variantRepo, skuSuffix, price, null, stock,
                ProductStatus.ACTIVE, true);
    }

    /** Persist an empty {@link Cart} for the user (direct repo write, bypassing HTTP). */
    public static Cart seedCart(CartRepository cartRepo, User user) {
        return cartRepo.findByUser_UserId(user.getUserId())
                .orElseGet(() -> cartRepo.save(Cart.builder().user(user).items(new ArrayList<>()).build()));
    }

    /**
     * Persist a {@link CartItem} for the user, lazy-creating the {@link Cart} row if
     * absent. Direct repo write — for PATCH / DELETE / cross-user-isolation setup
     * that must not go through the API.
     */
    public static CartItem seedCartLine(CartRepository cartRepo, CartItemRepository itemRepo, User user,
            ProductVariant variant, int quantity) {
        Cart cart = seedCart(cartRepo, user);
        return addLine(itemRepo, cart, variant, quantity);
    }

    /** Append a {@link CartItem} to an existing {@link Cart} (direct repo write). */
    public static CartItem addLine(CartItemRepository itemRepo, Cart cart, ProductVariant variant, int quantity) {
        CartItem item = CartItem.builder().cart(cart).productVariant(variant).quantity(quantity).build();
        return itemRepo.save(item);
    }

    private static ProductVariant seedVariant(ProductRepository productRepo, CategoryRepository categoryRepo,
            ProductVariantRepository variantRepo, String skuSuffix, BigDecimal price, BigDecimal discountPrice,
            int stock, ProductStatus status, boolean softDeleted) {
        Category rings = categoryRepo.findByName(RINGS_CATEGORY)
                .orElseThrow(() -> new IllegalStateException("Flyway category seed missing: " + RINGS_CATEGORY));
        String name = "Cart IT " + skuSuffix;
        Product product = productRepo.save(Product.builder().name(name).slug(SlugUtils.slugify(name)).status(status)
                .featured(false).unitsSold(0).deletedAt(softDeleted ? LocalDateTime.now() : null).category(rings)
                .build());
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSku(SKU_PREFIX + skuSuffix);
        variant.setPrice(price);
        variant.setDiscountPrice(discountPrice);
        variant.setStock(stock);
        return variantRepo.save(variant);
    }
}
