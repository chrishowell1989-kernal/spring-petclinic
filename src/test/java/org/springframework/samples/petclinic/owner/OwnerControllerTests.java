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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.samples.petclinic.owner.dto.OwnerRequest;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for {@link OwnerController}
 *
 * @author Colin But
 * @author Wick Dynex
 */
@WebMvcTest(OwnerController.class)
@DisabledInNativeImage
@DisabledInAotMode
class OwnerControllerTests {

	private static final int TEST_OWNER_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private OwnerService ownerService;

	private Owner george() {
		Owner george = new Owner();
		george.setId(TEST_OWNER_ID);
		george.setFirstName("George");
		george.setLastName("Franklin");
		george.setAddress("110 W. Liberty St.");
		george.setCity("Madison");
		george.setTelephone("6085551023");
		Pet max = new Pet();
		PetType dog = new PetType();
		dog.setName("dog");
		max.setType(dog);
		max.setName("Max");
		max.setBirthDate(LocalDate.now());
		george.addPet(max);
		max.setId(1);
		return george;
	}

	@BeforeEach
	void setup() {
		given(this.ownerService.getOrThrow(TEST_OWNER_ID)).willReturn(george());
	}

	@Test
	void createOwnerSuccess() throws Exception {
		OwnerRequest request = new OwnerRequest("Joe", "Bloggs", "123 Caramel Street", "London", "1316761638");
		given(this.ownerService.create(any(OwnerRequest.class))).willReturn(george());

		mockMvc
			.perform(post("/api/owners").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.id").value(TEST_OWNER_ID));
	}

	@Test
	void createOwnerHasErrors() throws Exception {
		OwnerRequest request = new OwnerRequest("Joe", "Bloggs", "", "London", "");

		mockMvc
			.perform(post("/api/owners").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[?(@.field=='address')]").exists())
			.andExpect(jsonPath("$.errors[?(@.field=='telephone')]").exists());
	}

	@Test
	void searchOwnersSuccess() throws Exception {
		given(this.ownerService.search(any(), any(Pageable.class))).willReturn(new PageImpl<>(List.of(george())));

		mockMvc.perform(get("/api/owners").param("lastName", "Franklin"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content[0].lastName").value("Franklin"));
	}

	@Test
	void searchOwnersNoneFound() throws Exception {
		given(this.ownerService.search(eq("Unknown Surname"), any(Pageable.class)))
			.willReturn(new PageImpl<>(List.of()));

		mockMvc.perform(get("/api/owners").param("lastName", "Unknown Surname"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.content").isEmpty());
	}

	@Test
	void getOwnerSuccess() throws Exception {
		mockMvc.perform(get("/api/owners/{ownerId}", TEST_OWNER_ID))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.firstName").value("George"))
			.andExpect(jsonPath("$.lastName").value("Franklin"))
			.andExpect(jsonPath("$.address").value("110 W. Liberty St."))
			.andExpect(jsonPath("$.city").value("Madison"))
			.andExpect(jsonPath("$.telephone").value("6085551023"))
			.andExpect(jsonPath("$.pets", org.hamcrest.Matchers.hasSize(1)));
	}

	@Test
	void getOwnerNotFound() throws Exception {
		given(this.ownerService.getOrThrow(999)).willThrow(new OwnerNotFoundException(999));

		mockMvc.perform(get("/api/owners/{ownerId}", 999)).andExpect(status().isNotFound());
	}

	@Test
	void updateOwnerSuccess() throws Exception {
		OwnerRequest request = new OwnerRequest("Joe", "Bloggs", "123 Caramel Street", "London", "1616291589");
		given(this.ownerService.update(eq(TEST_OWNER_ID), any(OwnerRequest.class))).willReturn(george());

		mockMvc
			.perform(put("/api/owners/{ownerId}", TEST_OWNER_ID).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk());
	}

	@Test
	void updateOwnerHasErrors() throws Exception {
		OwnerRequest request = new OwnerRequest("Joe", "Bloggs", "", "London", "");

		mockMvc
			.perform(put("/api/owners/{ownerId}", TEST_OWNER_ID).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[?(@.field=='address')]").exists())
			.andExpect(jsonPath("$.errors[?(@.field=='telephone')]").exists());
	}

	@Test
	void updateOwnerNotFound() throws Exception {
		OwnerRequest request = new OwnerRequest("Joe", "Bloggs", "123 Caramel Street", "London", "1616291589");
		given(this.ownerService.update(eq(999), any(OwnerRequest.class))).willThrow(new OwnerNotFoundException(999));

		mockMvc
			.perform(put("/api/owners/{ownerId}", 999).contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isNotFound());
	}

}
