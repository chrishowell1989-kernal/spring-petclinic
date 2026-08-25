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
package org.springframework.samples.petclinic.system;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.samples.petclinic.owner.DuplicatePetNameException;
import org.springframework.samples.petclinic.owner.OwnerNotFoundException;
import org.springframework.samples.petclinic.owner.PetNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Scoped to {@code @RestController} beans only — an unscoped advice would also catch
 * {@code NoResourceFoundException} from static-resource lookups (e.g. a missing hashed
 * asset) and turn a plain 404 into a misleading 500.
 */
@RestControllerAdvice(annotations = RestController.class)
class ApiExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
		List<ApiError.FieldError> errors = ex.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(this::toFieldError)
			.toList();
		return ResponseEntity.badRequest().body(new ApiError(errors));
	}

	private ApiError.FieldError toFieldError(FieldError fieldError) {
		return new ApiError.FieldError(fieldError.getField(), fieldError.getCode(), fieldError.getDefaultMessage());
	}

	@ExceptionHandler({ OwnerNotFoundException.class, PetNotFoundException.class })
	ResponseEntity<ApiError> handleNotFound(RuntimeException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiError.of("", "notFound", ex.getMessage()));
	}

	@ExceptionHandler(DuplicatePetNameException.class)
	ResponseEntity<ApiError> handleDuplicatePetName(DuplicatePetNameException ex) {
		return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiError.of("name", "duplicate", "already exists"));
	}

	@ExceptionHandler(NoSuchElementException.class)
	ResponseEntity<ApiError> handleBadReference(NoSuchElementException ex) {
		return ResponseEntity.badRequest().body(ApiError.of("", "invalid", ex.getMessage()));
	}

	@ExceptionHandler(Exception.class)
	ResponseEntity<ApiError> handleGeneric(Exception ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
			.body(ApiError.of("", "error", "An unexpected error occurred."));
	}

}
