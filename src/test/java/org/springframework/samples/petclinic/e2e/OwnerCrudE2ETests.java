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
 * End-to-end coverage for the owner add/find/edit flows — none of which had any
 * browser-driven coverage before the React SPA migration (only {@code @WebMvcTest}
 * view/model assertions).
 */
class OwnerCrudE2ETests extends PlaywrightTestSupport {

	private void fillOwnerForm(String firstName, String lastName, String address, String city, String telephone) {
		page.getByLabel("First Name").fill(firstName);
		page.getByLabel("Last Name").fill(lastName);
		page.getByLabel("Address").fill(address);
		page.getByLabel("City").fill(city);
		page.getByLabel("Telephone").fill(telephone);
	}

	@Test
	void addOwnerCreatesAndShowsDetails() {
		page.navigate(baseUrl() + "/owners/new");

		fillOwnerForm("Alice", "Wonderland", "42 Rabbit Hole", "Fantasyland", "1234567890");
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Owner")).click();

		assertThat(page).hasURL(Pattern.compile(".*/owners/\\d+$"));
		assertThat(page.getByText("Alice Wonderland")).isVisible();
		assertThat(page.getByText("42 Rabbit Hole")).isVisible();
	}

	@Test
	void findOwnerWithSingleMatchNavigatesStraightToDetails() {
		page.navigate(baseUrl() + "/owners/find");

		page.getByLabel("Last Name").fill("Franklin");
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Find Owner")).click();

		assertThat(page).hasURL(Pattern.compile(".*/owners/\\d+$"));
		assertThat(page.getByText("George Franklin")).isVisible();
	}

	@Test
	void editOwnerUpdatesDetails() {
		page.navigate(baseUrl() + "/owners/new");
		fillOwnerForm("Grace", "Hopper", "1 Compiler Way", "Arlington", "1112223333");
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Add Owner")).click();
		assertThat(page.getByText("Grace Hopper")).isVisible();

		// "Edit Owner" is a RouterLink-backed Button, i.e. an <a>: role LINK, not BUTTON.
		page.getByRole(AriaRole.LINK, new Page.GetByRoleOptions().setName("Edit Owner")).click();
		page.getByLabel("City").fill("Norfolk");
		page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Update Owner")).click();

		assertThat(page.getByText("Norfolk")).isVisible();
	}

}
