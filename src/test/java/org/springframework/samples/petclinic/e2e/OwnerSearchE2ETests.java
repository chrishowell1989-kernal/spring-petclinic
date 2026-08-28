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
 * End-to-end coverage for SCRUM-14: searching for an owner by last name must return the
 * matching record — both immediately after adding a brand-new owner and for pre-existing
 * (seed) owners — and must never surface the "has not been found" message for a real
 * match. The underlying bug double-concatenated the search term ({@code lastName +
 * lastName}), so every last-name search matched nothing (the UI then showed the
 * {@code "<term>" has not been found} warning instead of the owner).
 */
class OwnerSearchE2ETests extends PlaywrightTestSupport {

	private static final Pattern OWNER_DETAILS_URL = Pattern.compile(".*/owners/\\d+$");

	private static final Pattern NOT_FOUND_MESSAGE = Pattern.compile("has not been found");

	private void addOwner(String firstName, String lastName) {
		page.navigate(baseUrl() + "/owners/new");
		page.getByLabel("First Name").fill(firstName);
		page.getByLabel("Last Name").fill(lastName);
		page.getByLabel("Address").fill("1 Test Way");
		page.getByLabel("City").fill("Testville");
		page.getByLabel("Telephone").fill("1234567890");
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Owner")).click();
		// Landed on the new owner's details page.
		assertThat(page).hasURL(OWNER_DETAILS_URL);
	}

	private void searchByLastName(String lastName) {
		page.navigate(baseUrl() + "/owners/find");
		page.getByLabel("Last Name").fill(lastName);
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Find Owner")).click();
	}

	/**
	 * Scenario: Search by last name succeeds immediately after adding a new owner.
	 * <p>
	 * Given a new owner has just been added, When I search by their last name, Then the
	 * matching record is returned (a single match navigates straight to the details page)
	 * And the "Owner not found" message is not displayed.
	 */
	@Test
	void searchByLastNameFindsNewlyAddedOwner() {
		// Unique last name so the search resolves to exactly one owner.
		String lastName = "Zephyrwind";
		addOwner("Quincy", lastName);

		searchByLastName(lastName);

		// The matching record is returned (single match navigates straight to details).
		assertThat(page).hasURL(OWNER_DETAILS_URL);
		assertThat(page.getByText("Quincy " + lastName)).isVisible();
		// ...and the "not found" message is not displayed.
		assertThat(page.getByText(NOT_FOUND_MESSAGE)).not().isVisible();
	}

	/**
	 * Scenario: Search by last name succeeds immediately after adding a new owner —
	 * results-list variant.
	 * <p>
	 * With two owners sharing the freshly-added last name the UI renders the results list
	 * rather than redirecting, so this is where the "has not been found" warning would
	 * actually appear if the search returned nothing. It makes the acceptance criterion
	 * "an 'Owner not found' message should not be displayed" a meaningful assertion (and
	 * directly reproduces the reported bug, which surfaced that warning for every
	 * search).
	 */
	@Test
	void newlyAddedOwnersAppearInResultsWithoutNotFoundMessage() {
		String lastName = "Marbleheath";
		addOwner("Quincy", lastName);
		addOwner("Rhonda", lastName);

		searchByLastName(lastName);

		// Two matches keep us on the results list; both owners are shown...
		assertThat(page.getByText("Quincy " + lastName)).isVisible();
		assertThat(page.getByText("Rhonda " + lastName)).isVisible();
		// ...and the "not found" warning is absent even though the list view can show it.
		assertThat(page.getByText(NOT_FOUND_MESSAGE)).not().isVisible();
	}

	/**
	 * Scenario: Search by last name works for pre-existing owners (regression check).
	 * <p>
	 * George Franklin is seed data; searching his last name must still resolve to his
	 * record.
	 */
	@Test
	void searchByLastNameFindsPreExistingOwner() {
		searchByLastName("Franklin");

		// Single seed-data match navigates straight to details.
		assertThat(page).hasURL(OWNER_DETAILS_URL);
		assertThat(page.getByText("George Franklin")).isVisible();
		assertThat(page.getByText(NOT_FOUND_MESSAGE)).not().isVisible();
	}

}
