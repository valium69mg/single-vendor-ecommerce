package com.croman.SingleVendorEcommerce.Products;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.croman.SingleVendorEcommerce.General.LocaleUtils;
import com.croman.SingleVendorEcommerce.General.PaginationUtils;
import com.croman.SingleVendorEcommerce.Message.MessageService;
import com.croman.SingleVendorEcommerce.Products.DTO.AttributeType;
import com.croman.SingleVendorEcommerce.Products.DTO.AttributesDTO;
import com.croman.SingleVendorEcommerce.Products.Entity.Attribute;
import com.croman.SingleVendorEcommerce.Products.Entity.AttributeValue;
import com.croman.SingleVendorEcommerce.Products.Entity.Category;
import com.croman.SingleVendorEcommerce.Products.Repository.AttributeRepository;
import com.croman.SingleVendorEcommerce.Products.Repository.AttributeValueRepository;
import com.croman.SingleVendorEcommerce.Translations.TranslationService;
import com.croman.SingleVendorEcommerce.Translations.DTO.TranslatorPropertyType;

import io.jsonwebtoken.lang.Collections;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttributesService {

	private final AttributeRepository attributeRepository;
	private final AttributeValueRepository attributeValueRepository;
	private final TranslationService translationService;

	public List<AttributesDTO> getAttributes(String languageName, int page, int size) {

		Pageable pageable = PaginationUtils.getPageable(page, size, "attributeId");

		List<Attribute> attributes = attributeRepository.findAll(pageable).getContent();

		List<Long> attributeIds = attributes.stream().map(Attribute::getAttributeId).toList();

		List<AttributeValue> allAttributeValues = attributeValueRepository.findByAttributeIdIn(attributeIds);

		Map<Attribute, Set<AttributeValue>> valuesByAttribute = new LinkedHashMap<>();
		for (Attribute attribute : attributes) {
			valuesByAttribute.put(attribute, new LinkedHashSet<>());
		}
		for (AttributeValue av : allAttributeValues) {
			valuesByAttribute.get(av.getAttribute()).add(av);
		}

		HashMap<Integer, String> batchAttributeTranslateHashMap = null;
		if (!languageName.equals(LocaleUtils.DATABASE_DEFAULT_LANG)) {
			batchAttributeTranslateHashMap = translationService.batchTranslate(languageName,
					TranslatorPropertyType.ATTRIBUTE, attributeIds);
		}

		HashMap<Integer, String> batchAttributeValueTranslateHashMap = null;
		if (!languageName.equals(LocaleUtils.DATABASE_DEFAULT_LANG)) {
			List<Long> colorAttributeValueIds = allAttributeValues.stream()
					.filter(av -> av.getAttribute().getAttributeType() == AttributeType.COLOR)
					.map(AttributeValue::getAttributeValueId).toList();

			if (!colorAttributeValueIds.isEmpty()) {
				batchAttributeValueTranslateHashMap = translationService.batchTranslate(languageName,
						TranslatorPropertyType.COLOR, colorAttributeValueIds);
			}
		}

		List<AttributesDTO> attributesDTOs = new ArrayList<>();
		for (Map.Entry<Attribute, Set<AttributeValue>> entry : valuesByAttribute.entrySet()) {
			attributesDTOs.add(mapAttributeToAttributeDTO(entry.getKey(), entry.getValue(),
					batchAttributeTranslateHashMap, batchAttributeValueTranslateHashMap));
		}

		return attributesDTOs;
	}

	private AttributesDTO mapAttributeToAttributeDTO(Attribute attribute, Set<AttributeValue> values,
			HashMap<Integer, String> batchTranslateHashMap,
			HashMap<Integer, String> batchAttributeValueTranslateHashMap) {

		Integer key = attribute.getAttributeId().intValue();
		String name = batchTranslateHashMap != null ? batchTranslateHashMap.get(key)
				: attribute.getAttributeType().toString();

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
