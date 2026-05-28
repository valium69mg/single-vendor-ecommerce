package com.croman.singlevendorecommerce.service.products;

import java.util.ArrayList;
import java.util.List;
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
import com.croman.singlevendorecommerce.dto.utils.PageResponse;
import com.croman.singlevendorecommerce.entity.products.Material;
import com.croman.singlevendorecommerce.repository.products.MaterialRepository;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.utils.LocaleUtils;
import com.croman.singlevendorecommerce.utils.PaginationUtils;
import com.croman.singlevendorecommerce.utils.exceptions.ApiServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MaterialsService {

	private final MaterialRepository materialRepository;
	private final MessageService messageService;
	private static final String MATERIAL_NOT_FOUND = "material_not_found";

	@Transactional(readOnly = true)
	public PageResponse<MaterialDTO> getMaterials(int page, int size, String term) {

		Pageable pageable = PaginationUtils.getPageable(page, size, "materialId");

		Page<Material> allMaterials;

		if (term.isEmpty()) {
			allMaterials = materialRepository.findAll(pageable);
		} else {
			allMaterials = materialRepository.searchByName(term, pageable);
		}

		List<MaterialDTO> materialDTOs = new ArrayList<>();

		for (Material material : allMaterials.getContent()) {
			materialDTOs.add(mapMaterialToMaterialDTO(material));
		}

		return PageResponse.<MaterialDTO>builder().content(materialDTOs).page(allMaterials.getNumber())
				.size(allMaterials.getSize()).totalElements(allMaterials.getTotalElements())
				.totalPages(allMaterials.getTotalPages()).last(allMaterials.isLast()).build();

	}

	private MaterialDTO mapMaterialToMaterialDTO(Material material) {
		return MaterialDTO.builder().materialId(material.getMaterialId()).name(material.getName()).build();
	}

	@Transactional(readOnly = true)
	public MaterialByIdDTO getMaterialById(Long materialId) {

		Material material = materialRepository.findById(materialId)
				.orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
						messageService.getMessage(MATERIAL_NOT_FOUND, LocaleUtils.getDefaultLocale())));

		return mapMaterialToMaterialByIdDTO(material);

	}

	private MaterialByIdDTO mapMaterialToMaterialByIdDTO(Material material) {
		return MaterialByIdDTO.builder().materialId(material.getMaterialId()).name(material.getName()).build();
	}

	@Transactional
	public void createMaterial(CreateMaterialDTO createMaterialDTO) {
		String name = createMaterialDTO.getName();

		Optional<Material> materialOpt = materialRepository.findByName(name);

		if (materialOpt.isPresent()) {
			throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
					messageService.getMessage("material_already_exists", LocaleUtils.getDefaultLocale()));
		}

		Material material = Material.builder().name(name).build();

		materialRepository.save(material);

	}

	@Transactional
	public void updateMaterial(Long materialId, UpdateMaterialDTO updateMaterialDTO) {
		String name = updateMaterialDTO.getName();

		if (name == null) {
			throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
					messageService.getMessage("missing_name", LocaleUtils.getDefaultLocale()));
		}

		Material material = materialRepository.findById(materialId)
				.orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
						messageService.getMessage(MATERIAL_NOT_FOUND, LocaleUtils.getDefaultLocale())));

		updateName(material, name);

	}

	private void updateName(Material material, String name) {
		Optional<Material> existingMaterialOptional = materialRepository.findByName(name);
		boolean nameAlreadyExists = existingMaterialOptional.isPresent()
				&& !existingMaterialOptional.get().equals(material);
		if (nameAlreadyExists) {
			throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
					messageService.getMessage("name_already_taken", LocaleUtils.getDefaultLocale()));
		}
		material.setName(name);
		materialRepository.save(material);
	}

	@Transactional
	public void deleteMaterial(Long materialId) {
		boolean exists = materialRepository.existsById(materialId);

		if (!exists) {
			throw new ApiServiceException(HttpStatus.NOT_FOUND.value(),
					messageService.getMessage(MATERIAL_NOT_FOUND, LocaleUtils.getDefaultLocale()));
		}

		materialRepository.deleteById(materialId);

	}

}
