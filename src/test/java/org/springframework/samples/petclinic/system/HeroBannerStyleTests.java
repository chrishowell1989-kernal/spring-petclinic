/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic.system;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Guards the compiled hero-banner styles (SCRUM-2): the home page hero banner must render
 * with a <em>lighter</em> tint of the brand green while keeping a dark, high contrast
 * text colour. The source of truth is {@code src/main/scss/petclinic.scss}; this test
 * asserts on the committed compiled {@code petclinic.css} that is actually served to
 * browsers, so it also catches an SCSS edit that was never recompiled.
 */
class HeroBannerStyleTests {

	/**
	 * Lighter tints of {@code $spring-green} / {@code $spring-dark-green} that the hero
	 * gradient uses after SCRUM-2 (same hue/saturation, higher lightness).
	 */
	private static final String HERO_LIGHT_GREEN = "#badea2";

	private static final String HERO_LIGHT_DARK_GREEN = "#aad98a";

	/** Original full-strength brand greens that must no longer appear in the hero. */
	private static final String BRAND_GREEN = "#6db33f";

	private static final String BRAND_DARK_GREEN = "#5fa134";

	/**
	 * Dark brand brown used for hero text to keep WCAG AA contrast on the light green.
	 */
	private static final String HERO_TEXT_BROWN = "#34302d";

	private static String heroBlock;

	@BeforeAll
	static void loadHeroBlock() throws IOException {
		String css;
		try (var in = new ClassPathResource("static/resources/css/petclinic.css").getInputStream()) {
			css = StreamUtils.copyToString(in, StandardCharsets.UTF_8).toLowerCase();
		}
		heroBlock = cssBlock(css, ".home-hero {");
	}

	@Test
	void heroBackgroundUsesLighterTintOfBrandGreen() {
		assertThat(heroBlock).contains(HERO_LIGHT_GREEN).contains(HERO_LIGHT_DARK_GREEN);
		assertThat(heroBlock).doesNotContain(BRAND_GREEN).doesNotContain(BRAND_DARK_GREEN);
	}

	@Test
	void heroTextUsesDarkColourForContrast() {
		assertThat(heroBlock).contains("color: " + HERO_TEXT_BROWN);
	}

	/**
	 * Returns the declaration block ({@code { ... }}) that follows the given selector.
	 */
	private static String cssBlock(String css, String selector) {
		int start = css.indexOf(selector);
		assertThat(start).as("selector '%s' present in compiled CSS", selector).isGreaterThanOrEqualTo(0);
		int end = css.indexOf('}', start);
		assertThat(end).as("closing brace for '%s'", selector).isGreaterThan(start);
		return css.substring(start, end);
	}

}
