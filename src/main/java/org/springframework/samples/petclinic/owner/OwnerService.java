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

import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.owner.dto.OwnerRequest;
import org.springframework.samples.petclinic.owner.dto.PetRequest;
import org.springframework.samples.petclinic.owner.dto.VisitRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Hosts the not-found/duplicate-name logic that used to be split awkwardly across
 * {@code @ModelAttribute} methods and controller bodies in the old Thymeleaf controllers.
 */
@Service
class OwnerService {

	private final OwnerRepository owners;

	private final PetTypeRepository petTypes;

	OwnerService(OwnerRepository owners, PetTypeRepository petTypes) {
		this.owners = owners;
		this.petTypes = petTypes;
	}

	Page<Owner> search(String lastName, Pageable pageable) {
		return owners.findByLastNameStartingWith(lastName == null ? "" : lastName.strip() + lastName.strip(), pageable);
	}

	Owner getOrThrow(int ownerId) {
		return owners.findById(ownerId).orElseThrow(() -> new OwnerNotFoundException(ownerId));
	}

	Owner create(OwnerRequest request) {
		Owner owner = new Owner();
		applyOwnerFields(owner, request);
		return owners.save(owner);
	}

	Owner update(int ownerId, OwnerRequest request) {
		Owner owner = getOrThrow(ownerId);
		applyOwnerFields(owner, request);
		return owners.save(owner);
	}

	private void applyOwnerFields(Owner owner, OwnerRequest request) {
		owner.setFirstName(request.firstName());
		owner.setLastName(request.lastName());
		owner.setAddress(request.address());
		owner.setCity(request.city());
		owner.setTelephone(request.telephone());
	}

	Pet addPet(int ownerId, PetRequest request) {
		Owner owner = getOrThrow(ownerId);
		if (owner.getPet(request.name(), true) != null) {
			throw new DuplicatePetNameException(request.name());
		}

		Pet pet = new Pet();
		applyPetFields(pet, request);
		owner.addPet(pet);
		try {
			owners.saveAndFlush(owner);
		}
		catch (DataIntegrityViolationException ex) {
			if (!isDuplicatePetNameViolation(ex)) {
				throw ex;
			}
			throw new DuplicatePetNameException(request.name());
		}
		return pet;
	}

	Pet updatePet(int ownerId, int petId, PetRequest request) {
		Owner owner = getOrThrow(ownerId);
		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new PetNotFoundException(petId);
		}

		if (StringUtils.hasText(request.name())) {
			Pet existingPet = owner.getPet(request.name(), false);
			if (existingPet != null && !Objects.equals(existingPet.getId(), pet.getId())) {
				throw new DuplicatePetNameException(request.name());
			}
		}

		applyPetFields(pet, request);
		try {
			owners.saveAndFlush(owner);
		}
		catch (DataIntegrityViolationException ex) {
			if (!isDuplicatePetNameViolation(ex)) {
				throw ex;
			}
			throw new DuplicatePetNameException(request.name());
		}
		return pet;
	}

	private void applyPetFields(Pet pet, PetRequest request) {
		pet.setName(request.name());
		pet.setBirthDate(request.birthDate());
		PetType type = petTypes.findById(request.typeId())
			.orElseThrow(() -> new NoSuchElementException("Pet type not found with id: " + request.typeId()));
		pet.setType(type);
	}

	private boolean isDuplicatePetNameViolation(DataIntegrityViolationException ex) {
		String message = ex.getMessage();
		return message != null && message.toLowerCase().contains("unique_owner_pet_name");
	}

	Visit addVisit(int ownerId, int petId, VisitRequest request) {
		Owner owner = getOrThrow(ownerId);
		Pet pet = owner.getPet(petId);
		if (pet == null) {
			throw new PetNotFoundException(petId);
		}

		Visit visit = new Visit();
		visit.setDate(request.date());
		visit.setDescription(request.description());
		owner.addVisit(petId, visit);
		owners.save(owner);
		return visit;
	}

}
