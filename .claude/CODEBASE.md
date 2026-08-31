# Codebase Map

## Directory tree

```
src/main/java/com/croman/singlevendorecommerce/
├── config/
│   ├── MessageConfig.java          # MessageSource bean (classpath messages*.properties)
│   ├── SecurityConfig.java         # Spring Security filter chain, role rules, CORS
│   └── WebConfig.java              # Static file serving for FILE_DIRECTORY
├── controller/
│   ├── auth/AuthController.java    # POST /api/v1/auth/login
│   ├── health/HealthController.java# GET /health
│   ├── products/
│   │   ├── AdminProductsController.java  # /api/v1/admin/products/** (ADMIN)
│   │   └── ProductsController.java       # /api/v1/products/** (public/USER)
│   ├── storage/FileController.java # GET /api/v1/file/** (serves stored files)
│   ├── cart/CartController.java    # /api/v1/cart/** (authenticated USER)
│   └── users/UserController.java   # /api/v1/users/**
├── dto/
│   ├── DefaultApiResponse.java     # { status, message } for mutations
│   ├── auth/                       # LoginDTO, LoginResponseDTO, LoginContextDTO
│   ├── products/                   # Per-domain request/response DTOs
│   ├── roles/RoleType.java         # ADMIN / USER / GUEST enum
│   ├── storage/StoredFile.java
│   ├── users/
│   └── utils/PageResponse.java     # Generic paginated response
├── entity/
│   ├── auth/LoginAttempt.java, RefreshToken.java
│   ├── products/
│   │   ├── Attribute.java, AttributeValue.java
│   │   ├── Brand.java, Category.java, Material.java
│   │   ├── Product.java            # UUID PK, status enum, soft-delete, brand+category FK
│   │   ├── ProductMaterial.java    # M:N product↔material join
│   │   ├── ProductVariant.java     # SKU, price, discount_price, stock, weight_grams
│   │   └── ProductVariantAttribute.java  # M:N variant↔attribute_value join
│   ├── roles/UserRole.java
│   ├── cart/Cart.java, CartItem.java  # one cart per user (user_id UNIQUE), lazy-created
│   └── users/User.java
├── repository/                     # Spring Data JPA interfaces, JPQL custom queries
├── service/
│   ├── auth/AuthService.java       # Login, lockout logic
│   ├── message/MessageService.java # Wraps MessageSource
│   ├── products/
│   │   ├── AttributesService.java
│   │   ├── BrandsService.java
│   │   ├── CategoryService.java
│   │   ├── MaterialsService.java
│   │   └── ProductService.java
│   ├── roles/RolesService.java
│   ├── cart/CartService.java       # get/add/update/remove; live totals; read-only stock guard
│   ├── users/CurrentUserService.java # SecurityContextHolder → email principal → User (401 guard)
│   ├── storage/
│   │   ├── StorageService.java     # Interface
│   │   └── LocalStorageService.java# Implementation: writes to FILE_DIRECTORY on disk
│   ├── thumbnail/ThumbnailService.java  # Synchronous Thumbnailator resize (200×200, 400×400)
│   └── users/UserService.java
└── utils/
    ├── ApiResponseService.java     # Builds DefaultApiResponse from message keys
    ├── DateTimeUtils.java
    ├── EnvironmentUtils.java
    ├── FileUtils.java              # toSmallThumbnailKey(), toMediumThumbnailKey(), extension helpers
    ├── HttpUtils.java
    ├── LocaleUtils.java            # getDefaultLocale() → Locale("es")
    ├── PaginationUtils.java        # getPageable(page, size, sortField)
    ├── PasswordUtils.java
    ├── exceptions/
    │   ├── ApiServiceException.java
    │   └── GlobalExceptionHandler.java
    └── jwt/JwtAuthenticationFilter.java, JwtUtil.java

src/main/resources/
├── application.yaml
├── db/migration/V1__…V27__*.sql   # Flyway (auto-run on startup); V27 = carts + cart_items
├── messages.properties             # English fallback keys
└── messages_es.properties          # Spanish keys (default locale)
```

## Data model

| Entity | PK type | Key fields | Notes |
|---|---|---|---|
| `users` | BIGSERIAL | email, password_hash, role | role column (ADMIN/USER/GUEST) |
| `refresh_tokens` | BIGSERIAL | token, user_id, expires_at | ON DELETE CASCADE |
| `login_attempts` | BIGSERIAL | user_id, created_at | ON DELETE CASCADE; lockout at 5/1h |
| `user_roles` | BIGSERIAL | role_name | lookup table |
| `categories` | BIGSERIAL | name (unique), file_url, deleted_at | soft-delete |
| `brands` | BIGSERIAL | name (unique), deleted_at | soft-delete |
| `materials` | BIGSERIAL | name (unique), deleted_at | soft-delete |
| `attributes` | BIGSERIAL | attribute_type (COLOR/SIZE/CARAT), name | name stored in Spanish |
| `attribute_values` | BIGSERIAL | attribute_id, value | value stored in Spanish |
| `products` | UUID | name, status, featured, brand_id, category_id, units_sold, file_url, deleted_at | soft-delete |
| `product_materials` | BIGSERIAL | product_id, material_id | UNIQUE(product_id, material_id) |
| `product_variants` | BIGSERIAL | product_id, sku (UNIQUE), price, discount_price, stock, weight_grams | |
| `product_variant_attributes` | BIGSERIAL | product_variant_id, attribute_value_id | UNIQUE per pair |
| `carts` | BIGSERIAL | user_id (UNIQUE) | one per user, lazy-created; FK → users |
| `cart_items` | BIGSERIAL | cart_id, product_variant_id, quantity | UNIQUE(cart_id, product_variant_id); FK cart_id ON DELETE CASCADE; CHECK quantity > 0; no price snapshot |

## Key routes

| Method | Path | Auth | Handler |
|---|---|---|---|
| POST | /api/v1/auth/login | Public | AuthController |
| POST | /api/v1/users/register | Public | UserController |
| GET | /api/v1/file/** | Public | FileController |
| GET | /api/v1/products | Public | ProductsController |
| GET | /api/v1/products/{id} | Public | ProductsController |
| GET | /api/v1/admin/products | ADMIN | AdminProductsController |
| POST | /api/v1/admin/products | ADMIN | AdminProductsController |
| PATCH | /api/v1/admin/products/{id} | ADMIN | AdminProductsController |
| DELETE | /api/v1/admin/products/{id} | ADMIN | AdminProductsController |
| GET | /api/v1/admin/products/categories | ADMIN | AdminProductsController |
| POST | /api/v1/admin/products/categories | ADMIN | AdminProductsController |
| POST | /api/v1/admin/products/categories/{id}/image | ADMIN | AdminProductsController |
| PATCH | /api/v1/admin/products/categories/{id} | ADMIN | AdminProductsController |
| DELETE | /api/v1/admin/products/categories/{id} | ADMIN | AdminProductsController |
| PATCH | /api/v1/admin/products/categories/{id}/restore | ADMIN | AdminProductsController |
| (same pattern) | /admin/products/materials, /brands, /attributes | ADMIN | AdminProductsController |
| GET | /api/v1/cart | USER (JWT) | CartController |
| POST | /api/v1/cart/items | USER (JWT) | CartController |
| PATCH | /api/v1/cart/items/{cartItemId} | USER (JWT) | CartController |
| DELETE | /api/v1/cart/items/{cartItemId} | USER (JWT) | CartController — returns updated CartDTO (200) |

## External integrations

| System | How | Config |
|---|---|---|
| PostgreSQL 16 | Spring Data JPA / Hibernate | DB_HOST, DB_PORT, DB_NAME, DB_USERNAME, DB_PASSWORD |
| Flyway | Auto-run on startup | `db/migration/V*.sql` |
| Local file storage | LocalStorageService writes to disk | FILE_DIRECTORY, served via /api/v1/file/** |
| Thumbnailator | In-process resize on upload | no external dependency |
| JWT (HS256) | JwtUtil + JwtAuthenticationFilter | JWT_SECRET, JWT_EXPIRATION |

## Environment variables

```
DB_HOST / DB_PORT / DB_NAME / DB_USERNAME / DB_PASSWORD
JWT_SECRET          # HMAC secret
JWT_EXPIRATION      # milliseconds (default 3600000)
FILE_DIRECTORY      # absolute path on host for file storage
CORS_ORIGIN         # allowed origin (e.g. http://frontend:80)
SPRING_PROFILES_ACTIVE  # dev | prod
```
