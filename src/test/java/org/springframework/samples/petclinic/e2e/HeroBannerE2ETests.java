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
package org.springframework.samples.petclinic.e2e;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end coverage for SCRUM-2: the home page hero banner background is changed from
 * the full-strength brand green to a <em>lighter tint</em> of the same green, with dark
 * text that keeps WCAG AA contrast, and the change is scoped to the hero only.
 * <p>
 * Each test maps to an acceptance-criteria item from the Jira ticket and asserts against
 * the <em>rendered / computed</em> style in a real (headless) browser, not the source
 * files. Criteria that cannot be verified with this single-Chromium harness
 * (cross-browser Firefox/Safari, and pixel-diff against an approved design mock that is
 * not committed to the repo) are documented in the test that gets closest, and called out
 * in the QA notes.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HeroBannerE2ETests {

	/** Lighter tints of the brand green the hero gradient uses after SCRUM-2. */
	private static final int[] HERO_LIGHT_GREEN = { 186, 222, 162 }; // #badea2

	private static final int[] HERO_LIGHT_DARK_GREEN = { 170, 217, 138 }; // #aad98a

	/** Original full-strength brand greens that must no longer appear in the hero. */
	private static final int[] BRAND_GREEN = { 109, 179, 63 }; // #6db33f

	private static final int[] BRAND_DARK_GREEN = { 95, 161, 52 }; // #5fa134

	/** Dark brand brown used for the hero text to keep contrast on the light green. */
	private static final int[] HERO_TEXT_BROWN = { 52, 48, 45 }; // #34302d

	@LocalServerPort
	int port;

	private static Playwright playwright;

	private static Browser browser;

	private Page page;

	@BeforeAll
	static void launchBrowser() {
		playwright = Playwright.create();
		browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
	}

	@AfterAll
	static void closeBrowser() {
		browser.close();
		playwright.close();
	}

	@BeforeEach
	void newPage() {
		page = browser.newPage();
	}

	@AfterEach
	void closePage() {
		page.close();
	}

	private String homeUrl() {
		return "http://localhost:" + port + "/";
	}

	private String heroBackgroundImage() {
		return (String) page.evalOnSelector(".home-hero", "el => getComputedStyle(el).backgroundImage");
	}

	// AC #1: hero background is a lighter tint of the existing brand green — same
	// hue/saturation family, higher lightness.
	@Test
	void heroBackgroundIsLighterTintOfBrandGreen() {
		page.navigate(homeUrl());
		String bg = heroBackgroundImage();

		// The rendered gradient uses the light tints and no longer the brand greens.
		assertThat(bg).contains(rgb(HERO_LIGHT_GREEN)).contains(rgb(HERO_LIGHT_DARK_GREEN));
		assertThat(bg).doesNotContain(rgb(BRAND_GREEN)).doesNotContain(rgb(BRAND_DARK_GREEN));

		// ...and each tint really is the same hue/saturation with higher lightness.
		assertSameFamilyButLighter(HERO_LIGHT_GREEN, BRAND_GREEN);
		assertSameFamilyButLighter(HERO_LIGHT_DARK_GREEN, BRAND_DARK_GREEN);
	}

	// AC #2: the hex value documented in the PR/ticket matches what is actually rendered.
	@Test
	void renderedHeroBackgroundMatchesDocumentedHex() {
		page.navigate(homeUrl());
		String bg = heroBackgroundImage();

		assertThat(hexOf(HERO_LIGHT_GREEN)).isEqualTo("#badea2");
		assertThat(hexOf(HERO_LIGHT_DARK_GREEN)).isEqualTo("#aad98a");
		// Both documented hexes are present in the live computed gradient.
		assertThat(bg).contains(rgb(HERO_LIGHT_GREEN)).contains(rgb(HERO_LIGHT_DARK_GREEN));
	}

	// AC #3: hero text keeps a WCAG AA (>= 4.5:1) contrast ratio against the new
	// background, checked against the darkest (worst-case) gradient stop.
	@Test
	void heroTextMeetsWcagAaContrast() {
		page.navigate(homeUrl());

		int[] textColor = parseRgb((String) page.evalOnSelector(".home-hero h1", "el => getComputedStyle(el).color"));
		assertThat(textColor).containsExactly(HERO_TEXT_BROWN);

		// Worst case is the more-saturated / darker gradient stop.
		double contrastTop = contrastRatio(textColor, HERO_LIGHT_GREEN);
		double contrastBottom = contrastRatio(textColor, HERO_LIGHT_DARK_GREEN);
		assertThat(Math.min(contrastTop, contrastBottom)).as("hero text contrast against the lighter green background")
			.isGreaterThanOrEqualTo(4.5);
	}

	// AC #4: the change is scoped to the hero banner only — the navbar keeps its own
	// (brown) background and still uses the *original* brand green on its top border, so
	// nothing outside the hero shifted colour as a side effect.
	@Test
	void colorChangeIsScopedToHeroOnly() {
		page.navigate(homeUrl());

		int[] navBg = parseRgb(
				(String) page.evalOnSelector("nav.navbar", "el => getComputedStyle(el).backgroundColor"));
		int[] navBorder = parseRgb(
				(String) page.evalOnSelector("nav.navbar", "el => getComputedStyle(el).borderTopColor"));

		// Navbar did NOT pick up the hero light green...
		assertThat(navBg).isNotEqualTo(HERO_LIGHT_GREEN).isNotEqualTo(HERO_LIGHT_DARK_GREEN);
		// ...it keeps its brand-brown background, and the original brand green still
		// lives on its top border untouched.
		assertThat(navBg).containsExactly(HERO_TEXT_BROWN);
		assertThat(navBorder).containsExactly(BRAND_GREEN);
	}

	// AC #5: the colour renders consistently across desktop, tablet and mobile
	// breakpoints.
	@Test
	void heroColorIsConsistentAcrossBreakpoints() {
		int[][] viewports = { { 1280, 800 }, // desktop
				{ 768, 1024 }, // tablet
				{ 375, 667 } }; // mobile

		for (int[] viewport : viewports) {
			page.setViewportSize(viewport[0], viewport[1]);
			page.navigate(homeUrl());
			String bg = heroBackgroundImage();
			assertThat(bg).as("hero background at %dx%d", viewport[0], viewport[1])
				.contains(rgb(HERO_LIGHT_GREEN))
				.contains(rgb(HERO_LIGHT_DARK_GREEN));
		}
	}

	// AC #7: no new console errors or uncaught JS errors are introduced.
	@Test
	void homePageHasNoConsoleOrJsErrors() {
		List<String> errors = new ArrayList<>();
		page.onConsoleMessage(msg -> {
			if ("error".equals(msg.type())) {
				errors.add("console: " + msg.text());
			}
		});
		page.onPageError(err -> errors.add("pageerror: " + err));

		page.navigate(homeUrl());
		page.waitForLoadState();

		assertThat(errors).as("console / JS errors on the home page").isEmpty();
	}

	// --- helpers ---------------------------------------------------------------

	private static String rgb(int[] c) {
		return "rgb(" + c[0] + ", " + c[1] + ", " + c[2] + ")";
	}

	private static String hexOf(int[] c) {
		return String.format("#%02x%02x%02x", c[0], c[1], c[2]);
	}

	private static final Pattern RGB = Pattern.compile("rgba?\\((\\d+),\\s*(\\d+),\\s*(\\d+)");

	private static int[] parseRgb(String cssColor) {
		Matcher m = RGB.matcher(cssColor);
		assertThat(m.find()).as("parse rgb from '%s'", cssColor).isTrue();
		return new int[] { Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)), Integer.parseInt(m.group(3)) };
	}

	/**
	 * Asserts {@code tint} shares the hue and saturation of {@code base} (same brand
	 * colour family) but has a strictly higher lightness (a lighter shade).
	 */
	private static void assertSameFamilyButLighter(int[] tint, int[] base) {
		float[] tintHsl = toHsl(tint);
		float[] baseHsl = toHsl(base);
		assertThat(tintHsl[0]).as("hue of %s vs %s", hexOf(tint), hexOf(base)).isCloseTo(baseHsl[0], within(2.0f));
		assertThat(tintHsl[1]).as("saturation of %s vs %s", hexOf(tint), hexOf(base))
			.isCloseTo(baseHsl[1], within(2.0f));
		assertThat(tintHsl[2]).as("lightness of %s vs %s", hexOf(tint), hexOf(base)).isGreaterThan(baseHsl[2]);
	}

	private static org.assertj.core.data.Offset<Float> within(float v) {
		return org.assertj.core.data.Offset.offset(v);
	}

	/** RGB (0-255) to HSL, hue in degrees, saturation/lightness in percent. */
	private static float[] toHsl(int[] c) {
		float r = c[0] / 255f, g = c[1] / 255f, b = c[2] / 255f;
		float max = Math.max(r, Math.max(g, b));
		float min = Math.min(r, Math.min(g, b));
		float l = (max + min) / 2f;
		float h = 0f, s = 0f;
		float d = max - min;
		if (d != 0f) {
			s = l > 0.5f ? d / (2f - max - min) : d / (max + min);
			if (max == r) {
				h = (g - b) / d + (g < b ? 6f : 0f);
			}
			else if (max == g) {
				h = (b - r) / d + 2f;
			}
			else {
				h = (r - g) / d + 4f;
			}
			h /= 6f;
		}
		return new float[] { h * 360f, s * 100f, l * 100f };
	}

	/** WCAG 2.x contrast ratio between two sRGB colours. */
	private static double contrastRatio(int[] fg, int[] bg) {
		double l1 = relativeLuminance(fg);
		double l2 = relativeLuminance(bg);
		double lighter = Math.max(l1, l2);
		double darker = Math.min(l1, l2);
		return (lighter + 0.05) / (darker + 0.05);
	}

	private static double relativeLuminance(int[] c) {
		double[] lin = new double[3];
		for (int i = 0; i < 3; i++) {
			double channel = c[i] / 255.0;
			lin[i] = channel <= 0.03928 ? channel / 12.92 : Math.pow((channel + 0.055) / 1.055, 2.4);
		}
		return 0.2126 * lin[0] + 0.7152 * lin[1] + 0.0722 * lin[2];
	}

}
