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
 * lastName}), so every last-name search matched nothing.
 */
class OwnerSearchE2ETests extends PlaywrightTestSupport {

	private void addOwner(String firstName, String lastName) {
		page.navigate(baseUrl() + "/owners/new");
		page.getByLabel("First Name").fill(firstName);
		page.getByLabel("Last Name").fill(lastName);
		page.getByLabel("Address").fill("1 Test Way");
		page.getByLabel("City").fill("Testville");
		page.getByLabel("Telephone").fill("1234567890");
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Owner")).click();
		// Landed on the new owner's details page.
		assertThat(page).hasURL(Pattern.compile(".*/owners/\\d+$"));
	}

	private void searchByLastName(String lastName) {
		page.navigate(baseUrl() + "/owners/find");
		page.getByLabel("Last Name").fill(lastName);
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Find Owner")).click();
	}

	/**
	 * Scenario: Search by last name succeeds immediately after adding a new owner.
	 */
	@Test
	void searchByLastNameFindsNewlyAddedOwner() {
		// Unique last name so the search resolves to exactly one owner.
		String lastName = "Zephyrwind";
		addOwner("Quincy", lastName);

		searchByLastName(lastName);

		// The matching record is returned (single match navigates straight to details).
		assertThat(page).hasURL(Pattern.compile(".*/owners/\\d+$"));
		assertThat(page.getByText("Quincy " + lastName)).isVisible();
		// ...and the "not found" message is not displayed.
		assertThat(page.getByText(Pattern.compile("has not been found"))).not().isVisible();
	}

	/**
	 * Scenario: Search by last name works for pre-existing owners (regression check).
	 */
	@Test
	void searchByLastNameFindsPreExistingOwner() {
		searchByLastName("Franklin");

		// George Franklin is seed data; single match navigates straight to details.
		assertThat(page).hasURL(Pattern.compile(".*/owners/\\d+$"));
		assertThat(page.getByText("George Franklin")).isVisible();
		assertThat(page.getByText(Pattern.compile("has not been found"))).not().isVisible();
	}

}
