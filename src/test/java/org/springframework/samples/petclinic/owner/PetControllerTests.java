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

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.owner.dto.PetRequest;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for the {@link PetController}
 *
 * @author Colin But
 * @author Wick Dynex
 */
@WebMvcTest(PetController.class)
@DisabledInNativeImage
@DisabledInAotMode
class PetControllerTests {

	private static final int TEST_OWNER_ID = 1;

	private static final int TEST_PET_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private OwnerService ownerService;

	private Pet betty() {
		Pet pet = new Pet();
		pet.setId(TEST_PET_ID);
		pet.setName("Betty");
		pet.setBirthDate(LocalDate.of(2015, 2, 12));
		PetType hamster = new PetType();
		hamster.setId(3);
		hamster.setName("hamster");
		pet.setType(hamster);
		return pet;
	}

	@BeforeEach
	void setup() {
		given(this.ownerService.addPet(eq(TEST_OWNER_ID), any(PetRequest.class))).willReturn(betty());
		given(this.ownerService.updatePet(eq(TEST_OWNER_ID), anyInt(), any(PetRequest.class))).willReturn(betty());
	}

	@Test
	void createPetSuccess() throws Exception {
		PetRequest request = new PetRequest("Betty", LocalDate.of(2015, 2, 12), 3);

		mockMvc
			.perform(post("/api/owners/{ownerId}/pets", TEST_OWNER_ID).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.name").value("Betty"));
	}

	@Test
	void createPetWithBlankName() throws Exception {
		PetRequest request = new PetRequest(" ", LocalDate.of(2015, 2, 12), 3);

		mockMvc
			.perform(post("/api/owners/{ownerId}/pets", TEST_OWNER_ID).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[?(@.field=='name')]").exists());
	}

	@Test
	void createPetWithMissingType() throws Exception {
		PetRequest request = new PetRequest("Betty", LocalDate.of(2015, 2, 12), null);

		mockMvc
			.perform(post("/api/owners/{ownerId}/pets", TEST_OWNER_ID).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[?(@.field=='typeId')]").exists());
	}

	@Test
	void createPetWithFutureBirthDate() throws Exception {
		PetRequest request = new PetRequest("Betty", LocalDate.now().plusMonths(1), 3);

		mockMvc
			.perform(post("/api/owners/{ownerId}/pets", TEST_OWNER_ID).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[?(@.field=='birthDate')]").exists());
	}

	@Test
	void createPetWithDuplicateName() throws Exception {
		given(this.ownerService.addPet(eq(TEST_OWNER_ID), any(PetRequest.class)))
			.willThrow(new DuplicatePetNameException("Betty"));
		PetRequest request = new PetRequest("Betty", LocalDate.of(2015, 2, 12), 3);

		mockMvc
			.perform(post("/api/owners/{ownerId}/pets", TEST_OWNER_ID).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isConflict())
			.andExpect(jsonPath("$.errors[0].field").value("name"));
	}

	@Test
	void updatePetSuccess() throws Exception {
		PetRequest request = new PetRequest("Betty", LocalDate.of(2015, 2, 12), 3);

		mockMvc
			.perform(put("/api/owners/{ownerId}/pets/{petId}", TEST_OWNER_ID, TEST_PET_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.name").value("Betty"));
	}

	@Test
	void updatePetWithDuplicateName() throws Exception {
		given(this.ownerService.updatePet(eq(TEST_OWNER_ID), eq(TEST_PET_ID + 1), any(PetRequest.class)))
			.willThrow(new DuplicatePetNameException("Betty"));
		PetRequest request = new PetRequest("Betty", LocalDate.of(2015, 2, 12), 3);

		mockMvc
			.perform(put("/api/owners/{ownerId}/pets/{petId}", TEST_OWNER_ID, TEST_PET_ID + 1)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isConflict());
	}

	@Test
	void updatePetNotFound() throws Exception {
		given(this.ownerService.updatePet(eq(TEST_OWNER_ID), eq(999), any(PetRequest.class)))
			.willThrow(new PetNotFoundException(999));
		PetRequest request = new PetRequest("Betty", LocalDate.of(2015, 2, 12), 3);

		mockMvc
			.perform(put("/api/owners/{ownerId}/pets/{petId}", TEST_OWNER_ID, 999)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound());
	}

}
