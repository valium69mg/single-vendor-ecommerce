package com.croman.singlevendorecommerce.dto.orders;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record CreateOrderRequest(@Valid @NotNull ShippingAddressDTO shippingAddress) {
}
