package org.springframework.samples.petclinic.system;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies that the home page hero banner background uses a lighter shade of green than
 * the Spring brand green (SCRUM-2). The check is performed against both the SCSS source
 * (so the intent is expressed in the source of truth) and the compiled CSS (so the change
 * actually reaches the browser).
 *
 * <p>
 * The hero background is a green gradient; each of its colours must be lighter than the
 * base brand green {@code #6db33f} / dark green {@code #5fa134}.
 */
class HeroBannerColorTest {

	private static final Path SCSS = Path.of("src/main/scss/petclinic.scss");

	private static final Path CSS = Path.of("src/main/resources/static/resources/css/petclinic.css");

	/** Brand greens the hero used to be painted with, before it was lightened. */
	private static final int BRAND_GREEN = rgb("6db33f");

	private static final int BRAND_DARK_GREEN = rgb("5fa134");

	private static final Pattern HERO_BACKGROUND = Pattern
		.compile("\\.home-hero\\s*\\{[^}]*?background:\\s*linear-gradient\\(([^)]*)\\)", Pattern.DOTALL);

	private static final Pattern HEX_COLOR = Pattern.compile("#([0-9a-fA-F]{6})");

	@Test
	void heroBannerUsesLighterGreenInCompiledCss() throws Exception {
		List<Integer> colours = heroGradientColours(CSS);
		assertThat(colours).as("hero gradient should declare two colours").hasSize(2);
		assertThat(lightness(colours.get(0))).as("hero start colour must be lighter than the brand green")
			.isGreaterThan(lightness(BRAND_GREEN));
		assertThat(lightness(colours.get(1))).as("hero end colour must be lighter than the brand dark green")
			.isGreaterThan(lightness(BRAND_DARK_GREEN));
		colours.forEach(HeroBannerColorTest::assertIsGreen);
	}

	@Test
	void heroBannerScssReferencesLightenedGreenVariables() throws Exception {
		String scss = Files.readString(SCSS);
		assertThat(scss).contains("$spring-light-green:")
			.contains("$spring-light-dark-green:")
			.containsPattern("\\.home-hero\\s*\\{[^}]*\\$spring-light-green[^}]*\\$spring-light-dark-green");
	}

	private static List<Integer> heroGradientColours(Path file) throws Exception {
		Matcher block = HERO_BACKGROUND.matcher(Files.readString(file));
		assertThat(block.find()).as("could not find .home-hero linear-gradient background in %s", file).isTrue();
		Matcher hex = HEX_COLOR.matcher(block.group(1));
		List<Integer> colours = new ArrayList<>();
		while (hex.find()) {
			colours.add(rgb(hex.group(1)));
		}
		return colours;
	}

	private static void assertIsGreen(int colour) {
		int r = (colour >> 16) & 0xff, g = (colour >> 8) & 0xff, b = colour & 0xff;
		assertThat(g).as("hero colour #%06x should still be predominantly green", colour)
			.isGreaterThan(r)
			.isGreaterThan(b);
	}

	private static int rgb(String hex) {
		return Integer.parseInt(hex, 16);
	}

	/** Perceived lightness (0-255) of a packed RGB colour. */
	private static double lightness(int colour) {
		int r = (colour >> 16) & 0xff, g = (colour >> 8) & 0xff, b = colour & 0xff;
		return 0.299 * r + 0.587 * g + 0.114 * b;
	}

}
