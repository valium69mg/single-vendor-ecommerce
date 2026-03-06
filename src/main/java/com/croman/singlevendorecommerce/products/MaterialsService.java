package com.croman.singlevendorecommerce.products;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.croman.singlevendorecommerce.exceptions.ApiServiceException;
import com.croman.singlevendorecommerce.general.LocaleUtils;
import com.croman.singlevendorecommerce.general.PaginationUtils;
import com.croman.singlevendorecommerce.message.MessageService;
import com.croman.singlevendorecommerce.products.dto.CreateMaterialDTO;
import com.croman.singlevendorecommerce.products.dto.MaterialByIdDTO;
import com.croman.singlevendorecommerce.products.dto.MaterialDTO;
import com.croman.singlevendorecommerce.products.entity.Material;
import com.croman.singlevendorecommerce.products.repository.MaterialRepository;
import com.croman.singlevendorecommerce.translations.TranslationService;
import com.croman.singlevendorecommerce.translations.dto.TranslatorPropertyType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MaterialsService {

	private final MaterialRepository materialRepository;
	private final TranslationService translationService;
	private final MessageService messageService;
	
	@Transactional(readOnly = true)
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
	
	@Transactional(readOnly = true)
	public MaterialByIdDTO getMaterialById(Long materialId) {
		
		Material material = materialRepository.findById(materialId).orElseThrow(
				() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(), messageService.getMessage("material_not_found", 
						LocaleUtils.getDefaultLocale())));
		
		HashMap<Integer, String> batchTranslateHashMap = translationService.batchTranslate(LocaleUtils.ES,
				TranslatorPropertyType.MATERIAL, List.of(material.getMaterialId()));
	
		return mapMaterialToMaterialByIdDTO(material, batchTranslateHashMap);
		
	}
	
	private MaterialByIdDTO mapMaterialToMaterialByIdDTO(Material material, HashMap<Integer, String> batchTranslateHashMap) {
		Integer key = material.getMaterialId().intValue();
		String spanishName = batchTranslateHashMap != null ? batchTranslateHashMap.get(key) : material.getName();
		return MaterialByIdDTO.builder().materialId(material.getMaterialId()).englishName(material.getName())
		.spanishName(spanishName).build();
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
