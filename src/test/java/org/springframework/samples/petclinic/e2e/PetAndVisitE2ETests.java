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

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * End-to-end coverage for adding a pet and booking a visit — neither had any
 * browser-driven coverage before the React SPA migration.
 */
class PetAndVisitE2ETests extends PlaywrightTestSupport {

	/**
	 * Creates a fresh owner via the UI and leaves the browser on that owner's details
	 * page.
	 */
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
	 * Types a full date into an MUI X DatePicker field: it auto-advances
	 * section-by-section.
	 */
	private void typeDate(String label, String yyyyMmDd) {
		// getByLabel("Birth Date") is ambiguous: it matches both the field's visible
		// role=group wrapper and a hidden accessibility-only clone input MUI renders
		// alongside it. The group is the one that's actually interactive.
		page.getByRole(AriaRole.GROUP, new Page.GetByRoleOptions().setName(label)).click();
		page.keyboard().type(yyyyMmDd.replace("-", ""));
	}

	@Test
	void addPetShowsInOwnerDetails() {
		createOwner("Ada", "Lovelace");

		// "Add New Pet" is a RouterLink-backed Button, i.e. an <a>: role LINK, not
		// BUTTON.
		page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Add New Pet")).click();
		page.getByLabel("Name").fill("Analytical");
		typeDate("Birth Date", "2020-05-01");
		page.getByLabel("Type").click();
		page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("dog")).click();
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Pet")).click();

		assertThat(page.getByText("Analytical")).isVisible();
	}

	@Test
	void addVisitShowsInPreviousVisits() {
		createOwner("Marie", "Curie");

		// "Add New Pet" is a RouterLink-backed Button, i.e. an <a>: role LINK, not
		// BUTTON.
		page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Add New Pet")).click();
		page.getByLabel("Name").fill("Polonium");
		typeDate("Birth Date", "2019-01-01");
		page.getByLabel("Type").click();
		page.getByRole(AriaRole.OPTION, new Page.GetByRoleOptions().setName("cat")).click();
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Pet")).click();
		assertThat(page.getByText("Polonium")).isVisible();

		// Same RouterLink-vs-submit-button distinction as above: the details-page "Add
		// Visit" is a link to the visit form; the form's own submit button is a real
		// button.
		page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Add Visit")).click();
		page.getByLabel("Description").fill("Annual checkup");
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Visit")).click();

		assertThat(page.getByText("Annual checkup")).isVisible();
	}

}
