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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the home page hero banner (SCRUM-2) uses a lighter shade of the brand green
 * and keeps its inner text/icons/buttons at a WCAG AA (>= 4.5:1) contrast ratio against
 * the new background.
 * <p>
 * The test asserts against the compiled {@code petclinic.css} that is actually served to
 * the browser (compiled from {@code src/main/scss/petclinic.scss} via the {@code css}
 * Maven profile), so it guards against the source and the rendered stylesheet drifting
 * apart.
 */
class HeroBannerColorTest {

	private static final Path CSS = Path.of("src/main/resources/static/resources/css/petclinic.css");

	/**
	 * Old (darker) hero colours that must no longer be used for the banner background.
	 */
	private static final String OLD_GREEN = "#6db33f";

	private static final String OLD_DARK_GREEN = "#5fa134";

	/**
	 * New lighter tint documented in SCRUM-2 (same hue/saturation family, higher
	 * lightness).
	 */
	private static final String NEW_LIGHT_GREEN = "#b9dea1";

	/** Foreground colour used for hero text/icons. */
	private static final String HERO_TEXT = "#34302d";

	private String heroRule() throws Exception {
		String css = Files.readString(CSS).toLowerCase();
		// Match the ".home-hero { ... }" declaration block (not ".home-hero h1" etc.).
		Matcher m = Pattern.compile("\\.home-hero\\s*\\{([^}]*)}").matcher(css);
		assertThat(m.find()).as("`.home-hero` rule present in compiled petclinic.css").isTrue();
		return m.group(1);
	}

	@Test
	void heroBackgroundUsesLighterGreenTint() throws Exception {
		String rule = heroRule();
		assertThat(rule).as("hero background is set to the documented lighter green tint").contains(NEW_LIGHT_GREEN);
		assertThat(rule).as("old darker green is no longer used for the hero background")
			.doesNotContain(OLD_GREEN)
			.doesNotContain(OLD_DARK_GREEN);
	}

	@Test
	void newTintIsLighterThanTheOldGreen() {
		assertThat(relativeLuminance(NEW_LIGHT_GREEN))
			.as("new tint has a higher lightness (relative luminance) than the original green")
			.isGreaterThan(relativeLuminance(OLD_GREEN));
	}

	@Test
	void heroTextMeetsWcagAaAgainstEveryBackgroundStop() throws Exception {
		String rule = heroRule();
		assertThat(rule).as("hero text colour set to the dark brand brown").contains("color: " + HERO_TEXT);

		// Every colour that forms the hero background gradient must clear WCAG AA (4.5:1)
		// against the hero text colour.
		Matcher colours = Pattern.compile("#[0-9a-f]{6}").matcher(rule);
		int backgroundStops = 0;
		while (colours.find()) {
			String stop = colours.group();
			if (stop.equals(HERO_TEXT)) {
				continue;
			}
			backgroundStops++;
			double ratio = contrastRatio(HERO_TEXT, stop);
			assertThat(ratio).as("contrast of hero text %s on background %s", HERO_TEXT, stop)
				.isGreaterThanOrEqualTo(4.5);
		}
		assertThat(backgroundStops).as("hero background gradient exposes at least one colour stop").isGreaterThan(0);
	}

	// --- WCAG relative-luminance / contrast helpers (per WCAG 2.1 definitions) ---

	private static double contrastRatio(String hexA, String hexB) {
		double la = relativeLuminance(hexA);
		double lb = relativeLuminance(hexB);
		double lighter = Math.max(la, lb);
		double darker = Math.min(la, lb);
		return (lighter + 0.05) / (darker + 0.05);
	}

	private static double relativeLuminance(String hex) {
		String h = hex.startsWith("#") ? hex.substring(1) : hex;
		double r = channel(Integer.parseInt(h.substring(0, 2), 16));
		double g = channel(Integer.parseInt(h.substring(2, 4), 16));
		double b = channel(Integer.parseInt(h.substring(4, 6), 16));
		return 0.2126 * r + 0.7152 * g + 0.0722 * b;
	}

	private static double channel(int value) {
		double c = value / 255.0;
		return (c <= 0.03928) ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
	}

}
