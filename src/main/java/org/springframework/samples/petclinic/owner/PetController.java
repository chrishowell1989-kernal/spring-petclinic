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

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.owner.dto.PetRequest;
import org.springframework.samples.petclinic.owner.dto.PetResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Wick Dynex
 */
@RestController
@RequestMapping("/api/owners/{ownerId}/pets")
class PetController {

	private final OwnerService ownerService;

	PetController(OwnerService ownerService) {
		this.ownerService = ownerService;
	}

	@PostMapping
	ResponseEntity<PetResponse> create(@PathVariable int ownerId, @Valid @RequestBody PetRequest request) {
		PetResponse created = PetResponse.of(ownerService.addPet(ownerId, request));
		return ResponseEntity.created(URI.create("/api/owners/" + ownerId + "/pets/" + created.id())).body(created);
	}

	@PutMapping("/{petId}")
	PetResponse update(@PathVariable int ownerId, @PathVariable int petId, @Valid @RequestBody PetRequest request) {
		return PetResponse.of(ownerService.updatePet(ownerId, petId, request));
	}

}
