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
 * End-to-end coverage for the live, client-side name search on the Find Owners page
 * (SCRUM-3). Each test maps to one acceptance criterion from the ticket.
 *
 * The seed data (src/main/resources/db/h2/data.sql) contains 10 owners, two of whom
 * ("Betty Davis" and "Harold Davis") share the last name "Davis" — used here to prove
 * partial/substring, case-insensitive matching.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FindOwnerSearchE2ETests {

	/** Total number of owners in the H2 seed data. */
	private static final int TOTAL_OWNERS = 10;

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
		page.navigate("http://localhost:" + port + "/owners/find");
	}

	@AfterEach
	void closePage() {
		page.close();
	}

	private Locator visibleRows() {
		return page.locator("#owners-list-body tr:visible");
	}

	private Locator search() {
		return page.locator("#owner-name-search");
	}

	private Locator noResults() {
		return page.locator("#owners-no-results");
	}

	// Given the search bar is empty, When the page loads, Then the full list of owners
	// is shown (unchanged default state).
	@Test
	void fullListShownWhenSearchIsEmptyOnLoad() {
		assertThat(search()).hasValue("");
		assertThat(visibleRows()).hasCount(TOTAL_OWNERS);
		assertThat(noResults()).isHidden();
	}

	// Given I am on the Find Owner page, When I start typing in the search bar, Then the
	// owner list filters live to show only names matching my input. Also proves the
	// ticket's assumption: case-insensitive, partial/substring match.
	@Test
	void typingFiltersListLiveWithCaseInsensitiveSubstringMatch() {
		// "davis" is a lowercase substring shared only by "Betty Davis" and "Harold
		// Davis" (not "David Schroeder"), proving case-insensitive partial matching
		search().fill("davis");

		assertThat(visibleRows()).hasCount(2);
		assertThat(page.locator("#owners-list-body tr:visible"))
			.containsText(new String[] { "Betty Davis", "Harold Davis" });
		assertThat(noResults()).isHidden();

		// Narrow to a single owner to confirm live re-filtering as input changes.
		search().fill("franklin");
		assertThat(visibleRows()).hasCount(1);
		assertThat(visibleRows()).containsText("George Franklin");
		assertThat(noResults()).isHidden();
	}

	// Given I type a name with no matches, When the list filters, Then a "no results
	// found" message is shown instead of an empty list.
	@Test
	void noResultsMessageShownWhenNothingMatches() {
		search().fill("zzzzzz");

		assertThat(visibleRows()).hasCount(0);
		assertThat(noResults()).isVisible();
	}

	// Given I clear the search input, When the field becomes empty, Then the full owner
	// list reappears.
	@Test
	void clearingSearchRestoresFullList() {
		search().fill("zzzzzz");
		assertThat(visibleRows()).hasCount(0);
		assertThat(noResults()).isVisible();

		search().fill("");

		assertThat(visibleRows()).hasCount(TOTAL_OWNERS);
		assertThat(noResults()).isHidden();
	}

}
