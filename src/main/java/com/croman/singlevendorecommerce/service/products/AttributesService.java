package com.croman.singlevendorecommerce.service.products;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.croman.singlevendorecommerce.dto.products.AttributeByIdDTO;
import com.croman.singlevendorecommerce.dto.products.AttributesDTO;
import com.croman.singlevendorecommerce.dto.products.CreateAttributeDTO;
import com.croman.singlevendorecommerce.dto.products.UpdateAttributeDTO;
import com.croman.singlevendorecommerce.entity.products.Attribute;
import com.croman.singlevendorecommerce.entity.products.AttributeValue;
import com.croman.singlevendorecommerce.repository.products.AttributeRepository;
import com.croman.singlevendorecommerce.repository.products.AttributeValueRepository;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.utils.LocaleUtils;
import com.croman.singlevendorecommerce.utils.PaginationUtils;
import com.croman.singlevendorecommerce.utils.exceptions.ApiServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttributesService {

	private final AttributeRepository attributeRepository;
	private final AttributeValueRepository attributeValueRepository;
	private final MessageService messageService;

	private static final String ATTRIBUTE_NOT_FOUND = "attribute_not_found";
	private static final String ATTRIBUTE_ALREADY_EXISTS = "attribute_already_exists";

	@Transactional(readOnly = true)
	public List<AttributesDTO> getAttributes(int page, int size) {

		Pageable pageable = PaginationUtils.getPageable(page, size, "attributeId");

		List<Attribute> attributes = attributeRepository.findAll(pageable).getContent();

		List<Long> attributeIds = attributes.stream().map(Attribute::getAttributeId).toList();

		List<AttributeValue> allAttributeValues = attributeValueRepository.findByAttributeIdIn(attributeIds);

		Map<Attribute, Set<AttributeValue>> valuesByAttribute = getValuesByAttribute(attributes, allAttributeValues);

		List<AttributesDTO> attributesDTOs = new ArrayList<>();

		for (Map.Entry<Attribute, Set<AttributeValue>> entry : valuesByAttribute.entrySet()) {
			attributesDTOs.add(mapAttributeToAttributeDTO(entry.getKey(), entry.getValue()));
		}

		return attributesDTOs;
	}

	@Transactional(readOnly = true)
	public AttributeByIdDTO getAttributeById(Long attributeId) {
		Attribute attribute = attributeRepository.findById(attributeId)
				.orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
						messageService.getMessage(ATTRIBUTE_NOT_FOUND, LocaleUtils.getDefaultLocale())));

		List<AttributeValue> values = attributeValueRepository.findByAttributeIdIn(List.of(attributeId));

		List<AttributeByIdDTO.ValueDTO> valueDTOs = values.stream()
				.map(av -> AttributeByIdDTO.ValueDTO.builder()
						.attributeValueId(av.getAttributeValueId())
						.value(av.getValue())
						.build())
				.toList();

		return AttributeByIdDTO.builder()
				.attributeId(attribute.getAttributeId())
				.attributeType(attribute.getAttributeType())
				.name(attribute.getName())
				.attributeValues(valueDTOs)
				.build();
	}

	@Transactional
	public void createAttribute(CreateAttributeDTO createAttributeDTO) {
		String attributeType = createAttributeDTO.getAttributeType().toUpperCase();
		String name = createAttributeDTO.getName();

		Optional<Attribute> existing = attributeRepository.findByAttributeType(attributeType);

		if (existing.isPresent()) {
			throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
					messageService.getMessage(ATTRIBUTE_ALREADY_EXISTS, LocaleUtils.getDefaultLocale()));
		}

		Attribute attribute = Attribute.builder().attributeType(attributeType).name(name).build();
		attributeRepository.save(attribute);
	}

	@Transactional
	public void updateAttribute(Long attributeId, UpdateAttributeDTO updateAttributeDTO) {
		String name = updateAttributeDTO.getName();

		if (name == null) {
			throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
					messageService.getMessage("missing_name", LocaleUtils.getDefaultLocale()));
		}

		Attribute attribute = attributeRepository.findById(attributeId)
				.orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
						messageService.getMessage(ATTRIBUTE_NOT_FOUND, LocaleUtils.getDefaultLocale())));

		attribute.setName(name);
		attributeRepository.save(attribute);
	}

	@Transactional
	public void deleteAttribute(Long attributeId) {
		boolean exists = attributeRepository.existsById(attributeId);

		if (!exists) {
			throw new ApiServiceException(HttpStatus.NOT_FOUND.value(),
					messageService.getMessage(ATTRIBUTE_NOT_FOUND, LocaleUtils.getDefaultLocale()));
		}

		attributeRepository.deleteById(attributeId);
	}

	private Map<Attribute, Set<AttributeValue>> getValuesByAttribute(List<Attribute> attributes,
			List<AttributeValue> allAttributeValues) {
		Map<Attribute, Set<AttributeValue>> valuesByAttribute = new LinkedHashMap<>();

		for (Attribute attribute : attributes) {
			valuesByAttribute.put(attribute, new LinkedHashSet<>());
		}
		for (AttributeValue av : allAttributeValues) {
			valuesByAttribute.get(av.getAttribute()).add(av);
		}
		return valuesByAttribute;
	}

	private AttributesDTO mapAttributeToAttributeDTO(Attribute attribute, Set<AttributeValue> values) {

		List<AttributesDTO.AttributeValueDTO> attributeValueDTOs = new ArrayList<>();
		for (AttributeValue av : values) {
			AttributesDTO.AttributeValueDTO dto = AttributesDTO.AttributeValueDTO.builder()
					.attributeValueId(av.getAttributeValueId()).value(av.getValue()).build();
			attributeValueDTOs.add(dto);
		}

		return AttributesDTO.builder().attributeId(attribute.getAttributeId()).name(attribute.getName())
				.attributeValues(attributeValueDTOs).build();
	}

}
