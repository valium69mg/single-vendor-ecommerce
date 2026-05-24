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
import com.croman.singlevendorecommerce.dto.products.AttributeType;
import com.croman.singlevendorecommerce.dto.products.AttributesDTO;
import com.croman.singlevendorecommerce.dto.products.CreateAttributeDTO;
import com.croman.singlevendorecommerce.dto.products.UpdateAttributeDTO;
import com.croman.singlevendorecommerce.dto.translations.TranslatorPropertyType;
import com.croman.singlevendorecommerce.entity.products.Attribute;
import com.croman.singlevendorecommerce.entity.products.AttributeValue;
import com.croman.singlevendorecommerce.repository.products.AttributeRepository;
import com.croman.singlevendorecommerce.repository.products.AttributeValueRepository;
import com.croman.singlevendorecommerce.service.message.MessageService;
import com.croman.singlevendorecommerce.service.translations.TranslationService;
import com.croman.singlevendorecommerce.utils.LocaleUtils;
import com.croman.singlevendorecommerce.utils.PaginationUtils;
import com.croman.singlevendorecommerce.utils.exceptions.ApiServiceException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttributesService {

	private final AttributeRepository attributeRepository;
	private final AttributeValueRepository attributeValueRepository;
	private final TranslationService translationService;
	private final MessageService messageService;

	private static final String ATTRIBUTE_NOT_FOUND = "attribute_not_found";
	private static final String ATTRIBUTE_ALREADY_EXISTS = "attribute_already_exists";

	@Transactional(readOnly = true)
	public List<AttributesDTO> getAttributes(String languageName, int page, int size) {

		Pageable pageable = PaginationUtils.getPageable(page, size, "attributeId");

		List<Attribute> attributes = attributeRepository.findAll(pageable).getContent();

		List<Long> attributeIds = attributes.stream().map(Attribute::getAttributeId).toList();

		List<AttributeValue> allAttributeValues = attributeValueRepository.findByAttributeIdIn(attributeIds);

		Map<Attribute, Set<AttributeValue>> valuesByAttribute = getValuesByAttribute(attributes, allAttributeValues);

		Map<Integer, String> batchAttributeTranslateHashMap = getAttributeBatchTranslateHashMap(languageName,
				attributeIds);

		Map<Integer, String> batchAttributeValueTranslateHashMap = getAttributeValuesBatchTranslateHashMap(
				languageName, allAttributeValues);

		List<AttributesDTO> attributesDTOs = new ArrayList<>();

		for (Map.Entry<Attribute, Set<AttributeValue>> entry : valuesByAttribute.entrySet()) {
			attributesDTOs.add(mapAttributeToAttributeDTO(entry.getKey(), entry.getValue(),
					batchAttributeTranslateHashMap, batchAttributeValueTranslateHashMap));
		}

		return attributesDTOs;
	}

	@Transactional(readOnly = true)
	public AttributeByIdDTO getAttributeById(Long attributeId) {
		Attribute attribute = attributeRepository.findById(attributeId)
				.orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND.value(),
						messageService.getMessage(ATTRIBUTE_NOT_FOUND, LocaleUtils.getDefaultLocale())));

		List<AttributeValue> values = attributeValueRepository.findByAttributeIdIn(List.of(attributeId));

		Map<Integer, String> attributeTranslation = translationService.batchTranslate(LocaleUtils.ES,
				TranslatorPropertyType.ATTRIBUTE, List.of(attributeId));

		String spanishName = attributeTranslation.get(attributeId.intValue());

		List<AttributeByIdDTO.ValueDTO> valueDTOs = values.stream()
				.map(av -> AttributeByIdDTO.ValueDTO.builder()
						.attributeValueId(av.getAttributeValueId())
						.value(av.getValue())
						.build())
				.toList();

		return AttributeByIdDTO.builder()
				.attributeId(attribute.getAttributeId())
				.attributeType(attribute.getAttributeType())
				.spanishName(spanishName)
				.attributeValues(valueDTOs)
				.build();
	}

	@Transactional
	public void createAttribute(CreateAttributeDTO createAttributeDTO) {
		String attributeType = createAttributeDTO.getAttributeType().toUpperCase();
		String spanishName = createAttributeDTO.getSpanishName();

		Optional<Attribute> existing = attributeRepository.findByAttributeType(attributeType);

		if (existing.isPresent()) {
			throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
					messageService.getMessage(ATTRIBUTE_ALREADY_EXISTS, LocaleUtils.getDefaultLocale()));
		}

		Attribute attribute = Attribute.builder().attributeType(attributeType).build();
		attribute = attributeRepository.save(attribute);

		translationService.createTranslation(attribute.getAttributeId().intValue(), LocaleUtils.ES,
				TranslatorPropertyType.ATTRIBUTE, spanishName);
	}

	@Transactional
	public void updateAttribute(Long attributeId, UpdateAttributeDTO updateAttributeDTO) {
		String spanishName = updateAttributeDTO.getSpanishName();

		if (spanishName == null) {
			throw new ApiServiceException(HttpStatus.BAD_REQUEST.value(),
					messageService.getMessage("missing_language_names", LocaleUtils.getDefaultLocale()));
		}

		boolean exists = attributeRepository.existsById(attributeId);
		if (!exists) {
			throw new ApiServiceException(HttpStatus.NOT_FOUND.value(),
					messageService.getMessage(ATTRIBUTE_NOT_FOUND, LocaleUtils.getDefaultLocale()));
		}

		translationService.updateTranslation(attributeId.intValue(), LocaleUtils.ES,
				TranslatorPropertyType.ATTRIBUTE, spanishName);
	}

	@Transactional
	public void deleteAttribute(Long attributeId) {
		boolean exists = attributeRepository.existsById(attributeId);

		if (!exists) {
			throw new ApiServiceException(HttpStatus.NOT_FOUND.value(),
					messageService.getMessage(ATTRIBUTE_NOT_FOUND, LocaleUtils.getDefaultLocale()));
		}

		translationService.deleteTranslation(attributeId.intValue(), LocaleUtils.ES,
				TranslatorPropertyType.ATTRIBUTE);

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

	private Map<Integer, String> getAttributeBatchTranslateHashMap(String languageName, List<Long> attributeIds) {
		Map<Integer, String> batchAttributeTranslateHashMap = null;
		if (!languageName.equals(LocaleUtils.DATABASE_DEFAULT_LANG)) {
			batchAttributeTranslateHashMap = translationService.batchTranslate(languageName,
					TranslatorPropertyType.ATTRIBUTE, attributeIds);
		}
		return batchAttributeTranslateHashMap;
	}

	private Map<Integer, String> getAttributeValuesBatchTranslateHashMap(String languageName,
			List<AttributeValue> allAttributeValues) {
		Map<Integer, String> batchAttributeValueTranslateHashMap = null;
		if (!languageName.equals(LocaleUtils.DATABASE_DEFAULT_LANG)) {
			List<Long> colorAttributeValueIds = allAttributeValues.stream()
					.filter(av -> AttributeType.COLOR.name().equals(av.getAttribute().getAttributeType()))
					.map(AttributeValue::getAttributeValueId).toList();

			if (!colorAttributeValueIds.isEmpty()) {
				batchAttributeValueTranslateHashMap = translationService.batchTranslate(languageName,
						TranslatorPropertyType.COLOR, colorAttributeValueIds);
			}
		}
		return batchAttributeValueTranslateHashMap;
	}

	private AttributesDTO mapAttributeToAttributeDTO(Attribute attribute, Set<AttributeValue> values,
			Map<Integer, String> batchTranslateHashMap,
			Map<Integer, String> batchAttributeValueTranslateHashMap) {

		Integer key = attribute.getAttributeId().intValue();
		String name = batchTranslateHashMap != null ? batchTranslateHashMap.get(key)
				: attribute.getAttributeType();

		List<AttributesDTO.AttributeValueDTO> attributeValueDTOs = new ArrayList<>();
		for (AttributeValue av : values) {
			Integer valueKey = av.getAttributeValueId().intValue();
			String value = batchAttributeValueTranslateHashMap != null
					? batchAttributeValueTranslateHashMap.getOrDefault(valueKey, av.getValue())
					: av.getValue();

			AttributesDTO.AttributeValueDTO dto = AttributesDTO.AttributeValueDTO.builder()
					.attributeValueId(av.getAttributeValueId()).value(value).build();
			attributeValueDTOs.add(dto);
		}

		return AttributesDTO.builder().attributeId(attribute.getAttributeId()).name(name)
				.attributeValues(attributeValueDTOs).build();
	}

}
