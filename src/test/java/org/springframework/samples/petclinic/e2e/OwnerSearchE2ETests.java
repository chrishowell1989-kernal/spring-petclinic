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

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * End-to-end coverage for the last-name owner search (SCRUM-11). The service was
 * concatenating the search term with itself ({@code "Davis"} became
 * {@code "DavisDavis"}), so last-name search always returned no results. These drive the
 * real browser search flow — the Find Owner form, then the results the SPA renders — to
 * verify matching owner records actually come back, exercising each acceptance-criteria
 * scenario.
 */
class OwnerSearchE2ETests extends PlaywrightTestSupport {

	private void searchByLastName(String lastName) {
		page.navigate(baseUrl() + "/owners/find");
		page.getByLabel("Last Name").fill(lastName);
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Find Owner")).click();
	}

	private void createOwner(String firstName, String lastName) {
		page.navigate(baseUrl() + "/owners/new");
		page.getByLabel("First Name").fill(firstName);
		page.getByLabel("Last Name").fill(lastName);
		page.getByLabel("Address").fill("1 Test Street");
		page.getByLabel("City").fill("Testville");
		page.getByLabel("Telephone").fill("1234567890");
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Owner")).click();
		assertThat(page.getByText(firstName + " " + lastName)).isVisible();
	}

	/**
	 * Scenario: Search by last name returns matching owner. Two seeded owners share the
	 * last name "Davis" (Betty and Harold), so the search renders the results list and
	 * both records must appear — before the fix this list came back empty.
	 */
	@Test
	void searchByLastNameShowsMatchingOwnersInResults() {
		searchByLastName("Davis");

		assertThat(page).hasURL(Pattern.compile(".*/owners\\?lastName=Davis.*"));
		assertThat(page.getByText("Betty Davis")).isVisible();
		assertThat(page.getByText("Harold Davis")).isVisible();
	}

	/**
	 * Scenario: Search by last name works immediately after adding a new owner. Adds a
	 * brand-new owner via the UI, then searches for that unique last name; the single
	 * match takes the user straight to the owner's details, confirming the record is
	 * findable without delay.
	 */
	@Test
	void searchFindsNewlyAddedOwnerImmediately() {
		createOwner("Sam", "Schultz");

		searchByLastName("Schultz");

		assertThat(page).hasURL(Pattern.compile(".*/owners/\\d+$"));
		assertThat(page.getByText("Sam Schultz")).isVisible();
	}

}
