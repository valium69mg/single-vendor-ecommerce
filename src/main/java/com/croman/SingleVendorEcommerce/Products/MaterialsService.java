package com.croman.SingleVendorEcommerce.Products;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.croman.SingleVendorEcommerce.Exceptions.ApiServiceException;
import com.croman.SingleVendorEcommerce.General.LocaleUtils;
import com.croman.SingleVendorEcommerce.General.PaginationUtils;
import com.croman.SingleVendorEcommerce.Message.MessageService;
import com.croman.SingleVendorEcommerce.Products.DTO.CreateMaterialDTO;
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
	private final MessageService messageService;
	
	public List<MaterialDTO> getMaterials(String languageName, int page, int size) {
		
		Pageable pageable = PaginationUtils.getPageable(page, size, "materialId");
		
		List<Material> allMaterials = materialRepository.findAll(pageable).getContent();

		HashMap<Integer, String> batchTranslateHashMap = null;
		
		if (!languageName.equals(LocaleUtils.DATABASE_DEFAULT_LANG)) {
			List<Long> materialIds = allMaterials.stream().map(Material::getMaterialId)
					.distinct().toList();
			batchTranslateHashMap = translationService.batchTranslate(languageName, TranslatorPropertyType.MATERIAL,
					materialIds);
		}

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
	
	@Transactional
	public void createMaterial(CreateMaterialDTO createMaterialDTO) {
		String englishName = createMaterialDTO.getEnglishName();
		String spanishName = createMaterialDTO.getSpanishName();
		
		Optional<Material> materialOpt = materialRepository.findByName(englishName);
		
		if (materialOpt.isPresent()) {
			throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
					messageService.getMessage("material_already_exists", LocaleUtils.getDefaultLocale()));
		}
		
		Material material = Material.builder().name(englishName).build();
	
		material = materialRepository.save(material);
		
		translationService.createTranslation(material.getMaterialId().intValue(), LocaleUtils.ES,
				TranslatorPropertyType.MATERIAL, spanishName);
	
	}
	

}
