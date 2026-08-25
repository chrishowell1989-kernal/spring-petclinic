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
 * End-to-end coverage for the veterinarians list and its pagination (six seeded vets,
 * page size five).
 */
class VetsListE2ETests extends PlaywrightTestSupport {

	@Test
	void vetsListShowsSeededVetsWithSpecialtiesAndPaginates() {
		page.navigate(baseUrl() + "/vets");

		assertThat(page.getByText("James Carter")).isVisible();
		// Two vets have "radiology": scope to Helen Leary's row to keep the locator
		// unambiguous.
		assertThat(page.getByRole(AriaRole.ROW)
			.filter(new Locator.FilterOptions().setHasText("Helen Leary"))
			.getByText("radiology")).isVisible();
		assertThat(page.getByText("Sharon Jenkins")).not().isVisible();

		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Next")).click();

		assertThat(page.getByText("Sharon Jenkins")).isVisible();
		assertThat(page.getByText("James Carter")).not().isVisible();
	}

}
