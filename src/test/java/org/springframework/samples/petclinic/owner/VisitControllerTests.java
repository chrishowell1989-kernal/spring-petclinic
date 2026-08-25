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
import org.springframework.samples.petclinic.owner.dto.VisitRequest;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for {@link VisitController}
 *
 * @author Colin But
 * @author Wick Dynex
 */
@WebMvcTest(VisitController.class)
@DisabledInNativeImage
@DisabledInAotMode
class VisitControllerTests {

	private static final int TEST_OWNER_ID = 1;

	private static final int TEST_PET_ID = 1;

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private OwnerService ownerService;

	private Visit visit() {
		Visit visit = new Visit();
		visit.setId(1);
		visit.setDate(LocalDate.now().plusDays(1));
		visit.setDescription("Visit Description");
		return visit;
	}

	@BeforeEach
	void init() {
		given(this.ownerService.addVisit(eq(TEST_OWNER_ID), eq(TEST_PET_ID), any(VisitRequest.class)))
			.willReturn(visit());
	}

	@Test
	void processNewVisitFormSuccess() throws Exception {
		VisitRequest request = new VisitRequest(LocalDate.now().plusDays(1), "Visit Description");

		mockMvc
			.perform(post("/api/owners/{ownerId}/pets/{petId}/visits", TEST_OWNER_ID, TEST_PET_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.description").value("Visit Description"));
	}

	@Test
	void processNewVisitFormHasErrorsWhenDescriptionBlank() throws Exception {
		VisitRequest request = new VisitRequest(LocalDate.now().plusDays(1), "");

		mockMvc
			.perform(post("/api/owners/{ownerId}/pets/{petId}/visits", TEST_OWNER_ID, TEST_PET_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[?(@.field=='description')]").exists());
	}

	@Test
	void processNewVisitFormHasErrorsWhenVisitDateIsNotInFuture() throws Exception {
		VisitRequest request = new VisitRequest(LocalDate.now(), "Visit Description");

		mockMvc
			.perform(post("/api/owners/{ownerId}/pets/{petId}/visits", TEST_OWNER_ID, TEST_PET_ID)
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.errors[?(@.field=='date')]").exists());
	}

}
