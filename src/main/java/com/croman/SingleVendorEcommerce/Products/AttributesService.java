package com.croman.SingleVendorEcommerce.Products;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.croman.SingleVendorEcommerce.General.LocaleUtils;
import com.croman.SingleVendorEcommerce.General.PaginationUtils;
import com.croman.SingleVendorEcommerce.Message.MessageService;
import com.croman.SingleVendorEcommerce.Products.DTO.AttributesDTO;
import com.croman.SingleVendorEcommerce.Products.Entity.Attribute;
import com.croman.SingleVendorEcommerce.Products.Entity.Category;
import com.croman.SingleVendorEcommerce.Products.Repository.AttributeRepository;
import com.croman.SingleVendorEcommerce.Translations.TranslationService;
import com.croman.SingleVendorEcommerce.Translations.DTO.TranslatorPropertyType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttributesService {

	private final AttributeRepository attributeRepository;
	private final TranslationService translationService;

	public List<AttributesDTO> getAttributes(String languageName, int page, int size) {

		Pageable pageable = PaginationUtils.getPageable(page, size, "attributeId");

		List<Attribute> allAttributes = attributeRepository.findAll(pageable).getContent();

		HashMap<Integer, String> batchTranslateHashMap = null;

		if (!languageName.equals(LocaleUtils.DATABASE_DEFAULT_LANG)) {
			List<Long> attributeIds = allAttributes.stream().map(Attribute::getAttributeId).distinct().toList();
			batchTranslateHashMap = translationService.batchTranslate(languageName, TranslatorPropertyType.ATTRIBUTE,
					attributeIds);
		}

		List<AttributesDTO> attributesDTOs = new ArrayList<>();

		for (Attribute attribute : allAttributes) {
			attributesDTOs.add(mapAttributeToAttributeDTO(attribute, batchTranslateHashMap));
		}

		return attributesDTOs;
	}

	private AttributesDTO mapAttributeToAttributeDTO(Attribute attribute,
			HashMap<Integer, String> batchTranslateHashMap) {
		Integer key = attribute.getAttributeId().intValue();
		String name = batchTranslateHashMap != null ? batchTranslateHashMap.get(key)
				: attribute.getAttributeType().toString();
		return AttributesDTO.builder().attributeId(attribute.getAttributeId()).name(name).build();
	}

}
