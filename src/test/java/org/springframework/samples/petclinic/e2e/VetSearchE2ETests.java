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

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * End-to-end coverage for the Vets page name search (SCRUM-8), driving a real (headless)
 * browser so the debounced, backend-driven live search is exercised exactly as a clinic
 * staff member would use it.
 *
 * <p>
 * The seed data (see {@code db/h2/data.sql}) has six vets — James Carter, Helen Leary,
 * Linda Douglas, Rafael Ortega, Henry Stevens, Sharon Jenkins — and the list is paged
 * five per page, so the unfiltered list spans two pages (Sharon Jenkins lives alone on
 * page 2). That lets the "search across all pages" criterion be proven by finding a vet
 * that is not on the first page.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VetSearchE2ETests {

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

	private void openVetsPage() {
		page.navigate("http://localhost:" + port + "/vets.html");
	}

	private Locator vetRows() {
		return page.locator("#vets tbody tr");
	}

	private Locator searchInput() {
		return page.locator("#vet-search-input");
	}

	// Scenario 1 — Live partial match, backend-driven, searching across all pages.
	// Sharon Jenkins is on page 2 of the unfiltered list, so typing a partial term for
	// her name while page 1 is showing proves the search hits the backend across the
	// whole data set rather than filtering the current page in the browser.
	@Test
	void livePartialMatchSearchesAcrossAllPages() {
		openVetsPage();

		// Sharon is not visible in the initial (page 1) list.
		assertThat(vetRows()).hasCount(5);
		assertThat(page.locator("#vets")).not().containsText("Sharon Jenkins");

		searchInput().fill("sharo");

		// The debounced live search replaces the results with the single cross-page hit.
		assertThat(vetRows()).hasCount(1);
		assertThat(page.locator("#vets")).containsText("Sharon Jenkins");
	}

	// Scenario 2 — Case-insensitive: the same term in different casing yields the same
	// vet.
	@Test
	void searchIsCaseInsensitive() {
		openVetsPage();

		searchInput().fill("sharon");
		assertThat(vetRows()).hasCount(1);
		assertThat(page.locator("#vets")).containsText("Sharon Jenkins");

		searchInput().fill("SHARON");
		assertThat(vetRows()).hasCount(1);
		assertThat(page.locator("#vets")).containsText("Sharon Jenkins");
	}

	// Scenario 3 — No matches: an empty-state message is shown and the results table is
	// removed.
	@Test
	void noMatchesShowsEmptyState() {
		openVetsPage();

		searchInput().fill("zzz-no-such-vet");

		assertThat(page.locator("#vets-empty")).isVisible();
		assertThat(page.locator("#vets-empty")).hasText("No vets found");
		assertThat(page.locator("#vets")).hasCount(0);
	}

	// Scenario 4 — Empty search: the full paged list is displayed as before, including
	// the pagination controls for the second page.
	@Test
	void emptySearchShowsFullPagedList() {
		openVetsPage();

		// The search box starts empty and the clear (X) control is hidden.
		assertThat(searchInput()).hasValue("");
		assertThat(page.locator("#vet-search-clear")).hasClass(java.util.regex.Pattern.compile("\\bd-none\\b"));

		// First page shows five vets and pagination to the second page is present.
		assertThat(vetRows()).hasCount(5);
		assertThat(page.locator("a[href*='page=2']").first()).isVisible();
	}

	// Scenario 5 — Clear button: clicking the clear (X) control removes the term and
	// restores the full paged list.
	@Test
	void clearButtonRestoresFullList() {
		openVetsPage();

		searchInput().fill("sharon");
		assertThat(vetRows()).hasCount(1);

		// Typing a term reveals the clear control; clicking it resets the search.
		Locator clear = page.locator("#vet-search-clear");
		assertThat(clear).not().hasClass(java.util.regex.Pattern.compile("\\bd-none\\b"));
		clear.click();

		assertThat(searchInput()).hasValue("");
		assertThat(vetRows()).hasCount(5);
		assertThat(page.locator("#vets")).containsText("James Carter");
	}

}
