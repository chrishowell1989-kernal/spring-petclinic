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

package org.springframework.samples.petclinic.owner;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.owner.dto.OwnerRequest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Full-context integration tests for {@link OwnerService#search}, exercising the search
 * against the seeded H2 database. These guard against the last-name search regression
 * reported in SCRUM-11, where the service was querying for a doubled search term (e.g.
 * {@code "Davis"} became {@code "DavisDavis"}) and therefore never matched.
 */
@SpringBootTest
class OwnerServiceIntegrationTests {

	@Autowired
	private OwnerService ownerService;

	private final Pageable pageable = Pageable.unpaged();

	@Test
	void searchByLastNameReturnsMatchingOwner() {
		Page<Owner> results = this.ownerService.search("Davis", pageable);

		assertThat(results).isNotEmpty();
		assertThat(results).allMatch(owner -> owner.getLastName().startsWith("Davis"));
	}

	@Test
	void searchByLastNameIgnoresSurroundingWhitespace() {
		Page<Owner> results = this.ownerService.search("  Davis  ", pageable);

		assertThat(results).isNotEmpty();
		assertThat(results).allMatch(owner -> owner.getLastName().startsWith("Davis"));
	}

	@Test
	@Transactional
	void searchByLastNameFindsNewlyAddedOwner() {
		OwnerRequest request = new OwnerRequest("Sam", "Schultz", "4, Evans Street", "Wollongong", "4444444444");
		this.ownerService.create(request);

		Page<Owner> results = this.ownerService.search("Schultz", pageable);

		assertThat(results).isNotEmpty();
		assertThat(results).anyMatch(owner -> "Schultz".equals(owner.getLastName()));
	}

}
