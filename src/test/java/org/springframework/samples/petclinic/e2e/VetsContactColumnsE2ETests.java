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

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import org.junit.jupiter.api.Test;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

/**
 * End-to-end coverage for SCRUM-12 — the Email Address and Contact Number columns on the
 * Vets table. Each test maps to one acceptance-criteria scenario from the Jira story: the
 * two new column headers are present, and a seeded UK mobile number is rendered in the
 * "07xxx xxxxxx" national format.
 */
class VetsContactColumnsE2ETests extends PlaywrightTestSupport {

	// Scenario: Vets table displays email and contact number columns
	@Test
	void vetsTableShowsEmailAddressAndContactNumberColumnHeaders() {
		page.navigate(baseUrl() + "/vets");

		// Wait for the table to load before asserting on headers.
		assertThat(page.getByText("James Carter")).isVisible();

		assertThat(page.getByRole(AriaRole.COLUMNHEADER, new Page.GetByRoleOptions().setName("Email Address")))
			.isVisible();
		assertThat(page.getByRole(AriaRole.COLUMNHEADER, new Page.GetByRoleOptions().setName("Contact Number")))
			.isVisible();
	}

	// Scenario: Contact number is displayed in UK mobile format
	@Test
	void vetsTableShowsEmailAndContactNumberInUkMobileFormat() {
		page.navigate(baseUrl() + "/vets");

		// Scope to James Carter's row so the email/number assertions are unambiguous.
		// Seeded telephone 07700900123 must render as "07700 900123".
		Locator jamesRow = page.getByRole(AriaRole.ROW).filter(new Locator.FilterOptions().setHasText("James Carter"));

		assertThat(jamesRow.getByText("james.carter@petclinic.com")).isVisible();
		assertThat(jamesRow.getByText("07700 900123")).isVisible();
	}

}
