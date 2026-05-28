package com.croman.singlevendorecommerce.repository.products;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.croman.singlevendorecommerce.entity.products.Material;
import com.croman.singlevendorecommerce.entity.products.Product;
import com.croman.singlevendorecommerce.entity.products.ProductMaterial;

public interface ProductMaterialRepository extends JpaRepository<ProductMaterial, Long> {

	List<ProductMaterial> findByProductProductId(UUID productId);

	void deleteByProductAndMaterialIn(Product product, List<Material> materials);
}
