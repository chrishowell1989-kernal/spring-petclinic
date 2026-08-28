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
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase.Replace;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.owner.dto.OwnerRequest;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for {@link OwnerService}, exercising the search-by-last-name business
 * logic against H2. Covers the regression where searching by last name returned no match
 * (surfaced as an "Owner not found" empty result) for both newly added and pre-existing
 * owners.
 */
@DataJpaTest
@Import(OwnerService.class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
class OwnerServiceTests {

	@Autowired
	private OwnerService ownerService;

	private final Pageable pageable = Pageable.unpaged();

	@Test
	@Transactional
	void shouldFindOwnerByLastNameImmediatelyAfterAdding() {
		OwnerRequest request = new OwnerRequest("Sam", "Schultz", "4, Evans Street", "Wollongong", "4444444444");
		Owner created = this.ownerService.create(request);
		assertThat(created.getId()).isNotNull();

		Page<Owner> found = this.ownerService.search("Schultz", pageable);

		assertThat(found).isNotEmpty();
		assertThat(found).extracting(Owner::getLastName).contains("Schultz");
	}

	@Test
	void shouldFindPreExistingOwnerByLastName() {
		Page<Owner> found = this.ownerService.search("Franklin", pageable);

		assertThat(found).isNotEmpty();
		assertThat(found).extracting(Owner::getLastName).allMatch(name -> name.startsWith("Franklin"));
	}

	@Test
	void shouldTrimSurroundingWhitespaceWhenSearching() {
		Page<Owner> found = this.ownerService.search("  Franklin  ", pageable);

		assertThat(found).isNotEmpty();
		assertThat(found).extracting(Owner::getLastName).allMatch(name -> name.startsWith("Franklin"));
	}

	@Test
	void shouldReturnEmptyPageWhenNoOwnerMatches() {
		Page<Owner> found = this.ownerService.search("NoSuchSurname", pageable);

		assertThat(found).isEmpty();
	}

}
