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
package org.springframework.samples.petclinic.vet;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
class VetController {

	private final VetRepository vetRepository;

	public VetController(VetRepository vetRepository) {
		this.vetRepository = vetRepository;
	}

	@GetMapping("/vets.html")
	public String showVetList(@RequestParam(defaultValue = "1") int page,
			@RequestParam(name = "name", defaultValue = "") String name,
			@RequestHeader(name = "X-Requested-With", required = false) String requestedWith, Model model) {
		// A blank search term (or an all-whitespace one) falls back to the full paged
		// list, so the page behaves exactly as before when no filter is applied.
		String searchName = name.strip();
		Page<Vet> paginated = findPaginated(page, searchName);
		model.addAttribute("name", searchName);
		// The search box submits via a background fetch (see vetList.html) so that
		// re-rendering the results on each keystroke doesn't steal focus from the
		// input by replacing the whole page; only the results fragment is returned.
		boolean isAjax = "XMLHttpRequest".equals(requestedWith);
		return addPaginationModel(page, paginated, model, isAjax);
	}

	private String addPaginationModel(int page, Page<Vet> paginated, Model model, boolean isAjax) {
		List<Vet> listVets = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listVets", listVets);
		return isAjax ? "vets/vetList :: results" : "vets/vetList";
	}

	private Page<Vet> findPaginated(int page, String name) {
		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		// Search is backend-driven: the filter is applied across the whole data set, then
		// paged, rather than filtering an already-paged list on the client.
		if (name.isEmpty()) {
			return vetRepository.findAll(pageable);
		}
		return vetRepository.findByName(name, pageable);
	}

	@GetMapping({ "/vets" })
	public @ResponseBody Vets showResourcesVetList() {
		// Here we are returning an object of type 'Vets' rather than a collection of Vet
		// objects so it is simpler for JSon/Object mapping
		Vets vets = new Vets();
		vets.getVetList().addAll(this.vetRepository.findAll());
		return vets;
	}

}
