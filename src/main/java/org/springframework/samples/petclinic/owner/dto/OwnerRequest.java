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
package org.springframework.samples.petclinic.owner.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for creating or updating an
 * {@link org.springframework.samples.petclinic.owner.Owner}. Deliberately has no
 * {@code id} field: the REST-world replacement for the old Thymeleaf controllers'
 * {@code setDisallowedFields("id", "*.id")} mass-assignment guard.
 */
public record OwnerRequest(@NotBlank String firstName, @NotBlank String lastName, @NotBlank String address,
		@NotBlank String city, @NotBlank String telephone) {
}
