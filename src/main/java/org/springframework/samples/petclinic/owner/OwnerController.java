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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.owner.dto.OwnerRequest;
import org.springframework.samples.petclinic.owner.dto.OwnerResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Wick Dynex
 */
@RestController
class OwnerController {

	private final OwnerService ownerService;

	OwnerController(OwnerService ownerService) {
		this.ownerService = ownerService;
	}

	@PostMapping("/api/owners")
	ResponseEntity<OwnerResponse> create(@Valid @RequestBody OwnerRequest request) {
		OwnerResponse created = OwnerResponse.of(ownerService.create(request));
		return ResponseEntity.created(URI.create("/api/owners/" + created.id())).body(created);
	}

	@GetMapping("/api/owners")
	PagedModel<OwnerResponse> search(@RequestParam(defaultValue = "") String lastName,
			@PageableDefault(size = 5) Pageable pageable) {
		Page<OwnerResponse> owners = ownerService.search(lastName, pageable).map(OwnerResponse::of);
		return new PagedModel<>(owners);
	}

	@GetMapping("/api/owners/{ownerId}")
	OwnerResponse get(@PathVariable int ownerId) {
		return OwnerResponse.of(ownerService.getOrThrow(ownerId));
	}

	@PutMapping("/api/owners/{ownerId}")
	OwnerResponse update(@PathVariable int ownerId, @Valid @RequestBody OwnerRequest request) {
		return OwnerResponse.of(ownerService.update(ownerId, request));
	}

}
