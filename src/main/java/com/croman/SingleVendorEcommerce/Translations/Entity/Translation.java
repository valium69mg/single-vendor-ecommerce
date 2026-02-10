package com.croman.SingleVendorEcommerce.Translations.Entity;

import com.croman.SingleVendorEcommerce.Translations.DTO.TranslatorPropertyType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "translations")
public class Translation {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "translation_id")
    private Long translationId;
	
	@Column(name = "register_id", nullable = false)
	private Integer registerId;
	
	@Column(name = "translation", length = 255, nullable = false)
	private String translation;
	
	@ManyToOne
	@JoinColumn(name = "language_id", nullable = false)
	private Language language;
	
	@Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 50)
    private TranslatorPropertyType translatorPropertyType;
}
