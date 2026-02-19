package com.croman.SingleVendorEcommerce.Products.Entity;

import com.croman.SingleVendorEcommerce.Products.DTO.AttributeType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "attributes")
public class Attribute {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attribute_id")
    private Long attributeId;
	
	@Enumerated(EnumType.STRING)
    @Column(name = "attribute_type", nullable = false, length = 50, unique = true)
    private AttributeType attributeType;

}
