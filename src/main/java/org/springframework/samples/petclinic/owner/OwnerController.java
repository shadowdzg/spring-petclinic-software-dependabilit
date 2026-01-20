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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import jakarta.validation.Valid;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Wick Dynex
 */
@Controller
class OwnerController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "owners/createOrUpdateOwnerForm";

	private final OwnerRepository owners;

	private final OwnerRepositoryImpl ownerRepositoryImpl;

	public OwnerController(OwnerRepository owners, OwnerRepositoryImpl ownerRepositoryImpl) {
		this.owners = owners;
		this.ownerRepositoryImpl = ownerRepositoryImpl;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	/**
	 * Finds an owner by ID or returns a new owner if ID is null.
	 * @param ownerId the owner ID, may be null
	 * @return an Owner instance, never null
	 * @throws IllegalArgumentException if ownerId is provided but owner not found
	 */
	// @ requires ownerId == null || ownerId > 0;
	// @ ensures \result != null;
	// @ ensures ownerId == null ==> \result.isNew();
	// @ ensures ownerId != null ==> (\result.getId() != null &&
	// \result.getId().equals(ownerId));
	// @ ensures ownerId != null ==> !\result.isNew();
	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable(name = "ownerId", required = false) Integer ownerId) {
		return ownerId == null ? new Owner()
				: this.owners.findById(ownerId)
					.orElseThrow(() -> new IllegalArgumentException("Owner not found with id: " + ownerId
							+ ". Please ensure the ID is correct " + "and the owner exists in the database."));
	}

	@GetMapping("/owners/new")
	public String initCreationForm() {
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	/**
	 * Processes the creation form for a new owner.
	 * @param owner the owner to create, must be valid
	 * @param result binding result for validation
	 * @param redirectAttributes attributes for redirect
	 * @return view name or redirect URL
	 */
	// @ requires owner != null;
	// @ requires result != null;
	// @ requires redirectAttributes != null;
	// @ ensures \result != null;
	// @ ensures result.hasErrors() ==> \result.equals(VIEWS_OWNER_CREATE_OR_UPDATE_FORM);
	// @ ensures !result.hasErrors() ==> \result.startsWith("redirect:/owners/");
	// @ ensures !result.hasErrors() ==> !owner.isNew();
	@PostMapping("/owners/new")
	public String processCreationForm(@Valid Owner owner, BindingResult result, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "There was an error in creating the owner.");
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}

		this.owners.save(owner);
		redirectAttributes.addFlashAttribute("message", "New Owner Created");
		return "redirect:/owners/" + owner.getId();
	}

	@GetMapping("/owners/find")
	public String initFindForm() {
		return "owners/findOwners";
	}

	/**
	 * Processes the find form to search for owners by last name.
	 * @param page page number (default 1)
	 * @param owner owner object containing search criteria
	 * @param result binding result
	 * @param model Spring model
	 * @return view name or redirect URL
	 */
	// @ requires page > 0;
	// @ requires owner != null;
	// @ requires result != null;
	// @ requires model != null;
	// @ ensures \result != null;
	// @ ensures \result.equals("owners/findOwners") ||
	// \result.equals("owners/ownersList") || \result.startsWith("redirect:/owners/");
	@GetMapping("/owners")
	public String processFindForm(@RequestParam(defaultValue = "1") int page, Owner owner, BindingResult result,
			Model model) {
		// allow parameterless GET request for /owners to return all records
		String lastName = owner.getLastName();
		if (lastName == null) {
			lastName = ""; // empty string signifies broadest possible search
		}

		// find owners by last name
		Page<Owner> ownersResults = findPaginatedForOwnersLastName(page, lastName);
		if (ownersResults.isEmpty()) {
			// no owners found
			result.rejectValue("lastName", "notFound", "not found");
			return "owners/findOwners";
		}

		if (ownersResults.getTotalElements() == 1) {
			// 1 owner found
			owner = ownersResults.iterator().next();
			return "redirect:/owners/" + owner.getId();
		}

		// multiple owners found
		return addPaginationModel(page, model, ownersResults);
	}

	private String addPaginationModel(int page, Model model, Page<Owner> paginated) {
		List<Owner> listOwners = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listOwners", listOwners);
		return "owners/ownersList";
	}

	private Page<Owner> findPaginatedForOwnersLastName(int page, String lastname) {
		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return owners.findByLastNameStartingWith(lastname, pageable);
	}

	@GetMapping("/owners/{ownerId}/edit")
	public String initUpdateOwnerForm() {
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	/**
	 * Processes the update form for an existing owner.
	 * @param owner the owner with updated values
	 * @param result binding result for validation
	 * @param ownerId the owner ID from URL path
	 * @param redirectAttributes attributes for redirect
	 * @return view name or redirect URL
	 */
	// @ requires owner != null;
	// @ requires result != null;
	// @ requires ownerId > 0;
	// @ requires redirectAttributes != null;
	// @ ensures \result != null;
	// @ ensures result.hasErrors() || !Objects.equals(owner.getId(), ownerId) ==>
	// (\result.equals(VIEWS_OWNER_CREATE_OR_UPDATE_FORM) ||
	// \result.startsWith("redirect:/owners/"));
	// @ ensures !result.hasErrors() && Objects.equals(owner.getId(), ownerId) ==>
	// \result.startsWith("redirect:/owners/");
	@PostMapping("/owners/{ownerId}/edit")
	public String processUpdateOwnerForm(@Valid Owner owner, BindingResult result, @PathVariable("ownerId") int ownerId,
			RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "There was an error in updating the owner.");
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}

		if (!Objects.equals(owner.getId(), ownerId)) {
			result.rejectValue("id", "mismatch", "The owner ID in the form does not match the URL.");
			redirectAttributes.addFlashAttribute("error", "Owner ID mismatch. Please try again.");
			return "redirect:/owners/{ownerId}/edit";
		}

		owner.setId(ownerId);
		this.owners.save(owner);
		redirectAttributes.addFlashAttribute("message", "Owner Values Updated");
		return "redirect:/owners/{ownerId}";
	}

	/**
	 * Custom handler for displaying an owner.
	 * @param ownerId the ID of the owner to display
	 * @return a ModelMap with the model attributes for the view
	 */
	// @ requires ownerId > 0;
	// @ requires this.owners.findById(ownerId).isPresent();
	// @ ensures \result != null;
	// @ ensures \result.getViewName().equals("owners/ownerDetails");
	// @ ensures \result.getModel().containsKey("owner");
	@GetMapping("/owners/{ownerId}")
	public ModelAndView showOwner(@PathVariable("ownerId") int ownerId) {
		ModelAndView mav = new ModelAndView("owners/ownerDetails");
		Optional<Owner> optionalOwner = this.owners.findById(ownerId);
		Owner owner = optionalOwner.orElseThrow(() -> new IllegalArgumentException(
				"Owner not found with id: " + ownerId + ". Please ensure the ID is correct "));
		mav.addObject(owner);
		return mav;
	}

	/**
	 * VULNERABLE ENDPOINT - SQL Injection (OWASP A1: Injection)
	 *
	 * This endpoint uses a vulnerable repository method that concatenates user input
	 * directly into SQL queries.
	 *
	 * Attack example: /owners/vulnerable/search?lastName=Smith' OR '1'='1
	 * @param lastName user input that will be concatenated into SQL
	 * @param model Spring model
	 * @return view with search results
	 */
	@GetMapping("/owners/vulnerable/search")
	public String vulnerableSearch(@RequestParam(required = false) String lastName, Model model) {
		if (lastName != null && !lastName.isEmpty()) {
			// VULNERABLE: Uses repository method with SQL injection vulnerability
			List<Owner> results = ownerRepositoryImpl.findByLastNameVulnerable(lastName);
			model.addAttribute("listOwners", results);
			model.addAttribute("searchTerm", lastName); // Also vulnerable to XSS
		}
		return "owners/ownersList";
	}

	/**
	 * VULNERABLE ENDPOINT - SQL Injection + XSS (OWASP A1 + A3)
	 *
	 * This endpoint is vulnerable to both SQL injection and XSS attacks.
	 * @param searchTerm user input used in SQL and displayed in HTML
	 * @param model Spring model
	 * @return view with search results
	 */
	@GetMapping("/owners/vulnerable/searchAll")
	public String vulnerableSearchAll(@RequestParam(required = false) String searchTerm, Model model) {
		if (searchTerm != null && !searchTerm.isEmpty()) {
			// VULNERABLE: SQL Injection
			List<Owner> results = ownerRepositoryImpl.searchOwnersVulnerable(searchTerm);
			model.addAttribute("listOwners", results);
			// VULNERABLE: XSS - user input added directly to model without encoding
			model.addAttribute("message", "Search results for: " + searchTerm);
			model.addAttribute("searchTerm", searchTerm);
		}
		return "owners/ownersList";
	}

	/**
	 * VULNERABLE ENDPOINT - XSS (OWASP A3: Cross-Site Scripting)
	 *
	 * This endpoint displays user input without proper encoding.
	 *
	 * Attack example: /owners/vulnerable/comment?comment=<script>alert('XSS')</script>
	 * @param comment user input that will be displayed without encoding
	 * @param model Spring model
	 * @return view displaying the comment
	 */
	@GetMapping("/owners/vulnerable/comment")
	public String vulnerableComment(@RequestParam(required = false) String comment, Model model) {
		// VULNERABLE: User input added directly without encoding
		model.addAttribute("comment", comment);
		model.addAttribute("message", "Your comment: " + comment);
		return "owners/ownersList"; // Reusing view for simplicity
	}

}
