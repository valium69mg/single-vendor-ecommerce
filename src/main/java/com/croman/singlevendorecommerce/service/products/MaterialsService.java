package com.croman.singlevendorecommerce.service.products;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.croman.singlevendorecommerce.dto.products.CreateMaterialDTO;
import com.croman.singlevendorecommerce.dto.products.MaterialByIdDTO;
import com.croman.singlevendorecommerce.dto.products.MaterialDTO;
import com.croman.singlevendorecommerce.dto.products.UpdateMaterialDTO;
import com.croman.singlevendorecommerce.dto.translations.TranslatorPropertyType;
import com.croman.singlevendorecommerce.dto.utils.PageResponse;
import com.croman.singlevendorecommerce.entity.products.Material;
import com.croman.singlevendorecommerce.repository.products.MaterialRepository;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.service.translations.TranslationService;
import com.croman.singlevendorecommerce.utils.LocaleUtils;
import com.croman.singlevendorecommerce.utils.PaginationUtils;
import com.croman.singlevendorecommerce.utils.exceptions.ApiServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MaterialsService {

	private final MaterialRepository materialRepository;
	private final TranslationService translationService;
	private final MessageService messageService;
	private static final String MATERIAL_NOT_FOUND = "material_not_found";
	
	@Transactional(readOnly = true)
	public PageResponse<MaterialDTO> getMaterials(String languageName, int page, int size, String term) {
		
		Pageable pageable;
		
		Page<Material> allMaterials;
		
		if (term.isEmpty()) {
			pageable = PaginationUtils.getPageable(page, size, "materialId");
			allMaterials = materialRepository.findAll(pageable);
		} else {
			pageable  = PaginationUtils.getPageable(page, size, "material_id");
			allMaterials = materialRepository.searchByNameOrTranslation(term, pageable);
		}

		Map<Integer, String> batchTranslateHashMap = null;
		
		if (!languageName.equals(LocaleUtils.DATABASE_DEFAULT_LANG)) {
			List<Long> materialIds = allMaterials.stream().map(Material::getMaterialId)
					.distinct().toList();
			batchTranslateHashMap = translationService.batchTranslate(languageName, TranslatorPropertyType.MATERIAL,
					materialIds);
		}

		List<MaterialDTO> materialDTOs = new ArrayList<>();

		for (Material material : allMaterials.getContent()) {
			materialDTOs.add(mapMaterialToMaterialDTO(material, batchTranslateHashMap));
		}
		
		return PageResponse.<MaterialDTO>builder()
	            .content(materialDTOs)
	            .page(allMaterials.getNumber())
	            .size(allMaterials.getSize())
	            .totalElements(allMaterials.getTotalElements())
	            .totalPages(allMaterials.getTotalPages())
	            .last(allMaterials.isLast())
	            .build();

	}

	private MaterialDTO mapMaterialToMaterialDTO(Material material, Map<Integer, String> batchTranslateHashMap) {
		Integer key = material.getMaterialId().intValue();
		String name = batchTranslateHashMap != null ? batchTranslateHashMap.get(key) : material.getName();
		return MaterialDTO.builder().materialId(material.getMaterialId()).name(name).build();
	}
	
	@Transactional(readOnly = true)
	public MaterialByIdDTO getMaterialById(Long materialId) {
		
		Material material = materialRepository.findById(materialId).orElseThrow(
				() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(), messageService.getMessage(MATERIAL_NOT_FOUND, 
						LocaleUtils.getDefaultLocale())));
		
		Map<Integer, String> batchTranslateHashMap = translationService.batchTranslate(LocaleUtils.ES,
				TranslatorPropertyType.MATERIAL, List.of(material.getMaterialId()));
	
		return mapMaterialToMaterialByIdDTO(material, batchTranslateHashMap);
		
	}
	
	private MaterialByIdDTO mapMaterialToMaterialByIdDTO(Material material, Map<Integer, String> batchTranslateHashMap) {
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
	
	@Transactional
	public void updateMaterial(Long materialId, UpdateMaterialDTO updateMaterialDTO) {
		String englishName = updateMaterialDTO.getEnglishName();
		String spanishName = updateMaterialDTO.getSpanishName();

		if (englishName == null && spanishName == null) {
			throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
					messageService.getMessage("missing_language_names", LocaleUtils.getDefaultLocale()));
		}
		
		Material material = materialRepository.findById(materialId)
				.orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
						messageService.getMessage(MATERIAL_NOT_FOUND, LocaleUtils.getDefaultLocale())));
		
		if (englishName != null) {
			material.setName(englishName);
			materialRepository.save(material);
		}
		
		if (spanishName != null) {
			translationService.updateTranslation(materialId.intValue(), LocaleUtils.ES, TranslatorPropertyType.MATERIAL,
					spanishName);
		}
		
	}
	
	@Transactional
	public void deleteMaterial(Long materialId) {
		boolean exists = materialRepository.existsById(materialId);
		
		if (!exists) {
			throw new ApiServiceException(HttpStatus.NOT_FOUND.value(),
					messageService.getMessage(MATERIAL_NOT_FOUND, LocaleUtils.getDefaultLocale()));
		}
		
		translationService.deleteTranslation(materialId.intValue(), LocaleUtils.ES, TranslatorPropertyType.MATERIAL);
		
		materialRepository.deleteById(materialId);
		
	}

}
