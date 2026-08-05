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
 * End-to-end coverage for the vet-list search bar (SCRUM-7), driving a real (headless)
 * browser against the running app.
 * <p>
 * Exercises the acceptance criteria: server-side filtering after the 300ms debounce
 * (case-insensitive, partial match), an empty state for no matches, and clearing the
 * search restoring the full paginated list at page 1.
 * <p>
 * The seed data (see {@code db/h2/data.sql}) contains six vets — James Carter, Helen
 * Leary, Linda Douglas, Rafael Ortega, Henry Stevens, Sharon Jenkins — so the unfiltered
 * list spans two pages at the fixed page size of five. No single name substring matches
 * more than five of those six, so a <em>filtered</em> result that itself spans more than
 * one page cannot be produced through the UI with the seed data; the "search results are
 * paginated" scenario is therefore asserted at the query level in
 * {@code ClinicServiceTests.shouldPaginateVetNameSearchResults}. Here we verify that the
 * default list uses the pagination pattern that search reuses, and that an active search
 * term is preserved in the search box.
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

	private String vetsUrl() {
		return "http://localhost:" + port + "/vets.html";
	}

	// Scenario: Searching filters results server-side.
	@Test
	void searchFiltersResultsServerSide() {
		page.navigate(vetsUrl());
		// Sanity: the unfiltered list shows the first page of the six seeded vets.
		assertThat(page.locator("#vets tbody tr").count()).isEqualTo(5);

		page.locator("#vetSearch").fill("lea");
		// A server-side GET carrying the search term completes (the debounced form
		// submit).
		page.waitForURL(url -> url.contains("name=lea"));

		assertThat(page.locator("#vets tbody tr").count()).isEqualTo(1);
		assertThat(page.locator("#vets tbody tr").first().textContent()).contains("Helen Leary");
	}

	// Scenario: Searching filters results server-side — case-insensitive, partial match.
	@Test
	void searchIsCaseInsensitiveAndPartial() {
		page.navigate(vetsUrl());

		// Upper-case, partial, and matching across the full "first last" name.
		page.locator("#vetSearch").fill("HELEN LEA");
		page.waitForURL(url -> url.contains("name=HELEN"));

		assertThat(page.locator("#vets tbody tr").count()).isEqualTo(1);
		assertThat(page.locator("#vets tbody tr").first().textContent()).contains("Helen Leary");
	}

	// Scenario: Searching filters results server-side — the request is debounced (300ms).
	@Test
	void searchIsDebouncedBeforeFiringRequest() {
		page.navigate(vetsUrl());
		String before = page.url();

		page.locator("#vetSearch").fill("lea");
		// Immediately after typing, the 300ms debounce means no navigation has happened
		// yet.
		assertThat(page.url()).isEqualTo(before);
		assertThat(page.url()).doesNotContain("name=lea");

		// After the debounce elapses the server-side request fires and the URL updates.
		page.waitForURL(url -> url.contains("name=lea"));
		assertThat(page.locator("#vets tbody tr").count()).isEqualTo(1);
	}

	// Scenario: No matching results.
	@Test
	void searchWithNoMatchesShowsEmptyState() {
		page.navigate(vetsUrl());

		page.locator("#vetSearch").fill("zzznomatch");
		page.waitForURL(url -> url.contains("name=zzznomatch"));

		assertThat(page.locator("#vetsEmpty").isVisible()).isTrue();
		assertThat(page.locator("#vets").count()).isZero();
	}

	// Scenario: Clearing the search.
	@Test
	void clearingSearchRestoresFullPaginatedListAtPageOne() {
		// Start from an active search that filters the list down to a single vet.
		page.navigate(vetsUrl() + "?name=lea");
		assertThat(page.locator("#vets tbody tr").count()).isEqualTo(1);

		// Clear the search box; the debounced submit restores the default list.
		page.locator("#vetSearch").fill("");
		page.waitForURL(url -> url.endsWith("/vets.html?name=") || url.endsWith("/vets.html"));

		// Full list, page 1: five of the six seeded vets, with pagination controls
		// present.
		assertThat(page.locator("#vets tbody tr").count()).isEqualTo(5);
		assertThat(page.locator("a[href*='page=2']").count()).isGreaterThan(0);
		assertThat(page.locator("#vetSearch").inputValue()).isEmpty();
	}

	// Scenario: Search results are paginated (same pattern as the default list).
	//
	// The seed data caps any name search at five matches (one page), so a multi-page
	// filtered result cannot be produced here; that query-level behaviour is covered by
	// ClinicServiceTests.shouldPaginateVetNameSearchResults. This test verifies the
	// pagination pattern that search reuses is present and functional on the default
	// list,
	// and that an active search term is preserved in the search box across the page.
	@Test
	void defaultListIsPaginatedAndSearchTermIsPreserved() {
		page.navigate(vetsUrl());

		// Six seeded vets at a page size of five => a second page exists and is
		// reachable.
		assertThat(page.locator("#vets tbody tr").count()).isEqualTo(5);
		page.locator("a[href*='page=2']").first().click();
		page.waitForURL(url -> url.contains("page=2"));
		assertThat(page.locator("#vets tbody tr").count()).isEqualTo(1);
		assertThat(page.locator("#vets tbody tr").first().textContent()).contains("Sharon Jenkins");

		// An active search term stays in the box so it survives paging/reloads.
		page.navigate(vetsUrl() + "?name=lea");
		assertThat(page.locator("#vetSearch").inputValue()).isEqualTo("lea");
	}

}
