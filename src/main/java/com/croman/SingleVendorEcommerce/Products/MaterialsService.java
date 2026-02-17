package com.croman.SingleVendorEcommerce.Products;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.croman.SingleVendorEcommerce.General.LocaleUtils;
import com.croman.SingleVendorEcommerce.Message.MessageService;
import com.croman.SingleVendorEcommerce.Products.DTO.MaterialDTO;
import com.croman.SingleVendorEcommerce.Products.Entity.Material;
import com.croman.SingleVendorEcommerce.Products.Repository.MaterialRepository;
import com.croman.SingleVendorEcommerce.Translations.TranslationService;
import com.croman.SingleVendorEcommerce.Translations.DTO.TranslatorPropertyType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MaterialsService {

	private final MaterialRepository materialRepository;
	private final TranslationService translationService;
	
	public List<MaterialDTO> getMaterials(String languageName) {

		HashMap<Integer, String> batchTranslateHashMap = null;

		if (!languageName.equals(LocaleUtils.DATABASE_DEFAULT_LANG)) {
			batchTranslateHashMap = translationService.batchTranslate(languageName, TranslatorPropertyType.MATERIAL);
		}

		List<Material> allMaterials = materialRepository.findAll();
		List<MaterialDTO> materialDTOs = new ArrayList<>();

		for (Material material : allMaterials) {
			materialDTOs.add(mapMaterialToMaterialDTO(material, batchTranslateHashMap));
		}

		return materialDTOs;

	}

	private MaterialDTO mapMaterialToMaterialDTO(Material material, HashMap<Integer, String> batchTranslateHashMap) {
		Integer key = material.getMaterialId().intValue();
		String name = batchTranslateHashMap != null ? batchTranslateHashMap.get(key) : material.getName();
		return MaterialDTO.builder().materialId(material.getMaterialId()).name(name).build();
	}

}
