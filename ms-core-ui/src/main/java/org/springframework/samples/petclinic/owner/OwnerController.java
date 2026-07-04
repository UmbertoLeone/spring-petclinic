package org.springframework.samples.petclinic.owner;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.samples.petclinic.support.CustomPage;
import jakarta.validation.Valid;

@Controller
class OwnerController {

	private static final String VIEWS_OWNER_CREATE_OR_UPDATE_FORM = "owners/createOrUpdateOwnerForm";

	private final RestTemplate restTemplate;

	private final String vetsOwnersUrl;

	private final String visitsUrl;

	public OwnerController(RestTemplate restTemplate, @Value("${services.vets-owners.url}") String vetsOwnersUrl,
			@Value("${services.visits.url}") String visitsUrl) {
		this.restTemplate = restTemplate;
		this.vetsOwnersUrl = vetsOwnersUrl;
		this.visitsUrl = visitsUrl;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id", "*.id");
	}

	@ModelAttribute("owner")
	public Owner findOwner(@PathVariable(name = "ownerId", required = false) Integer ownerId) {
		if (ownerId == null) {
			return new Owner();
		}
		Owner owner = restTemplate.getForObject(vetsOwnersUrl + "/api/owners/" + ownerId, Owner.class);
		if (owner == null) {
			throw new IllegalArgumentException("Owner not found with id: " + ownerId);
		}
		return owner;
	}

	@GetMapping("/owners/new")
	public String initCreationForm() {
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/owners/new")
	public String processCreationForm(@Valid Owner owner, BindingResult result, RedirectAttributes redirectAttributes) {
		if (result.hasErrors()) {
			redirectAttributes.addFlashAttribute("error", "There was an error in creating the owner.");
			return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
		}

		Owner savedOwner = restTemplate.postForObject(vetsOwnersUrl + "/api/owners", owner, Owner.class);
		redirectAttributes.addFlashAttribute("message", "New Owner Created");
		return "redirect:/owners/" + (savedOwner != null ? savedOwner.getId() : "");
	}

	@GetMapping("/owners/find")
	public String initFindForm() {
		return "owners/findOwners";
	}

	@GetMapping("/owners")
	public String processFindForm(@RequestParam(defaultValue = "1") int page, Owner owner, BindingResult result,
			Model model) {
		String lastName = owner.getLastName();
		if (lastName == null) {
			lastName = "";
		}

		String url = vetsOwnersUrl + "/api/owners?lastName=" + lastName + "&page=" + page;
		CustomPage<Owner> ownersResults = restTemplate
			.exchange(url, HttpMethod.GET, null, new ParameterizedTypeReference<CustomPage<Owner>>() {
			})
			.getBody();

		if (ownersResults == null || ownersResults.isEmpty()) {
			result.rejectValue("lastName", "notFound", "not found");
			return "owners/findOwners";
		}

		if (ownersResults.getTotalElements() == 1) {
			owner = ownersResults.getContent().iterator().next();
			return "redirect:/owners/" + owner.getId();
		}

		return addPaginationModel(page, model, ownersResults);
	}

	private String addPaginationModel(int page, Model model, CustomPage<Owner> paginated) {
		List<Owner> listOwners = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listOwners", listOwners);
		return "owners/ownersList";
	}

	@GetMapping("/owners/{ownerId}/edit")
	public String initUpdateOwnerForm() {
		return VIEWS_OWNER_CREATE_OR_UPDATE_FORM;
	}

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
		restTemplate.put(vetsOwnersUrl + "/api/owners/" + ownerId, owner);
		redirectAttributes.addFlashAttribute("message", "Owner Values Updated");
		return "redirect:/owners/{ownerId}";
	}

	@GetMapping("/owners/{ownerId}")
	public ModelAndView showOwner(@PathVariable("ownerId") int ownerId) {
		ModelAndView mav = new ModelAndView("owners/ownerDetails");
		Owner owner = restTemplate.getForObject(vetsOwnersUrl + "/api/owners/" + ownerId, Owner.class);
		if (owner != null) {
			for (Pet pet : owner.getPets()) {
				Visit[] visits = restTemplate.getForObject(visitsUrl + "/api/pets/" + pet.getId() + "/visits",
						Visit[].class);
				if (visits != null) {
					pet.getVisits().clear();
					pet.getVisits().addAll(Arrays.asList(visits));
				}
			}
		}
		mav.addObject(owner);
		return mav;
	}

}
